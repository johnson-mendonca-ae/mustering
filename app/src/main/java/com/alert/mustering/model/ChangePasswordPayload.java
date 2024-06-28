package com.alert.mustering.model;

public class ChangePasswordPayload {
    String password;
    String newPassword;
    String confirmPassword;
    Boolean skip;

    public ChangePasswordPayload(String newPassword) {
        this.newPassword = newPassword;
        this.confirmPassword = newPassword;
        this.skip = true;
        this.password = "";
    }
}
