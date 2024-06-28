package com.alert.mustering.ui.forgotPassword;

import static com.alert.mustering.ui.login.LoginActivity.USER_DATA_STORAGE_KEY;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.alert.mustering.helper.ComponentUtil;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.alert.mustering.constants.AppConstants;
import com.alert.mustering.databinding.ForgotPasswordBinding;
import com.alert.mustering.model.ForgotPasswordPayload;
import com.alert.mustering.model.Verify2FAResponse;
import com.alert.mustering.services.APIService;
import com.alert.mustering.ui.login.LoginActivity;
import com.alert.mustering.ui.otp.OtpActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    @Override
    public boolean onSupportNavigateUp() {
        return super.onSupportNavigateUp();
    }

    static final String TAG = com.alert.mustering.ui.forgotPassword.ForgotPasswordActivity.class.getSimpleName();
    private TextInputEditText userId;
    private String environmentURL;
    private ForgotPasswordViewModel viewModel;
    TextWatcher userIdTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String userIdInput = null != userId.getText() ? userId.getText().toString().trim() : null;
            viewModel.validateUserName(Objects.requireNonNull(userIdInput));
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate ------> START ");
        ForgotPasswordBinding binding = ForgotPasswordBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(ForgotPasswordViewModel.class);
        setContentView(binding.getRoot());
        environmentURL = getIntent().getStringExtra(AppConstants.STORAGE_KEY_ENV_URL);
        Log.d(TAG, "onCreate ------> environmentURL"+environmentURL);
        userId = binding.useridText;
        MaterialButton submitButton = binding.forgotpwdSubmitBtn;
        userId.addTextChangedListener(userIdTextWatcher);

        ImageButton backBtn = binding.backButton;

        viewModel.getIsDataValid().observe(this, check -> {
            Log.d(TAG, "isDataValid -> " + check);
            submitButton.setEnabled(check);
        });

        submitButton.setOnClickListener(view -> {
            Log.i(TAG, "submitButton clicked ------> ");
            binding.progressBar.setVisibility(View.VISIBLE);
            submitButton.setClickable(false);
            Log.d(TAG, "submitButton clicked ------> environmentURL"+environmentURL);
            validateAction(submitButton, binding.progressBar, environmentURL);
        });

        backBtn.setOnClickListener(v -> {
            SharedPreferences userCredentialPreferences = getSharedPreferences(USER_DATA_STORAGE_KEY, MODE_PRIVATE);
            SharedPreferences.Editor editor = userCredentialPreferences.edit();
            //NOTE :  once user come to OTP screen and go back all the data will be erased / including previously stored data
            editor.remove(AppConstants.TEMP_STORAGE_ENV_NAME);
            editor.remove(AppConstants.TEMP_STORAGE_ENV_URL);
            editor.putString(AppConstants.TEMP_STORAGE_ACCESS_TOKEN, "{}");
            editor.putString(AppConstants.STORAGE_KEY_SKIP_LOGIN, "FALSE");
            editor.putString(AppConstants.STORAGE_KEY_ALLOW_BIOMETRIC_LOGIN, "FALSE");
            editor.apply();
            startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
        });
    }

    private void validateAction(MaterialButton submitButton, ProgressBar progressBar, String serverUrl) {

        new APIService().verify2FA(new ForgotPasswordPayload(Objects.requireNonNull(userId.getText()).toString()), getApplicationContext(), serverUrl).enqueue(
                new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        runOnUiThread(() -> {
                            submitButton.setClickable(true);
                            ComponentUtil.displayTopMessage(getApplicationContext(),"ERROR ! Something went wrong !" );
                            progressBar.setVisibility(View.GONE);
                        });
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "verify2FA() : onResponse  ==> SUCCESS");
                            String responseString = Objects.requireNonNull(response.body()).string();
                            Verify2FAResponse verify2FAResponse = new Gson().fromJson(responseString, Verify2FAResponse.class);
                            if (verify2FAResponse.getData().getRequire2fa() != null && verify2FAResponse.getData().getRequire2fa()) {
                                Log.d(TAG, "verify2FA() : onResponse  ==> requires 2FA...");
                                SharedPreferences userCredentialPreferences = getSharedPreferences(USER_DATA_STORAGE_KEY, MODE_PRIVATE);
                                SharedPreferences.Editor credsEditor = userCredentialPreferences.edit();
                                credsEditor.putString(AppConstants.TEMP_STORAGE_ACCESS_TOKEN, new Gson().toJson(verify2FAResponse.getData()));
                                credsEditor.putString(AppConstants.TEMP_STORAGE_ENV_URL, serverUrl);
                                credsEditor.apply();
                                Log.d(TAG, "verify2FA() : onResponse  ==> SUCCESS...serverUrl = "+serverUrl);

                                runOnUiThread(() -> {
                                    new APIService().sendOtp(getApplicationContext(), serverUrl).enqueue(new Callback() {
                                        @Override
                                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                            Log.e(TAG, "sendOtp onFailure() : onResponse  ==> FAILED"  + e);
                                            runOnUiThread(() -> ComponentUtil.displayTopMessage(getApplicationContext(),"Error Sending OTP - retry again" ));
                                        }

                                        @Override
                                        public void onResponse(@NonNull Call call, @NonNull Response response) {
                                            Log.d(TAG, "sendOtp onResponse() : onResponse  ==> ");
                                            if (response.code() == 401) {
                                                Log.w(TAG, "sendOtp onResponse() : onResponse (401)  ==> ");
                                                SharedPreferences userCredentialPreferences = getSharedPreferences(USER_DATA_STORAGE_KEY, MODE_PRIVATE);
                                                SharedPreferences.Editor editor = userCredentialPreferences.edit();
                                                editor.remove(AppConstants.TEMP_STORAGE_ENV_NAME);
                                                editor.remove(AppConstants.TEMP_STORAGE_ENV_URL);
                                                editor.putString(AppConstants.TEMP_STORAGE_ACCESS_TOKEN, "{}");
                                                editor.putString(AppConstants.STORAGE_KEY_SKIP_LOGIN, "FALSE");
                                                editor.putString(AppConstants.STORAGE_KEY_ALLOW_BIOMETRIC_LOGIN, "FALSE");
                                                editor.apply();
                                                startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                                                runOnUiThread(() -> {
                                                    ComponentUtil.displayTopMessage(getApplicationContext(),"Authorization expired please login again" );
                                                });
                                            } else if (response.isSuccessful()) {
                                                Log.d(TAG, "sendOtp onResponse() : onResponse SUCCESSFUL ==> ");
                                                runOnUiThread(() -> {
                                                    ComponentUtil.displayTopMessage(getApplicationContext(),"Sent OTP successfully" );
                                                    Intent intent = new Intent(ForgotPasswordActivity.this, OtpActivity.class);
                                                    intent.putExtra(AppConstants.RESET_PASSWORD_FLOW,AppConstants.RESET_PASSWORD_FLOW);
                                                    startActivity(intent);
                                                    //startActivity(new Intent(ForgotPasswordActivity.this, OtpActivity.class));
                                                    finish();
                                                });
                                            }
                                        }
                                    });
                                });
                            }else{
                                //Cannot reset password since 2FA is not setup - else anyone can keep resetting other users passwords
                                ComponentUtil.displayTopMessage(getApplicationContext(),"Cannot reset password since 2FA is not setup for the user" );
                                Log.w(TAG, "verify2FA() : Cannot reset password since 2FA is not setup for the user.");
                                submitButton.setClickable(true);
                                progressBar.setVisibility(View.GONE);
                            }
                            //finish();
                        } else {
                            runOnUiThread(() -> {
                                submitButton.setClickable(true);
                                progressBar.setVisibility(View.GONE);
                                ComponentUtil.displayTopMessage(getApplicationContext(),"Error ! Unable to Verify User to reset password" );
                            });
                        }
                    }
                }
        );
    }
}