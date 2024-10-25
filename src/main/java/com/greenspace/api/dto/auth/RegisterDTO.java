package com.greenspace.api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class RegisterDTO {
    private String username;
    private String nickname;
    private String email;
    private String password;
    private String confirmPassword;
}