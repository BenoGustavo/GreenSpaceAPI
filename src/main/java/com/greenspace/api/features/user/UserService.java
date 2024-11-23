package com.greenspace.api.features.user;

import org.springframework.stereotype.Service;

import com.greenspace.api.error.http.NotFound404Exception;
import com.greenspace.api.jwt.Jwt;
import com.greenspace.api.models.UserModel;

@Service
public class UserService {

    private final UserRepository repository;
    private final Jwt jwtManager;

    public UserService(UserRepository repository, Jwt jwtManager) {
        this.repository = repository;
        this.jwtManager = jwtManager;
    }

    public UserModel getLoggedUser() {
        String loggedUserEmail = jwtManager.getCurrentUserEmail();

        UserModel loggedUser = repository.findByEmailAddress(loggedUserEmail)
                .orElseThrow(() -> new NotFound404Exception("Perhaps the user is not logged in."));

        return loggedUser;
    }
}
