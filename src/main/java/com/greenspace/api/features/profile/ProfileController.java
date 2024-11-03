package com.greenspace.api.features.profile;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenspace.api.dto.profile.ProfileDTO;
import com.greenspace.api.dto.responses.Response;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<Response<Object>> update(@PathVariable UUID id, @RequestBody ProfileDTO profile) {
        Response<Object> response = Response.builder()
                .data(profileService.update(id, profile))
                .build();

        return ResponseEntity.ok(response);
    }

    // DEVE SER DELETADO NO MEMENTO EM QUE O USUARIO DESATIVAR SUA CONTA
    // @DeleteMapping("/delete/{id}")
    // public ResponseEntity<Response<Object>> delete(@PathVariable UUID id) {
    // Response<Object> response = Response.builder()
    // .data(profileService.delete(id))
    // .build();

    // return ResponseEntity.ok(response);
    // }
}
