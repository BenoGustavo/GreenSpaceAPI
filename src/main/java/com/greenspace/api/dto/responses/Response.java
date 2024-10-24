package com.greenspace.api.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Response<T> {
    private int status;
    private String message;
    private ResponseError error;
    private T data;
}
