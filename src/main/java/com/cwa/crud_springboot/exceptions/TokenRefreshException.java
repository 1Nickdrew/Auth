package com.cwa.crud_springboot.exceptions;

public class TokenRefreshException extends RuntimeException {

    private String token;

    public TokenRefreshException(String token, String message) {
        super(message);
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}

