package com.greenspace.api.features.user.banned;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.greenspace.api.dto.banned.BanRevokeDTO;
import com.greenspace.api.dto.banned.BanUsersDTO;
import com.greenspace.api.error.http.NotFound404Exception;
import com.greenspace.api.features.user.UserRepository;
import com.greenspace.api.jwt.Jwt;
import com.greenspace.api.models.BanRevokeLogModel;
import com.greenspace.api.models.BannedUsersModel;
import com.greenspace.api.models.UserModel;

import jakarta.transaction.Transactional;

@Service
public class BannedUsersService {
        private final BannedUsersRepository bannedUsersRepository;
        private final UserRepository userRepository;
        private final Jwt jwt;
        private final BanRevokeRepository banRevokeRepository;
        private final UserModel machineUser;

        public BannedUsersService(BannedUsersRepository bannedUsersRepository, UserRepository userRepository, Jwt jwt,
                        BanRevokeRepository banRevokeRepository) {
                this.bannedUsersRepository = bannedUsersRepository;
                this.userRepository = userRepository;
                this.jwt = jwt;
                this.banRevokeRepository = banRevokeRepository;
                this.machineUser = userRepository.findByEmailAddress("machine@scheduled.com").orElse(null);
        }

        public void banUser(BanUsersDTO banUsersDTO) {
                UserModel user = userRepository.findByEmailAddress(banUsersDTO.getUserEmailAddress()).orElseThrow(
                                () -> new NotFound404Exception(banUsersDTO.getUserEmailAddress() + " not found!"));

                UserModel adminThatBannedTheUser = userRepository.findByEmailAddress(jwt.getCurrentUserEmail())
                                .orElseThrow(
                                                () -> new NotFound404Exception("Admin not found!"));

                // Verifica se o usuario ja foi banido antes
                if (isUserBanned(user.getEmailAddress())) {
                        throw new NotFound404Exception("User " + user.getEmailAddress() + " may be already banned!");
                }

                // Cria um novo banimento
                BannedUsersModel bannedUser = BannedUsersModel.builder()
                                .user(user)
                                .reason(banUsersDTO.getReason())
                                .bannedAt(Timestamp.valueOf(LocalDateTime.now()))
                                .bannedUntil(
                                                Timestamp.valueOf(
                                                                (LocalDateTime.of(banUsersDTO.getYear(),
                                                                                banUsersDTO.getMonth(),
                                                                                banUsersDTO.getDayOfMonth(),
                                                                                banUsersDTO.getHour(),
                                                                                banUsersDTO.getMinute()))))
                                .bannedBy(adminThatBannedTheUser)
                                .build();

                if (bannedUser.getBannedUntil().before(bannedUser.getBannedAt())) {
                        throw new IllegalArgumentException("Banishment date cannot be before the today!");
                }

                user.setBan(bannedUser);
                bannedUsersRepository.save(bannedUser);

                userRepository.save(user);
        }

        public void unbanUser(BanRevokeDTO RevokeDTO) {

                // Verficia se o motivo de revogação é nulo
                if (RevokeDTO.getReason() == null) {
                        throw new IllegalArgumentException("Revocation reason cannot be null!");
                }

                // Verifica se está em branco ou vazio
                if (RevokeDTO.getReason().isBlank() ||
                                RevokeDTO.getReason().isEmpty()) {
                        throw new IllegalArgumentException("Revocation reason cannot be empty!");
                }

                UserModel adminThatBanned = userRepository.findByEmailAddress(jwt.getCurrentUserEmail())
                                .orElseThrow(() -> new NotFound404Exception(
                                                "Admin not found, how do you even got here wtf?!"));

                // Acha o usuário banido
                BannedUsersModel bannedUser = bannedUsersRepository
                                .findByUserEmailAddress(RevokeDTO.getUserEmailAddress()).orElseThrow(
                                                () -> new NotFound404Exception("User " + RevokeDTO.getUserEmailAddress()
                                                                + " may not be banned or not found!"));

                // Salva o log de revogação
                BanRevokeLogModel banRevokeLog = BanRevokeLogModel.builder()
                                .revokedBy(adminThatBanned)
                                .reason(RevokeDTO.getReason())
                                .revokedAt(Timestamp.valueOf(LocalDateTime.now()))
                                // ban info
                                .bannedUser(bannedUser.getUser())
                                .bannedBy(bannedUser.getBannedBy())
                                .banReason(bannedUser.getReason())
                                .bannedAt(bannedUser.getBannedAt())
                                .bannedUntil(bannedUser.getBannedUntil())
                                .bannedUserEmailAddress(bannedUser.getUser().getEmailAddress())
                                .build();

                bannedUser.getUser().setBan(null);

                bannedUsersRepository.delete(bannedUser);
                banRevokeRepository.save(banRevokeLog);
                userRepository.save(bannedUser.getUser());
        }

        // Desbanir usuários automaticamente que já passaram do tempo de banimento
        // @Scheduled(fixedRate = 600) // mais ou menos 1 minutos
        @Scheduled(fixedRate = 3600000) // mais ou menos 60 minutos
        @Transactional
        public void unbanExpiredUsers() {
                List<BannedUsersModel> expiredBans = bannedUsersRepository
                                .findAllByBannedUntilBefore(Timestamp.valueOf(LocalDateTime.now()));

                BanRevokeDTO revokeDTO = BanRevokeDTO.builder()
                                .reason("Automaticly unbaned because banishment expired!")
                                .build();

                for (BannedUsersModel bannedUser : expiredBans) {
                        revokeDTO.setUserEmailAddress(bannedUser.getUser().getEmailAddress());
                        scheduledUnban(revokeDTO);
                }
        }

        private void scheduledUnban(BanRevokeDTO RevokeDTO) {
                // Acha o usuário banido
                BannedUsersModel bannedUser = bannedUsersRepository
                                .findByUserEmailAddress(RevokeDTO.getUserEmailAddress()).orElseThrow(
                                                () -> new NotFound404Exception("User " + RevokeDTO.getUserEmailAddress()
                                                                + " may not be banned or not found!"));

                // Salva o log de revogação
                BanRevokeLogModel banRevokeLog = BanRevokeLogModel.builder()
                                .revokedBy(machineUser)
                                .reason(RevokeDTO.getReason())
                                .revokedAt(Timestamp.valueOf(LocalDateTime.now()))
                                // ban info
                                .bannedUser(bannedUser.getUser())
                                .bannedBy(bannedUser.getBannedBy())
                                .banReason(bannedUser.getReason())
                                .bannedAt(bannedUser.getBannedAt())
                                .bannedUntil(bannedUser.getBannedUntil())
                                .bannedUserEmailAddress(bannedUser.getUser().getEmailAddress())
                                .build();

                bannedUser.getUser().setBan(null);

                bannedUsersRepository.delete(bannedUser);
                banRevokeRepository.save(banRevokeLog);
                userRepository.save(bannedUser.getUser());
        }

        public boolean isUserBanned(String emailAddress) {
                return bannedUsersRepository.existsByEmailAddress(emailAddress);
        }
}
