package com.greenspace.api.features.token;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.greenspace.api.enums.TokenType;
import com.greenspace.api.models.TokenModel;

public interface TokenRepository extends JpaRepository<TokenModel, UUID> {
    Optional<TokenModel> findByToken(String token);

    Optional<TokenModel> findByTokenAndTokenType(String token, TokenType tokenType);

    Optional<List<TokenModel>> findByUserIdAndTokenType(UUID userId, TokenType tokenType);
}
