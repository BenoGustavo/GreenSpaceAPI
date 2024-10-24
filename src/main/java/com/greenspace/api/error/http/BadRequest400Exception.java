package com.greenspace.api.error.http;

public class BadRequest400Exception extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private static final int ERROR_CODE = 400;

    public BadRequest400Exception(String message) {
        super(message);
    }

    public int getCode() {
        return ERROR_CODE;
    }
}
