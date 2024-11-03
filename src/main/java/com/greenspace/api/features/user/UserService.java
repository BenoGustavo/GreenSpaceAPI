package com.greenspace.api.features.user;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.greenspace.api.models.UserModel;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserModel getUserById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }
}
