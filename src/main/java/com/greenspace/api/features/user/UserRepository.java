package com.greenspace.api.features.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.greenspace.api.models.UserModel;

public interface UserRepository extends JpaRepository<UserModel, UUID> {
    Optional<UserModel> findByEmailAddress(String emailAddress);
}
