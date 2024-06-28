package com.alert.mustering.model;

public class OtpPayload {
    String totp;
    Boolean appLogin;
    Boolean rememberMe;

    public OtpPayload(String totp, Boolean appLogin, Boolean rememberMe) {
        this.totp = totp;
        this.appLogin = appLogin;
        this.rememberMe = rememberMe;
    }
}
