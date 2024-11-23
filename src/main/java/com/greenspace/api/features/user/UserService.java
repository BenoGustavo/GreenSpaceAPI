package com.greenspace.api.features.user;

import org.springframework.stereotype.Service;

import com.greenspace.api.error.http.BadRequest400Exception;
import com.greenspace.api.error.http.NotFound404Exception;
import com.greenspace.api.jwt.Jwt;
import com.greenspace.api.models.UserModel;
import com.greenspace.api.utils.Validation;

@Service
public class UserService {

    private final UserRepository repository;
    private final Jwt jwtManager;
    private final Validation validationUtil;

    public UserService(UserRepository repository, Jwt jwtManager, Validation validationUtil) {
        this.repository = repository;
        this.jwtManager = jwtManager;
        this.validationUtil = validationUtil;
    }

    public UserModel getLoggedUser() {
        String loggedUserEmail = jwtManager.getCurrentUserEmail();

        UserModel loggedUser = repository.findByEmailAddress(loggedUserEmail)
                .orElseThrow(() -> new NotFound404Exception("Perhaps the user is not logged in."));

        return loggedUser;
    }

    public UserModel updateLoggedUserUsername(String newUsername) {
        UserModel loggedUser = getLoggedUser();

        if (!validationUtil.isFieldValid(newUsername, validationUtil.USERNAME_PATTERN)) {
            throw new BadRequest400Exception(
                    "Invalid username, please use only letters, numbers, dots and underscores, a maximum of 30 characters and a @ at the beginning.");
        }

        loggedUser.setUsername(newUsername);
        return repository.save(loggedUser);
    };

    public UserModel updateLoggedUserPhoneNumber(String newPhoneNumber) {
        UserModel loggedUser = getLoggedUser();

        if (!validationUtil.isFieldValid(newPhoneNumber, validationUtil.BRAZILIAM_CELLPHONE_NUMBER_PATTERN)) {
            throw new BadRequest400Exception(
                    "Invalid phone number, please use only numbers and a maximum of 11 characters.");
        }

        loggedUser.setPhoneNumber(newPhoneNumber);
        return repository.save(loggedUser);
    }

    public UserModel updateLoggedUserNickname(String newNickname) {
        UserModel loggedUser = getLoggedUser();

        loggedUser.setNickname(newNickname);

        return repository.save(loggedUser);
    }
}
