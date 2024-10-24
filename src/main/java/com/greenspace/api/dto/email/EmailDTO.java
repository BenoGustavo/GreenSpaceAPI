package com.greenspace.api.dto.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class EmailDTO {
    private String userEmail;
    private String subject;
    private String message;
}
