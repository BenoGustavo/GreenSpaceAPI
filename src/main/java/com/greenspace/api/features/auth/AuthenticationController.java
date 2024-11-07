package com.greenspace.api.features.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.greenspace.api.dto.RecoverPasswordRequestDTO;
import com.greenspace.api.dto.auth.LoginDTO;
import com.greenspace.api.dto.auth.RegisterDTO;
import com.greenspace.api.dto.auth.TokenDTO;
import com.greenspace.api.dto.responses.Response;
import com.greenspace.api.enums.TokenType;
import com.greenspace.api.features.token.TokenService;
import com.greenspace.api.jwt.Jwt;
import com.greenspace.api.models.TokenModel;
import com.greenspace.api.models.UserModel;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
        @Autowired
        private AuthenticationService authenticationService;

        @Autowired
        private TokenService tokenService;

        @Autowired
        private Jwt jwtUtil;

        @Value("${spring.security.oauth2.client.registration.google.client-id}")
        private String clientId;

        @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
        private String redirectUri;

        ///////////////
        // TESTING//
        //////////////

        @GetMapping("/oauth2/testing")
        public String oauthTestingPage() {

                String googleButton = "<a href=\"https://accounts.google.com/o/oauth2/v2/auth?redirect_uri="
                                + redirectUri + "&response_type=code&client_id=" + clientId
                                + "&scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile+openid&access_type=offline\">Sign in with Google</a>";

                return googleButton;
        }

        ////////////////
        // OAUTH2//
        //////////////
        @GetMapping("/oauth2/callback/google")
        public ResponseEntity<Response<Object>> getGmailProfile(@RequestParam("code") String code,
                        @RequestParam("scope") String scope, @RequestParam("authuser") String authUser,
                        @RequestParam("prompt") String prompt) {

                String acessToken = authenticationService.getOauthAccessTokenGoogle(code);
                Object userProfile = authenticationService.getProfileDetailsGoogle(acessToken);

                Response<Object> response = Response.builder()
                                .message("Success")
                                .status(200)
                                .data(userProfile)
                                .build();

                return ResponseEntity.ok(response);
        }

        ///////////////
        // Normal//
        ////////////
        @PostMapping("/signup")
        public ResponseEntity<Response<Object>> signup(@RequestBody RegisterDTO registerDto)
                        throws IllegalArgumentException {

                String confirmationMessage = authenticationService.signup(registerDto);

                Response<Object> response = Response.builder()
                                .message("Success")
                                .status(201)
                                .data(confirmationMessage)
                                .build();

                return ResponseEntity.status(201).body(response);
        }

        @GetMapping("/verify-account")
        public ResponseEntity<Response<Object>> verifyEmail(@RequestParam("token") String token) {
                TokenModel validatedToken = tokenService.findByToken(token, TokenType.ACCOUNT_ACTIVATION);

                Response<Object> response = Response.builder()
                                .message("Success")
                                .status(200)
                                .data("Email successfully verified")
                                .build();

                tokenService.deleteToken(validatedToken);
                return ResponseEntity.ok(response);
        }

        @PostMapping("/login")
        public ResponseEntity<Response<Object>> login(@RequestBody LoginDTO loginDto) {
                UserModel authenticatedUser = authenticationService.authenticate(loginDto);

                String jwtToken = jwtUtil.generateToken(authenticatedUser);

                TokenDTO loginResponse = TokenDTO
                                .builder()
                                .token(jwtToken)
                                .expiresIn(jwtUtil.extractExpirationDate(jwtToken))
                                .build();

                Response<Object> response = Response.builder()
                                .message("Success")
                                .status(200)
                                .data(loginResponse)
                                .build();

                return ResponseEntity.ok(response);
        }

        @PostMapping("/logout")
        public ResponseEntity<Response<Object>> logout(@RequestHeader("Authorization") String authorizationHeader) {
                authenticationService.logout(authorizationHeader);

                Response<Object> response = Response.builder()
                                .message("Success")
                                .status(200)
                                .data("User successfully logged out")
                                .build();

                return ResponseEntity.ok(response);
        }

        @PostMapping("/send-reset-password-token")
        public ResponseEntity<Response<Object>> sendResetPasswordToken(@RequestParam("email") String email) {
                authenticationService.sendRecoverPasswordToken(email);

                Response<Object> response = Response.builder()
                                .message("Success")
                                .status(200)
                                .data("A password reset token has been sent to your email!")
                                .build();

                return ResponseEntity.ok(response);
        }

        @PostMapping("/reset-password")
        public ResponseEntity<Response<Object>> resetPassword(@RequestParam("token") String token,
                        @RequestBody RecoverPasswordRequestDTO newPassword) {
                authenticationService.resetPassword(token, newPassword);

                Response<Object> response = Response.builder()
                                .message("Success")
                                .status(200)
                                .data("Password successfully reset")
                                .build();

                return ResponseEntity.ok(response);
        }
}
