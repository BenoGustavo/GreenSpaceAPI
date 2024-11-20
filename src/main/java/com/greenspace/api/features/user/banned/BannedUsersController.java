package com.greenspace.api.features.user.banned;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenspace.api.dto.banned.BanUsersDTO;
import com.greenspace.api.dto.responses.Response;

@RestController
@RequestMapping("/admin/api/banned-users")
public class BannedUsersController {
    private final BannedUsersService bannedUsersService;

    public BannedUsersController(BannedUsersService bannedUsersService) {
        this.bannedUsersService = bannedUsersService;
    }

    @PostMapping("/ban")
    public ResponseEntity<Response<Object>> banUser(@RequestBody BanUsersDTO banUsersDTO) {
        bannedUsersService.banUser(banUsersDTO);

        Response<Object> response = Response.builder()
                .message("User banned successfully!")
                .status(204)
                .build();

        return ResponseEntity.status(204).body(response);
    }

    @DeleteMapping("/unban/{userEmailAddress}")
    public ResponseEntity<Response<Object>> unbanUser(@PathVariable String userEmailAddress) {
        bannedUsersService.unbanUser(userEmailAddress);

        Response<Object> response = Response.builder()
                .message("User unbanned successfully!")
                .status(204)
                .build();

        return ResponseEntity.status(204).body(response);
    }
}
