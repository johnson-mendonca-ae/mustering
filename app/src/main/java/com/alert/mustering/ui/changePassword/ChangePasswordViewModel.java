package com.alert.mustering.ui.changePassword;


import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ChangePasswordViewModel extends ViewModel {
    //static final String TAG = ChangePasswordActivity.class.getSimpleName();
    private final MutableLiveData<Boolean> checkLengthChar = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> checkNumericChar = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> checkSpecialChar = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> checkUpperChar = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> checkConfirmPassword = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isDataValid = new MutableLiveData<>();

    public void validatePassword(String input) {
        checkLengthChar.postValue(input.length() >= 8);
        checkNumericChar.setValue(input.matches(".*\\d.*"));
        checkSpecialChar.setValue(input.matches(".*" + "[!@#$%^&*()_+{}\\[\\]:;<>,.?~`]" + ".*"));
        checkUpperChar.setValue(input.matches(".*[A-Z].*"));
        isDataValid.setValue(checkIsDataValid());
    }

    public void validateConfirmPassword(String newPassword, String confirmPassword) {
        checkConfirmPassword.setValue(confirmPassword.equals(newPassword));
        isDataValid.postValue(checkIsDataValid());
    }

    private Boolean checkIsDataValid() {
        //Log.i(TAG, MessageFormat.format("checkIsDataValid: {0} {1} {2} {3} {4}", checkLengthChar.getValue(), checkNumericChar.getValue(), checkUpperChar.getValue(), checkSpecialChar.getValue(), checkConfirmPassword.getValue()));
        return Boolean.TRUE.equals(checkLengthChar.getValue()) && Boolean.TRUE.equals(checkNumericChar.getValue())
                && Boolean.TRUE.equals(checkUpperChar.getValue()) &&
                Boolean.TRUE.equals(checkSpecialChar.getValue()) &&
                Boolean.TRUE.equals(checkConfirmPassword.getValue());
    }

    public MutableLiveData<Boolean> getCheckLengthChar() {
        return checkLengthChar;
    }

    public MutableLiveData<Boolean> getCheckNumericChar() {
        return checkNumericChar;
    }

    public MutableLiveData<Boolean> getCheckSpecialChar() {
        return checkSpecialChar;
    }

    public MutableLiveData<Boolean> getCheckUpperChar() {
        return checkUpperChar;
    }

    public MutableLiveData<Boolean> getIsDataValid() {
        return isDataValid;
    }
}
