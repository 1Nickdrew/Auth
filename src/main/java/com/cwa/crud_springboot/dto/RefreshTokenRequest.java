package com.cwa.crud_springboot.dto;

import jakarta.validation.constraints.NotBlank;

public class RefreshTokenRequest {
    @NotBlank
    private String refreshToken;

    // Getters et setters

    public @NotBlank String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(@NotBlank String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
