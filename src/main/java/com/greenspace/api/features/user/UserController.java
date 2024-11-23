package com.greenspace.api.features.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenspace.api.dto.responses.Response;
import com.greenspace.api.models.UserModel;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/get-logged-user")
    public ResponseEntity<Response<Object>> getLoggedUser() {
        UserModel loggedUser = userService.getLoggedUser();

        Response<Object> response = Response.builder()
                .message("Success")
                .status(200)
                .data(loggedUser)
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/update-logged-user-username")
    public ResponseEntity<Response<Object>> updateLoggedUserUsername(String newPassword) {
        UserModel updatedUser = userService.updateLoggedUserUsername(newPassword);

        Response<Object> response = Response.builder()
                .message("Username updated successfully")
                .status(200)
                .data(updatedUser)
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/update-logged-user-phone-number")
    public ResponseEntity<Response<Object>> updateLoggedUserPhoneNumber(String newPhoneNumber) {
        UserModel updatedUser = userService.updateLoggedUserPhoneNumber(newPhoneNumber);

        Response<Object> response = Response.builder()
                .message("Phone number updated successfully")
                .status(200)
                .data(updatedUser)
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/update-logged-user-nickname")
    public ResponseEntity<Response<Object>> updateLoggedUserNickname(String newNickname) {
        UserModel updatedUser = userService.updateLoggedUserNickname(newNickname);

        Response<Object> response = Response.builder()
                .message("Nickname updated successfully")
                .status(200)
                .data(updatedUser)
                .build();

        return ResponseEntity.ok(response);
    }
}
