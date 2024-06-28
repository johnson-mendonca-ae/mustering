package com.alert.mustering.ui.otp;

import static android.content.ContentValues.TAG;
import static com.alert.mustering.ui.login.LoginActivity.USER_DATA_STORAGE_KEY;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.alert.mustering.MainActivity;
import com.alert.mustering.constants.AppConstants;
import com.alert.mustering.databinding.ActivityOtpBinding;
import com.alert.mustering.model.LoginErrorResponse;
import com.alert.mustering.model.LoginResponse;
import com.alert.mustering.model.OtpPayload;
import com.alert.mustering.services.APIService;
import com.alert.mustering.ui.changePassword.ChangePasswordActivity;
import com.alert.mustering.ui.login.LoginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.alert.mustering.helper.ComponentUtil;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class OtpActivity extends AppCompatActivity {
    int countDownTime = 60000;
    CountDownTimer otpTimer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.alert.mustering.databinding.ActivityOtpBinding binding = ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        MaterialButton submitOtpBtn = binding.otpSubmitBtn;
        TextInputEditText otpText = binding.otpText;
        TextView resendBtn = binding.resendOtpBtn;
        ProgressBar loading = binding.loading;
        ImageButton backBtn = binding.backButton;
        TextView timer = binding.timer;
        SharedPreferences userCredentialPreferences = getSharedPreferences(USER_DATA_STORAGE_KEY, MODE_PRIVATE);

        resendBtn.setVisibility(View.GONE);
        otpTimer = new CountDownTimer(countDownTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                int minis = (int) ((millisUntilFinished / (1000 * 60)) % 60);
                timer.setText(MessageFormat.format("You can resend OTP in {0} : {1,number,00}", minis, seconds));
                timer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinish() {
                countDownTime = 60000;//
                loading.setVisibility(View.GONE);
                timer.setVisibility(View.GONE);
                resendBtn.setVisibility(View.VISIBLE);
                resendBtn.setEnabled(true);
                submitOtpBtn.setClickable(true);
            }
        };
        resendBtn.setOnClickListener(v -> {
            countDownTime = 60000;
            String serverUrl = userCredentialPreferences.getString(AppConstants.TEMP_STORAGE_ENV_URL, "");
            new APIService().resendOtp(getApplicationContext(), serverUrl).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> ComponentUtil.displayTopMessage(getApplicationContext(), "Error Resending OTP"));
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() == 401) {
                        SharedPreferences.Editor editor = userCredentialPreferences.edit();
                        editor.remove(AppConstants.TEMP_STORAGE_ENV_NAME);
                        editor.remove(AppConstants.TEMP_STORAGE_ENV_URL);
                        editor.putString(AppConstants.TEMP_STORAGE_ACCESS_TOKEN, "{}");
                        editor.putString(AppConstants.STORAGE_KEY_SKIP_LOGIN, "FALSE");
                        editor.putString(AppConstants.STORAGE_KEY_ALLOW_BIOMETRIC_LOGIN, "FALSE");
                        editor.apply();
                        startActivity(new Intent(OtpActivity.this, LoginActivity.class));
                        runOnUiThread(() -> {
                            otpTimer.cancel();
                            timer.setVisibility(View.GONE);
                            ComponentUtil.displayTopMessage(getApplicationContext(), "Authorization expired please login again");
                        });
                    } else if (response.isSuccessful()) {
                        runOnUiThread(() -> {
                            timer.setVisibility(View.VISIBLE);
                            ComponentUtil.displayTopMessage(getApplicationContext(), "Resending OTP");
                        });
                    }
                }
            });
            resendBtn.setEnabled(false);
            otpTimer.start();

        });
        otpTimer.start();
        submitOtpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateOTP();
                submitOtpBtn.setClickable(false);
                loading.setVisibility(View.VISIBLE);
            }

            private void validateOTP() {
                Boolean rememberMe = userCredentialPreferences.getBoolean(AppConstants.TEMP_STORAGE_REMEMBER_ME, false);
                OtpPayload otpPayload = new OtpPayload(Objects.requireNonNull(otpText.getText()).toString(), false, rememberMe);
                String serverUrl = userCredentialPreferences.getString(AppConstants.TEMP_STORAGE_ENV_URL, "");
                new APIService().validateOtp(otpPayload, getApplicationContext(), serverUrl).enqueue(
                        new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                Log.i(TAG, "onFailure:-> invalidate OTP");
                                runOnUiThread(() -> ComponentUtil.displayTopMessage(getApplicationContext(), "There has been a problem"));
                                loading.setVisibility(View.GONE);
                                submitOtpBtn.setClickable(true);
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                if (!response.isSuccessful()) {
                                    Log.i(TAG, "onResponse: " + response.code());
                                    if (response.code() == 401) { // Ques
                                        SharedPreferences.Editor editor = userCredentialPreferences.edit();
                                        editor.remove(AppConstants.TEMP_STORAGE_ENV_NAME);
                                        editor.remove(AppConstants.TEMP_STORAGE_ENV_URL);
                                        editor.putString(AppConstants.TEMP_STORAGE_ACCESS_TOKEN, "{}");
                                        editor.putString(AppConstants.STORAGE_KEY_SKIP_LOGIN, "FALSE");
                                        editor.putString(AppConstants.STORAGE_KEY_ALLOW_BIOMETRIC_LOGIN, "FALSE");
                                        editor.apply();
                                        startActivity(new Intent(OtpActivity.this, LoginActivity.class));

                                        runOnUiThread(() -> ComponentUtil.displayTopMessage(getApplicationContext(), "Authorization expired please login again"));
                                    } else {
                                        LoginErrorResponse otpErrorResponse = new Gson().
                                                fromJson(response.peekBody(Long.MAX_VALUE).charStream(), LoginErrorResponse.class);
                                        //TODO :: Toast Messages position Top API>30 does not allow setGravity()
                                        // https://developer.android.com/reference/android/widget/Toast#setGravity(int,%20int,%20int)
                                        runOnUiThread(() -> {
                                            otpText.setText("");
                                            //resendBtn.setEnabled(true);
                                            ComponentUtil.displayTopMessage(getApplicationContext(), otpErrorResponse.getMessages()[0].getMessageDisplayText());
                                            loading.setVisibility(View.GONE);
                                            submitOtpBtn.setClickable(true);
                                        });
                                    }
                                } else {
                                    LoginResponse loginResponse = new Gson().fromJson(response.peekBody(Long.MAX_VALUE).charStream(), LoginResponse.class);
                                    SharedPreferences.Editor credsEditor = userCredentialPreferences.edit();
                                    credsEditor.putString(AppConstants.STORAGE_KEY_TOKEN_INFO, new Gson().toJson(loginResponse));
                                    String storedTempEnvUrl = userCredentialPreferences.getString(AppConstants.TEMP_STORAGE_ENV_URL, "null");
                                    String storedTempEnvName = userCredentialPreferences.getString(AppConstants.TEMP_STORAGE_ENV_NAME, "null");
                                    credsEditor.putString(AppConstants.STORAGE_KEY_ENV_NAME, storedTempEnvName);
                                    credsEditor.putString(AppConstants.STORAGE_KEY_ENV_URL, storedTempEnvUrl);
                                    long expiry_datetime_millis = Instant.now().plus(Duration.ofSeconds(loginResponse.getExpires_in() - 60)).toEpochMilli();
                                    credsEditor.putLong(AppConstants.STORAGE_KEY_TOKEN_EXPIRY_TIME, expiry_datetime_millis);
                                    if (rememberMe) {
                                        credsEditor.putString(AppConstants.STORAGE_KEY_ALLOW_BIOMETRIC_LOGIN, "TRUE");
                                        credsEditor.putString(AppConstants.STORAGE_KEY_SKIP_LOGIN, "TRUE");
                                    } else {
                                        credsEditor.putString(AppConstants.STORAGE_KEY_ALLOW_BIOMETRIC_LOGIN, "FALSE");
                                        credsEditor.putString(AppConstants.STORAGE_KEY_SKIP_LOGIN, "FALSE");
                                    }
                                    credsEditor.apply();

                                    Handler toastHandler = new Handler(Looper.getMainLooper());
                                    toastHandler.post(() -> {
                                        ComponentUtil.displayTopMessage(getApplicationContext(), "OTP Verified");
                                        if(getIntent().getStringExtra(AppConstants.RESET_PASSWORD_FLOW) !=null &&
                                                "ResetPassword".equals(getIntent().getStringExtra(AppConstants.RESET_PASSWORD_FLOW))){
                                            Log.i(TAG, "onResponse: " + response.code());
                                            startActivity(new Intent(OtpActivity.this, ChangePasswordActivity.class));
                                        }else{
                                            startActivity(new Intent(OtpActivity.this, MainActivity.class));
                                        }
                                    });
                                   /* runOnUiThread(() -> {
                                        ComponentUtil.displayTopMessage(getApplicationContext(), "OTP Verified");
                                        //loading.setVisibility(View.GONE);
                                    });
                                    */
                                    APIService.serverUrl = null;
                                }
                            }
                        }
                );
            }
        });

        backBtn.setOnClickListener(v -> {
            SharedPreferences.Editor editor = userCredentialPreferences.edit();
            //NOTE :  once user come to OTP screen and go back all the data will be erased / including previously stored data
            editor.remove(AppConstants.TEMP_STORAGE_ENV_NAME);
            editor.remove(AppConstants.TEMP_STORAGE_ENV_URL);
            editor.putString(AppConstants.TEMP_STORAGE_ACCESS_TOKEN, "{}");
            editor.putString(AppConstants.STORAGE_KEY_SKIP_LOGIN, "FALSE");
            editor.putString(AppConstants.STORAGE_KEY_ALLOW_BIOMETRIC_LOGIN, "FALSE");
            editor.apply();
            startActivity(new Intent(OtpActivity.this, LoginActivity.class));
        });
    }

    void cancelOtpTimer() {
        if (otpTimer != null) {
            otpTimer.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelOtpTimer();
    }
}