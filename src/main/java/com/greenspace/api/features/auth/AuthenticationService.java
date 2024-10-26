package com.greenspace.api.features.auth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.greenspace.api.dto.auth.LoginDTO;
import com.greenspace.api.dto.auth.RegisterDTO;
import com.greenspace.api.dto.email.EmailDTO;
import com.greenspace.api.enums.PermissionLevel;
import com.greenspace.api.enums.TokenType;
import com.greenspace.api.error.http.BadRequest400Exception;
import com.greenspace.api.error.http.Unauthorized401Exception;
import com.greenspace.api.features.email.EmailService;
import com.greenspace.api.features.token.TokenService;
import com.greenspace.api.features.user.UserRepository;
import com.greenspace.api.jwt.Jwt;
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
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private Jwt jwtUtil;

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

        if (!isFieldValid(registerDto.getUsername(), USERNAME_PATTERN)) {
            throw new BadRequest400Exception(
                    "Invalid username, please use only letters, numbers, dots and underscores, a maximum of 30 characters and a @ at the beginning");
        }

        // Verifica se as senhas são iguais
        if (!registerDto.getPassword().equals(registerDto.getConfirmPassword())) {
            throw new BadRequest400Exception("Passwords do not match");
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

    public UserModel authenticate(LoginDTO loginDto)
            throws UsernameNotFoundException, Unauthorized401Exception {

        // Verifica se ja existe um usuario logado
        if (jwtUtil.isUserAuthenticated()) {
            throw new Unauthorized401Exception("Logout first before authenticating");
        }

        // Tenta realizar o login utilizando email e senha
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getEmail(),
                            loginDto.getPassword()));
        } catch (AuthenticationException e) {
            throw new Unauthorized401Exception("Invalid email or password");
        }

        // Procura o usuario no banco de dados
        UserModel user = userRepository.findByEmailAddress(loginDto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Verifica se o usuario ja ativou sua conta
        if (!user.getIsEmailValidated()) {
            throw new Unauthorized401Exception("Account not verified, check your email adress or signup");
        }

        // Verifica se o usuario não deletou sua conta
        if (user.getDeletedAt() != null) {
            throw new Unauthorized401Exception("User is currently deleted user");
        }

        // Atualiza o ultimo login do usuario e verifica se o usuario ja está online e
        // então muda o status de online
        userRepository.updateLastLogin(user.getId());

        if (!isUserOnline(user)) {
            userRepository.toggleIsOnline(user.getId());
        }

        return user;
    }

    public void logout(String authHeader) {
        // Verifica se o usuario esta autenticado
        if (!jwtUtil.isUserAuthenticated()) {
            throw new Unauthorized401Exception("User is not authenticated");
        }

        // Procura o usuario no banco de dados
        UserModel user = userRepository
                .findByEmailAddress(jwtUtil.getCurrentUserEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Verifica se o usuario ja deletou sua conta
        if (user.getDeletedAt() != null) {
            throw new Unauthorized401Exception("User is currently deleted user");
        }

        // Verifica se o usuario ja está online e então muda o status de online
        if (isUserOnline(user)) {
            userRepository.toggleIsOnline(user.getId());
        }

        // Extraindo token do header
        String token = extractTokenFromHeader(authHeader);

        // Invalida o token do usuario
        jwtUtil.invalidateUserToken(token);
    }

    public boolean isFieldValid(String field, Pattern pattern) {
        if (field == null) {
            return false;
        }
        Matcher matcher = pattern.matcher(field);
        return matcher.matches();
    }

    private boolean isUserOnline(UserModel user) {
        return user.getIsOnline();
    }

    private String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
}
