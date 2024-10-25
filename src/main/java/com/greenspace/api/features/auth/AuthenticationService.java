package com.greenspace.api.features.auth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.greenspace.api.dto.auth.LoginDTO;
import com.greenspace.api.dto.auth.RegisterDTO;
import com.greenspace.api.dto.email.EmailDTO;
import com.greenspace.api.enums.PermissionLevel;
import com.greenspace.api.enums.TokenType;
import com.greenspace.api.error.http.BadRequest400Exception;
import com.greenspace.api.features.email.EmailService;
import com.greenspace.api.features.token.TokenService;
import com.greenspace.api.features.user.UserRepository;
import com.greenspace.api.models.UserModel;

@Service
public class AuthenticationService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;
    @Autowired
    private TokenService tokenService;

    private static final String USERNAME_REGEX = "@([A-Za-z0-9._]{1,30})";
    private static final Pattern USERNAME_PATTERN = Pattern.compile(USERNAME_REGEX);

    // Cadastra o usuario na plataforma através dos meios tradicionais (Sem admin)
    public String signup(RegisterDTO registerDto) {
        // Procura usuario no banco de dados
        UserModel user = userRepository.findByEmailAddress(registerDto.getEmail()).orElse(null);
        UserModel userWithUsername = userRepository.findByUsername(registerDto.getUsername()).orElse(null);
        boolean isUsernameInUse = userWithUsername != null;
        boolean isEmailInUse = user == null;
        boolean isAccountActivated = user != null && user.getIsEmailValidated();

        // Verifica se o email já está em uso e se a conta já está ativada
        if (isEmailInUse && isAccountActivated) {
            throw new BadRequest400Exception("Email already in use");
        }

        // Verifica se o username já está em uso
        if (isUsernameInUse && isAccountActivated) {
            throw new BadRequest400Exception("Username already in use");
        }

        if (!isUsernameValid(registerDto.getUsername())) {
            throw new BadRequest400Exception(
                    "Invalid username, please use only letters, numbers, dots and underscores, a maximum of 30 characters and a @ at the beginning");
        }

        // Se a conta não está ativada, deleta o usuario e todos os tokens associados a
        // ele
        if (!isAccountActivated && user != null) {
            userRepository.deleteAllUserTokens(user);
            userRepository.delete(user);
        }

        UserModel userEntity = UserModel.builder()
                .username(registerDto.getUsername())
                .nickname(registerDto.getNickname())
                .emailAddress(registerDto.getEmail())
                .password(passwordEncoder.encode(registerDto.getPassword()))
                .isOnline(false)
                .isEmailValidated(false)
                .isDeactivated(false)
                .isBanned(false)
                .permissionLevel(PermissionLevel.USER)
                .build();

        UserModel newUser = userRepository.save(userEntity);

        // Deve ser mudado futuramente indexando para o front-end que ira fazer a
        // requisição para o back-end
        String verificationLink = "http://localhost:8080/public/api/v1/auth/verify-account?token="
                + tokenService.createVerificationToken(newUser, TokenType.ACCOUNT_ACTIVATION).getToken();

        EmailDTO accountVerificationEmail = EmailDTO.builder()
                .userEmail(userEntity.getEmailAddress())
                .subject("Verifique sua conta no GreenSpace")
                .message("Clique no link para verificar sua conta: " + verificationLink)
                .build();

        emailService.sendEmail(accountVerificationEmail);

        return "A email has been sent to your email address. Please verify your email address to login";
    }

    UserModel authenticate(LoginDTO loginDto) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isUsernameValid(String username) {
        if (username == null) {
            return false;
        }
        Matcher matcher = USERNAME_PATTERN.matcher(username);
        return matcher.matches();
    }

}
