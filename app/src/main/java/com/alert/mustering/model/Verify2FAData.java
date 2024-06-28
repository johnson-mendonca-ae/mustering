package com.alert.mustering.model;

import androidx.annotation.NonNull;

public class Verify2FAData {
    String access_token;
    String token_type;
    Boolean require2FA = false;
    public String getAccess_token() {
        return access_token;
    }

    public String getToken_type() {
        return token_type;
    }

    public Boolean getRequire2fa() {
        return require2FA;
    }

    @NonNull
    @Override
    public String toString() {
        return "Verify2FAData{" +
                "  access_token='" + access_token + '\'' +
                ", token_type='" + token_type + '\'' +
                ", require2FA=" + require2FA +
                '}';
    }
}
