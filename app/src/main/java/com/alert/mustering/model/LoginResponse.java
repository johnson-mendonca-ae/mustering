package com.alert.mustering.model;

import androidx.annotation.NonNull;

public class LoginResponse {
    String access_token;
    String refresh_token;
    String token_type;
    String tenantName;
    Long expires_in;
    Long expiryDate;
    Boolean require2FA = false;

    public String getAccess_token() {
        return access_token;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public String getToken_type() {
        return token_type;
    }

    public Long getExpires_in() {
        return expires_in;
    }

    public String getTenantName() {
        return tenantName;
    }

    public Boolean getRequire2fa() {
        return require2FA;
    }

    @NonNull
    @Override
    public String toString() {
        return "LoginResponse{" +
                "  access_token='" + access_token + '\'' +
                "  expiryDate='" + expiryDate + '\'' +
                ", refresh_token='" + refresh_token + '\'' +
                ", token_type='" + token_type + '\'' +
                ", expired_in=" + expires_in +
                ", require2FA=" + require2FA +
                ", tenantName=" + tenantName +
                '}';
    }
}
