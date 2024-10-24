package com.greenspace.api.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ResponseError {
    private int code;
    private String message;
    private String description;

    public ResponseError(int code, String message) {
        this.code = code;
        this.message = message;
        this.description = null;
    }
}
