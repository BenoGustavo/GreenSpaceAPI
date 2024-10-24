package com.greenspace.api.error.http;

public class Unauthorized401Exception extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private static final int ERROR_CODE = 401;

    public Unauthorized401Exception(String message) {
        super(message);
    }

    public int getCode() {
        return ERROR_CODE;
    }

}
