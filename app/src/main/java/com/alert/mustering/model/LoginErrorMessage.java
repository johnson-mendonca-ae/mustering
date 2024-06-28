package com.alert.mustering.model;

import androidx.annotation.NonNull;

public class LoginErrorMessage {
    String messageText;
    String messageType;
    String messageCode;
    String messageDisplayText;
    Integer priority;

    public String getMessageDisplayText() {
        return messageDisplayText;
    }

    @NonNull
    @Override
    public String toString() {
        return "LoginErrorMessage{" +
                "messageText='" + messageText + '\'' +
                ", messageType='" + messageType + '\'' +
                ", messageCode='" + messageCode + '\'' +
                ", messageDisplayText=" + messageDisplayText +
                ", priority=" + priority +
                '}';
    }
}
