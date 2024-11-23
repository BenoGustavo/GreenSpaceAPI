package com.greenspace.api.features.user;

import org.springframework.stereotype.Service;

import com.greenspace.api.dto.profile.UserUpdateDTO;
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

    // TEM QUE ARRUMAR POR QUE ISSO DAQUI TEM QUE LANÇAR UM ERRO QUANDO NÃO PASSA
    // #TODO
    public UserModel updateLoggedUser(UserUpdateDTO updatedUserUpdateDTO) {
        UserModel loggedUser = getLoggedUser();

        if (validationUtil.isFieldValid(
                updatedUserUpdateDTO.getUsername(),
                validationUtil.USERNAME_PATTERN)) {

            loggedUser.setUsername(updatedUserUpdateDTO.getUsername());
        }

        if (validationUtil.isFieldValid(
                updatedUserUpdateDTO.getPhoneNumber(),
                validationUtil.BRAZILIAM_CELLPHONE_NUMBER_PATTERN)) {

            loggedUser.setPhoneNumber(updatedUserUpdateDTO.getPhoneNumber());
        }

        if (updatedUserUpdateDTO.getNickname() != null) {
            loggedUser.setNickname(updatedUserUpdateDTO.getNickname());
        }

        return repository.save(loggedUser);
    }
}
