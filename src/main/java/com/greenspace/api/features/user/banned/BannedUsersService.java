package com.greenspace.api.features.user.banned;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.greenspace.api.dto.banned.BanUsersDTO;
import com.greenspace.api.error.http.NotFound404Exception;
import com.greenspace.api.features.user.UserRepository;
import com.greenspace.api.jwt.Jwt;
import com.greenspace.api.models.BannedUsersModel;
import com.greenspace.api.models.UserModel;

import jakarta.transaction.Transactional;

@Service
public class BannedUsersService {
    private final BannedUsersRepository bannedUsersRepository;
    private final UserRepository userRepository;
    private final Jwt jwt;

    public BannedUsersService(BannedUsersRepository bannedUsersRepository, UserRepository userRepository, Jwt jwt) {
        this.bannedUsersRepository = bannedUsersRepository;
        this.userRepository = userRepository;
        this.jwt = jwt;
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
                                (LocalDateTime.of(banUsersDTO.getYear(), banUsersDTO.getMonth(),
                                        banUsersDTO.getDayOfMonth(), banUsersDTO.getHour(),
                                        banUsersDTO.getMinute()))))
                .bannedBy(adminThatBannedTheUser)
                .build();

        if (bannedUser.getBannedUntil().before(bannedUser.getBannedAt())) {
            throw new IllegalArgumentException("Banishment date cannot be before the banishment date!");
        }

        user.setBan(bannedUser);
        bannedUsersRepository.save(bannedUser);

        userRepository.save(user);
    }

    public void unbanUser(String userEmailAddress) {

        // // Verficia se o motivo de revogação é nulo
        // if (RevokeDTO.getRevocationReason() == null) {
        // throw new IllegalArgumentException("Revocation reason cannot be null!");
        // }

        // // Verifica se está em branco ou vazio
        // if (RevokeDTO.getRevocationReason().isBlank() ||
        // RevokeDTO.getRevocationReason().isEmpty()) {
        // throw new IllegalArgumentException("Revocation reason cannot be empty!");
        // }

        // Acha o usuário banido
        BannedUsersModel bannedUser = bannedUsersRepository
                .findByUserEmailAddress(userEmailAddress).orElseThrow(
                        () -> new NotFound404Exception("User " + userEmailAddress
                                + " may not be banned or not found!"));

        bannedUser.getUser().setBan(null);

        bannedUsersRepository.delete(bannedUser);
        userRepository.save(bannedUser.getUser());
    }

    // Desbanir usuários automaticamente que já passaram do tempo de banimento
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void unbanExpiredUsers() {
        List<BannedUsersModel> expiredBans = bannedUsersRepository
                .findAllByBannedUntilBefore(Timestamp.valueOf(LocalDateTime.now()));
        for (BannedUsersModel bannedUser : expiredBans) {
            unbanUser(bannedUser.getUser().getEmailAddress());
        }
    }

    public boolean isUserBanned(String emailAddress) {
        return bannedUsersRepository.existsByEmailAddress(emailAddress);
    }
}
