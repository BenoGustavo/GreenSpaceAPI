package com.greenspace.api.features.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

}
