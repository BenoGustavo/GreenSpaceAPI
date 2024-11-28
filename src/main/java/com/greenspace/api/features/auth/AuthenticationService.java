package com.greenspace.api.features.auth;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.greenspace.api.dto.RecoverPasswordRequestDTO;
import com.greenspace.api.dto.auth.LoginDTO;
import com.greenspace.api.dto.auth.RegisterDTO;
import com.greenspace.api.dto.auth.UserProfileOauth2GmailDTO;
import com.greenspace.api.dto.email.EmailDTO;
import com.greenspace.api.enums.PermissionLevel;
import com.greenspace.api.enums.TokenType;
import com.greenspace.api.error.http.BadRequest400Exception;
import com.greenspace.api.error.http.Conflict409Exception;
import com.greenspace.api.error.http.Unauthorized401Exception;
import com.greenspace.api.features.token.TokenService;
import com.greenspace.api.features.user.UserRepository;
import com.greenspace.api.features.user.banned.BannedUsersRepository;
import com.greenspace.api.features.user.permissions.PermissionsRepository;
import com.greenspace.api.jwt.Jwt;
import com.greenspace.api.models.BannedUsersModel;
import com.greenspace.api.models.PermissionModel;
import com.greenspace.api.models.TokenModel;
import com.greenspace.api.models.UserModel;
import com.greenspace.api.utils.EmailSender;
import com.greenspace.api.utils.Validation;

import jakarta.mail.MessagingException;

@Service
public class AuthenticationService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailSender emailSender;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private Jwt jwtUtil;
    @Autowired
    private PermissionsRepository permissionsRepository;
    @Autowired
    private BannedUsersRepository banRepository;
    @Autowired
    private Validation validationUtil;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    ////////////////
    // OAUTH2//
    //////////////

    // Pegar o token de acesso do google através da autorização OAuth2
    public String getOauthAccessTokenGoogle(String code) {
        // Cria um objeto RestTemplate para fazer requisições HTTP
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Adiciona os parametros necessários para pegar o token de acesso
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("redirect_uri", redirectUri);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("scope", "https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile");
        params.add("scope", "https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email");
        params.add("scope", "openid");
        params.add("grant_type", "authorization_code");

        // Cria uma entidade HTTP com os parametros e headers
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, httpHeaders);

        String url = "https://oauth2.googleapis.com/token";
        String response = restTemplate.postForObject(url, requestEntity, String.class);

        // Pega o token de acesso do objeto JSON
        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
        String accessToken = jsonObject.get("access_token").getAsString();

        return accessToken;
    }

    // Recolhe as informações do perfil do usuario do google
    public UserProfileOauth2GmailDTO getProfileDetailsGoogle(String accessToken) {

        // Cria um objeto RestTemplate para fazer requisições HTTP
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(accessToken);

        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);

        String url = "https://www.googleapis.com/oauth2/v2/userinfo";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

        // Converte o JSON para um objeto UserProfileOauth2GmailDTO
        Gson gson = new Gson();
        UserProfileOauth2GmailDTO userProfile = gson.fromJson(response.getBody(), UserProfileOauth2GmailDTO.class);

        // Verifica se o usuario ja existe no banco de dados
        Optional<UserModel> userModelFromDatabase = userRepository.findByEmailAddress(userProfile.getEmail());

        if (userModelFromDatabase.isPresent() || !userModelFromDatabase.isEmpty()) {
            userProfile.setEmailAlreadyRegistered(true);
        } else {
            userProfile.setEmailAlreadyRegistered(false);
        }

        return userProfile;
    }

    //////////////////////////////
    // CADASTRO PADRÃO///////////
    ////////////////////////////

    // Cadastra o usuario na plataforma através dos meios tradicionais (Sem admin)
    public String signup(RegisterDTO registerDto) {
        // Procura usuario no banco de dados
        UserModel user = userRepository.findByEmailAddress(registerDto.getEmail()).orElse(null);

        boolean isUsernameInUse = userRepository.existsByUsername(registerDto.getUsername());
        boolean isEmailInUse = user != null;
        boolean isAccountActivated = user != null && user.getIsEmailValidated();

        if (isAccountActivated) {
            throw new Conflict409Exception("Account already activated");
        }

        // Verifica se o email já está em uso e se a conta já está ativada
        if (isEmailInUse && isAccountActivated) {
            throw new Conflict409Exception("Email already in use");
        }

        // Verifica se o username já está em uso
        if (isUsernameInUse && isAccountActivated) {
            throw new Conflict409Exception("Username already in use");
        }

        if (!validationUtil.isFieldValid(registerDto.getUsername(), validationUtil.USERNAME_PATTERN)) {
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

        PermissionModel userPermissionEntity = permissionsRepository.findByName(PermissionLevel.ROLE_USER).orElseThrow(
                () -> new BadRequest400Exception("Basic user permission not found, please contact the administrator"));

        UserModel userEntity;
        userEntity = UserModel.builder()
                .username(registerDto.getUsername())
                .nickname(registerDto.getNickname())
                .emailAddress(registerDto.getEmail())
                .password(passwordEncoder.encode(registerDto.getPassword()))
                .isOnline(false)
                .isEmailValidated(false)
                .isDeactivated(false)
                .permissionLevel(userPermissionEntity)
                .build();

        UserModel newUser = userRepository.save(userEntity);

        // Deve ser mudado futuramente indexando para o front-end que ira fazer a
        // requisição para o back-end
        String verificationLink = "http://localhost:8080/api/auth/verify-account?token="
                + tokenService.createVerificationToken(newUser, TokenType.ACCOUNT_ACTIVATION).getToken();

        EmailDTO accountVerificationEmail = EmailDTO.builder()
                .userEmail(userEntity.getEmailAddress())
                .subject("Verifique sua conta no GreenSpace")
                .message("Clique no link para verificar sua conta: " + verificationLink)
                .build();

        try {
            emailSender.sendEmail(accountVerificationEmail);
        } catch (MessagingException ex) {
            throw new BadRequest400Exception("Error sending email: " + ex.getMessage());
        }

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

        if (user.getBan() != null) {
            BannedUsersModel banRecord = banRepository.findByUserEmailAddress(user.getEmailAddress()).get();
            throw new Unauthorized401Exception("User is banned - REASON: " + banRecord.getReason());
        }

        if (user.getIsDeactivated() != null && user.getIsDeactivated()) {
            throw new Unauthorized401Exception(
                    "Account is currently deactivated, the owner must reactivate it to continue");
        }

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

    public void sendRecoverPasswordToken(String email) {
        // Procura pelo email para enviar o token de recuperação de senha
        UserModel user = userRepository.findByEmailAddress(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getDeletedAt() != null) {
            throw new Unauthorized401Exception("User is currently deleted user");
        }

        if (user.getBan() != null) {
            throw new Unauthorized401Exception("User is banned");
        }

        if (!user.getIsEmailValidated()) {
            throw new Unauthorized401Exception("Account not verified, check your email adress or signup");
        }

        var emailContentBuilder = EmailDTO.builder()
                .userEmail(user.getEmailAddress())
                .subject("GreenSpace - Recuperação de senha");

        TokenModel validToken = null;
        // Procura e deleta todos os tokens de recuperação de senha já expirados
        List<TokenModel> tokens = tokenService.findByUserIdAndTokenType(user.getId(),
                TokenType.PASSWORD_RECOVERY);

        // Verifica se o usuario ja pediu um token de recuperação de senha
        for (TokenModel token : tokens) {
            if (!tokenService.isTokenExpired(token)) {
                validToken = token;
            }
            tokenService.deleteToken(token);
        }

        // se tiver pedido e for valido envia o mesmo token
        if (validToken != null) {
            EmailDTO finishedEmailContent = emailContentBuilder
                    .message("Token para recuperação da conta: " + validToken.getToken())
                    .build();

            try {
                emailSender.sendEmail(finishedEmailContent);
            } catch (MessagingException ex) {
                throw new BadRequest400Exception("Error sending email: " + ex.getMessage());
            }

            return;
        }

        // Se não cria um novo token e envia ao usuario
        TokenModel token = tokenService.createVerificationToken(user, TokenType.PASSWORD_RECOVERY);

        EmailDTO finishedEmailContent = emailContentBuilder
                .message("Token para recuperação da conta: " + token.getToken())
                .build();

        try {
            emailSender.sendEmail(finishedEmailContent);
        } catch (MessagingException ex) {
            throw new BadRequest400Exception("Error sending email: " + ex.getMessage());
        }
    }

    public void resetPassword(String token, RecoverPasswordRequestDTO newPassword) {
        TokenModel verificationToken = tokenService.findByToken(token, TokenType.PASSWORD_RECOVERY);

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expired, please get a new one");
        }

        if (!newPassword.getPassword().equals(newPassword.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        UserModel user = verificationToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword.getPassword()));
        userRepository.save(user);
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
