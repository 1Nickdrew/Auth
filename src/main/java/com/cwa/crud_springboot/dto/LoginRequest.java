package com.cwa.crud_springboot.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for login request, containing username and password.
 */
public class LoginRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    // Getters et Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
