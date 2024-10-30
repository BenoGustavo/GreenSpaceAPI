package com.greenspace.api.error.http;

public class Conflict409Exception extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private static final int ERROR_CODE = 409;

    public Conflict409Exception(String message) {
        super(message);
    }

    public int getCode() {
        return ERROR_CODE;
    }
}
