package com.alert.mustering.ui.forgotPassword;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ForgotPasswordViewModel extends ViewModel {
    private final MutableLiveData<Boolean> checkLengthChar = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isDataValid = new MutableLiveData<>();

    public void validateUserName(String input) {
        checkLengthChar.postValue(input.length() >= 1);
        isDataValid.setValue(checkIsDataValid());
    }
    private Boolean checkIsDataValid() {
        return Boolean.TRUE.equals(checkLengthChar.getValue());
    }

    public MutableLiveData<Boolean> getCheckLengthChar() {
        return checkLengthChar;
    }

    public MutableLiveData<Boolean> getIsDataValid() {
        return isDataValid;
    }
}
