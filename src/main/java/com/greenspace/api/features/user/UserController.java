package com.greenspace.api.features.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.greenspace.api.dto.auth.LoginDTO;
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

    // @PatchMapping("/update-logged-user-password") //Por algum motivo swagger não
    // reconhece o patchmapping como multipartfile
    @RequestMapping(value = "/update-logged-user-profile-picture", method = RequestMethod.PATCH, consumes = "multipart/form-data")
    public ResponseEntity<Response<Object>> updateLoggedUserProfilePicture(@RequestParam("file") MultipartFile file) {
        UserModel updatedUser = userService.updateLoggedUserProfilePicture(file);

        Response<Object> response = Response.builder()
                .message("Profile picture updated successfully")
                .status(200)
                .data(updatedUser)
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/update-logged-user-username")
    public ResponseEntity<Response<Object>> updateLoggedUserUsername(@RequestParam String newUsername) {
        UserModel updatedUser = userService.updateLoggedUserUsername(newUsername);

        Response<Object> response = Response.builder()
                .message("Username updated successfully")
                .status(200)
                .data(updatedUser)
                .build();

        return ResponseEntity.ok(response);
    }

    // ESSE REGEX NÃO ESTÁ FUNCIONANDO DIREITO
    @PatchMapping("/update-logged-user-phone-number")
    public ResponseEntity<Response<Object>> updateLoggedUserPhoneNumber(@RequestParam String newPhoneNumber) {
        System.out.println("O numero novo do usuario é: " + newPhoneNumber);
        UserModel updatedUser = userService.updateLoggedUserPhoneNumber(newPhoneNumber);

        Response<Object> response = Response.builder()
                .message("Phone number updated successfully")
                .status(200)
                .data(updatedUser)
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/update-logged-user-nickname")
    public ResponseEntity<Response<Object>> updateLoggedUserNickname(@RequestParam String newNickname) {
        UserModel updatedUser = userService.updateLoggedUserNickname(newNickname);

        Response<Object> response = Response.builder()
                .message("Nickname updated successfully")
                .status(200)
                .data(updatedUser)
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/deactivate-logged-user")
    public ResponseEntity<Response<Object>> deactivateLoggedUser(@RequestBody LoginDTO userCredentials) {
        UserModel deactivatedUser = userService.deactivateLoggedUser(userCredentials);

        Response<Object> response = Response.builder()
                .message("User deactivated successfully!")
                .status(200)
                .data(deactivatedUser)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-account-reactivation-token")
    public ResponseEntity<Response<Object>> sendAccountActivationToken(@RequestParam String accountEmail) {
        userService.sendAccountActivationToken(accountEmail);

        Response<Object> response = Response.builder()
                .message("Account reactivation token has been sent to your email successfully!")
                .status(201)
                .data("Check your email address!")
                .build();

        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/reactivate-account")
    public ResponseEntity<Response<Object>> reactivateAccount(@RequestParam String token) {

        UserModel reactivatedUser = userService.reactivateAccount(token);

        Response<Object> response = Response.builder()
                .message("Success")
                .status(200)
                .data(reactivatedUser)
                .build();

        return ResponseEntity.ok(response);
    }

}
