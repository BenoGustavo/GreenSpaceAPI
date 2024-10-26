package com.greenspace.api.error.http;

public class Forbidden403Exception extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private static final int ERROR_CODE = 403;

    public Forbidden403Exception(String message) {
        super(message);
    }

    public int getCode() {
        return ERROR_CODE;
    }
}
