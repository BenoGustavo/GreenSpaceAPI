package com.greenspace.api.features.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.greenspace.api.models.AddressModel;
import com.greenspace.api.models.UserModel;

public interface UserRepository extends JpaRepository<UserModel, UUID> {
    Optional<UserModel> findByEmailAddress(String emailAddress);

    Optional<UserModel> findByUsername(String username);

    @Modifying
    @Transactional
    @Query("DELETE FROM TokenModel v WHERE v.user = :user")
    void deleteAllUserTokens(@Param("user") UserModel user);

    @Modifying
    @Transactional
    @Query("UPDATE UserModel u SET u.loggedInAt = CURRENT_TIMESTAMP WHERE u.id = :id")
    void updateLastLogin(@Param("id") UUID id);

    @Modifying
    @Transactional
    @Query("UPDATE UserModel u SET u.isOnline = NOT u.isOnline WHERE u.id = :id")
    void toggleIsOnline(@Param("id") UUID id);

    @Query("SELECT u.address FROM UserModel u WHERE u.emailAddress = :email")
    Optional<AddressModel> findAddressByEmailAddress(@Param("email") String email);

    boolean existsByUsername(String username);

    boolean existsByEmailAddress(String emailAddress);

    Optional<UserModel> findByEmailAddressAndIsEmailValidated(String email, boolean isEmailValidated);
}
