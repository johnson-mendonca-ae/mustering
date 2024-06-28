package com.alert.mustering.model;

public class LoginRequest {
    String username;
    String password;
    Boolean rememberMe;

    public LoginRequest(String username, String password, Boolean rememberMe) {
        this.username = username != null ? username.trim() : username;
        this.password = password;
        this.rememberMe = rememberMe;
    }
}
