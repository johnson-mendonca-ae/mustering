package com.alert.mustering.model;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class LoginErrorResponse {
    final Boolean success = true;
    LoginErrorMessage[] messages;

    public LoginErrorMessage[] getMessages() {
        return messages;
    }

    @NonNull
    @Override
    public String toString() {
        return "LoginErrorResponse{" +
                "success=" + success +
                ", messages=" + Arrays.toString(messages) +
                '}';
    }
}
