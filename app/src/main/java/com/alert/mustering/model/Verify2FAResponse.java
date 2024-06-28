package com.alert.mustering.model;

import androidx.annotation.NonNull;

public class Verify2FAResponse {
    private Boolean success;
    private Integer numberOfElements;
    private LoginResponse data;
    public Integer getNumberOfElements() {
        return numberOfElements;
    }
    public void setNumberOfElements(Integer numberOfElements) {
        this.numberOfElements = numberOfElements;
    }
    public LoginResponse getData() {
        return data;
    }
    public void setData(LoginResponse data) {
        this.data = data;
    }
    public Boolean getSuccess() {
        return success;
    }
    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
