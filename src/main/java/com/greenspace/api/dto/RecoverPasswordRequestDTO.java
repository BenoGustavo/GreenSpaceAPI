package com.greenspace.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RecoverPasswordRequestDTO {
    private String password;
    private String confirmPassword;
}