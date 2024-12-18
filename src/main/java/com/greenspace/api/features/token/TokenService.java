package com.greenspace.api.features.token;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.greenspace.api.enums.TokenType;
import com.greenspace.api.error.http.BadRequest400Exception;
import com.greenspace.api.features.profile.ProfileService;
import com.greenspace.api.features.user.UserRepository;
import com.greenspace.api.models.ProfileModel;
import com.greenspace.api.models.TokenModel;
import com.greenspace.api.models.UserModel;

@Service
public class TokenService {

    // Should be configured in application.properties
    @Value("${verification.token.expiry.hours}")
    private int tokenExpiryHours;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private ProfileService profileService;

    public TokenModel createVerificationToken(UserModel user, TokenType tokenType) {
        TokenModel token = TokenModel.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .tokenType(tokenType)
                .expiryDate(LocalDateTime.now().plusHours(tokenExpiryHours))
                .build();
        tokenRepository.save(token);
        return token;
    }

    public boolean isTokenExpired(TokenModel token) {
        return token.getExpiryDate().isBefore(LocalDateTime.now());
    }

    public void deleteToken(TokenModel token) {
        tokenRepository.delete(token);
    }

    public TokenModel findByToken(String token, TokenType tokenType) {
        Optional<TokenModel> verificationToken = tokenRepository.findByTokenAndTokenType(token, tokenType);

        if (!verificationToken.isPresent()) {
            throw new BadRequest400Exception("Invalid verification token");
        }

        TokenModel tokenEntity = verificationToken.get();

        if (isTokenExpired(tokenEntity)) {
            throw new BadRequest400Exception("Verification token has expired");
        }

        // If the token is for account activation, activate it
        if (tokenType == TokenType.ACCOUNT_ACTIVATION) {
            UserModel user = tokenEntity.getUser();

            // Ativa a conta e cria um perfil
            user.setIsEmailValidated(true);
            ProfileModel userProfile = profileService.create(user);

            user.setProfile(userProfile);
            userRepository.save(user);
        }

        return tokenEntity;
    }

    public List<TokenModel> findByUserIdAndTokenType(UUID userId, TokenType tokenType) {
        return tokenRepository.findByUserIdAndTokenType(userId, tokenType);
    }
}
