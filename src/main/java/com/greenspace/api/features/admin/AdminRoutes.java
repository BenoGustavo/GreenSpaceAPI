package com.greenspace.api.features.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.greenspace.api.dto.banned.BanRevokeDTO;
import com.greenspace.api.dto.banned.BanUsersDTO;
import com.greenspace.api.dto.responses.Response;
import com.greenspace.api.features.imagesManager.UserImagesService;
import com.greenspace.api.features.user.banned.BannedUsersService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/admin/api/")
@Tag(name = "Admin", description = "Endpoints para administradores do sistema!")
public class AdminRoutes {
    private final UserImagesService userImagesService;
    private final BannedUsersService bannedUsersService;

    public AdminRoutes(
            UserImagesService userImagesService,
            BannedUsersService bannedUsersService) {
        this.userImagesService = userImagesService;
        this.bannedUsersService = bannedUsersService;
    }

    @PutMapping(value = "images/register-new-default-profile-picture", consumes = "multipart/form-data")
    @Operation(summary = "Registre uma nova foto de perfil padrão para a plataforma! Essa foto será automaticamente inserida no perfil de novos usuarios!", tags = {
            "Admin", "Images" })
    public ResponseEntity<Response<Object>> registerDefaultProfilePicture(@RequestParam("file") MultipartFile file) {
        String profilePictureUrl = userImagesService.registerDefaultProfilePicture(file);

        Response<Object> response = Response.builder()
                .message("Default profile picture registered successfully")
                .status(200)
                .data("Default profile picture bean registered with URL: " + profilePictureUrl)
                .build();

        return ResponseEntity.status(200).body(response);
    }

    @PostMapping("banned-user/ban")
    @Operation(summary = "Realize o banimento de um usuario", tags = { "Admin", "Banned Users" })
    public ResponseEntity<Response<Object>> banUser(@RequestBody BanUsersDTO banUsersDTO) {
        bannedUsersService.banUser(banUsersDTO);

        Response<Object> response = Response.builder()
                .message("User banned successfully!")
                .status(204)
                .build();

        return ResponseEntity.status(204).body(response);
    }

    @DeleteMapping("banned-user/unban")
    @Operation(summary = "Realize o desbanimento de um usuario", tags = { "Admin", "Banned Users" })
    public ResponseEntity<Response<Object>> unbanUser(@RequestBody BanRevokeDTO revokeDTO) {
        bannedUsersService.unbanUser(revokeDTO);

        Response<Object> response = Response.builder()
                .message("User unbanned successfully!")
                .status(204)
                .build();

        return ResponseEntity.status(204).body(response);
    }
}
