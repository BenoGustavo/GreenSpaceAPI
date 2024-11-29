package com.greenspace.api.features.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.greenspace.api.dto.auth.LoginDTO;
import com.greenspace.api.dto.email.EmailDTO;
import com.greenspace.api.enums.ImageType;
import com.greenspace.api.enums.TokenType;
import com.greenspace.api.error.http.BadRequest400Exception;
import com.greenspace.api.error.http.NotFound404Exception;
import com.greenspace.api.features.address.AddressService;
import com.greenspace.api.features.profile.ProfileService;
import com.greenspace.api.features.token.TokenRepository;
import com.greenspace.api.features.token.TokenService;
import com.greenspace.api.jwt.Jwt;
import com.greenspace.api.models.TokenModel;
import com.greenspace.api.models.UserModel;
import com.greenspace.api.utils.EmailSender;
import com.greenspace.api.utils.ImageUploader;
import com.greenspace.api.utils.Validation;

import jakarta.mail.MessagingException;

@Service
public class UserService {

    private final UserRepository repository;
    private final Jwt jwtManager;
    private final Validation validationUtil;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final AddressService addressService;
    private final ProfileService profileService;
    private final TokenService tokenService;
    private final EmailSender emailSender;
    private final TokenRepository tokenRepository;
    private final ImageUploader imageUploader;

    public UserService(
            UserRepository repository,
            Jwt jwtManager,
            Validation validationUtil,
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            AddressService addressService,
            ProfileService profileService,
            TokenService tokenService,
            EmailSender emailSender,
            TokenRepository tokenRepository,
            ImageUploader imageUploader) {
        this.repository = repository;
        this.jwtManager = jwtManager;
        this.validationUtil = validationUtil;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.addressService = addressService;
        this.profileService = profileService;
        this.tokenService = tokenService;
        this.emailSender = emailSender;
        this.tokenRepository = tokenRepository;
        this.imageUploader = imageUploader;
    }

    public UserModel getLoggedUser() {
        String loggedUserEmail = jwtManager.getCurrentUserEmail();

        UserModel loggedUser = repository.findByEmailAddress(loggedUserEmail)
                .orElseThrow(() -> new NotFound404Exception("Perhaps the user is not logged in."));

        return loggedUser;
    }

    public UserModel updateLoggedUserUsername(String newUsername) {
        UserModel loggedUser = getLoggedUser();

        if (!validationUtil.isFieldValid(newUsername, validationUtil.USERNAME_PATTERN)) {
            throw new BadRequest400Exception(
                    "Invalid username, please use only letters, numbers, dots and underscores, a maximum of 30 characters and a @ at the beginning.");
        }

        loggedUser.setUsername(newUsername);
        return repository.save(loggedUser);
    };

    public UserModel updateLoggedUserPhoneNumber(String newPhoneNumber) {
        UserModel loggedUser = getLoggedUser();

        System.out.println(newPhoneNumber);

        if (!validationUtil.isFieldValid(newPhoneNumber, validationUtil.BRAZILIAM_CELLPHONE_NUMBER_PATTERN)) {
            throw new BadRequest400Exception(
                    "Invalid phone number, please use only numbers and a maximum of 11 characters.");
        }

        loggedUser.setPhoneNumber(newPhoneNumber);
        return repository.save(loggedUser);
    }

    public UserModel updateLoggedUserNickname(String newNickname) {
        UserModel loggedUser = getLoggedUser();

        loggedUser.setNickname(newNickname);

        return repository.save(loggedUser);
    }

    public UserModel updateLoggedUserProfilePicture(MultipartFile file) {
        UserModel loggedUser = getLoggedUser();

        if (file.isEmpty()) {
            throw new BadRequest400Exception("File is empty perhaps you forgot to send one?.");
        }

        String profilePictureUrl = imageUploader.uploadImage(file, loggedUser.getId(), ImageType.PROFILE_PICTURE);
        loggedUser.getProfile().setProfilePicture(profilePictureUrl);

        return repository.save(loggedUser);
    }

    public UserModel deactivateLoggedUser(LoginDTO userCredentials) {
        UserModel loggedUser = getLoggedUser();

        if (!checkEmailAndPassword(loggedUser.getEmailAddress(), userCredentials.getPassword())) {
            throw new BadRequest400Exception(
                    "Can't deactivate account, wrong password or email.");
        }

        loggedUser.setIsDeactivated(true);
        loggedUser.setIsOnline(false);

        if (loggedUser.getAddress() != null) {
            addressService.softdeleteLoggedUserAddress();
        }
        if (loggedUser.getProfile() != null) {
            profileService.softdeleteUserProfile();
        }
        return repository.save(getLoggedUser());
    }

    private boolean checkEmailAndPassword(String email, String password) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return passwordEncoder.matches(password, userDetails.getPassword());
    }

    public void sendAccountActivationToken(String accountEmail) {
        UserModel user = repository.findByEmailAddress(accountEmail)
                .orElseThrow(() -> new NotFound404Exception("User not found."));

        // Verifica se a conta já está ativada
        if (!user.getIsDeactivated()) {
            throw new BadRequest400Exception("The account with the email " + accountEmail + " is not deactivated.");
        }

        TokenModel accountReactivationToken = tokenService.createVerificationToken(user,
                TokenType.ACCOUNT_REACTIVATION_TOKEN);

        // Criando o conteúdo do email
        var emailContentBuilder = EmailDTO.builder()
                .userEmail(user.getEmailAddress())
                .subject("GreenSpace - Recuperação de senha");

        // Enviar email com o token
        EmailDTO finishedEmailContent = emailContentBuilder
                .message("Olá, " + user.getUsername() + "!<br><br>"
                        + "Você solicitou a reativação da sua conta no GreenSpace. Para reativá-la, clique no link abaixo:<br><br>"
                        + "<a href='http://localhost:8080/api/user/reactivate-account?token="
                        + accountReactivationToken.getToken() + "'>Clique aqui para reativar sua conta</a><br><br>"
                        + "Se você não solicitou a reativação da sua conta, por favor, ignore este email.<br><br>"
                        + "Atenciosamente, equipe GreenSpace.")
                .build();

        try {
            emailSender.sendEmail(finishedEmailContent);
        } catch (MessagingException e) {
            throw new BadRequest400Exception("Error sending email.");
        }
    }

    public UserModel reactivateAccount(String token) {
        TokenModel validatedToken = tokenService.findByToken(token, TokenType.ACCOUNT_REACTIVATION_TOKEN);
        UserModel user = validatedToken.getUser();

        user.setIsDeactivated(false);
        if (user.getAddress() != null) {
            addressService.restoreSoftdeletedUserAddress(user);
        }
        if (user.getProfile() != null) {
            profileService.restoreProfile(user);
        }

        tokenRepository.delete(validatedToken);
        return repository.save(user);
    }
}
