package com.alert.mustering.ui.login;

import androidx.annotation.Nullable;

/**
 * Data validation state of the login form.
 */
class LoginFormState {
    @Nullable
    private Integer usernameError;
    @Nullable
    private Integer passwordError;
    @Nullable
    private Integer tenantError;
    private boolean isDataValid;

    LoginFormState() {
    }

    @Nullable
    public Integer getTenantError() {
        return tenantError;
    }

    public void setTenantError(@Nullable Integer tenantError) {
        this.tenantError = tenantError;
    }

    @Nullable
    Integer getUsernameError() {
        return usernameError;
    }

    public void setUsernameError(@Nullable Integer usernameError) {
        this.usernameError = usernameError;
    }

    @Nullable
    Integer getPasswordError() {
        return passwordError;
    }

    public void setPasswordError(@Nullable Integer passwordError) {
        this.passwordError = passwordError;
    }

    boolean isDataValid() {
        return isDataValid;
    }

    public void setDataValid(boolean dataValid) {
        isDataValid = dataValid;
    }
}