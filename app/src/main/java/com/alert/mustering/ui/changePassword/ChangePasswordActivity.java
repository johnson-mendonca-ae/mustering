package com.alert.mustering.ui.changePassword;

import static com.alert.mustering.ui.login.LoginActivity.USER_DATA_STORAGE_KEY;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.lifecycle.ViewModelProvider;

import com.alert.mustering.R;
import com.alert.mustering.constants.AppConstants;
import com.alert.mustering.databinding.ActivityChangePasswordBinding;
import com.alert.mustering.model.ChangePasswordPayload;
import com.alert.mustering.services.APIService;
import com.alert.mustering.ui.login.LoginActivity;
import com.alert.mustering.ui.otp.OtpActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.alert.mustering.helper.ComponentUtil;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    static final String TAG = ChangePasswordActivity.class.getSimpleName();
    private TextInputEditText newPassword, confirmPassword;
    private com.alert.mustering.ui.changePassword.ChangePasswordViewModel passwordViewModel;
    TextWatcher newPasswordTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String newPasswordInput = null != newPassword.getText() ? newPassword.getText().toString() : null;
            passwordViewModel.validatePassword(Objects.requireNonNull(newPasswordInput));
            passwordViewModel.validateConfirmPassword(newPassword.getText().toString(),
                    Objects.requireNonNull(confirmPassword.getText()).toString());
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
    TextWatcher confirmPasswordTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            passwordViewModel.validateConfirmPassword(Objects.requireNonNull(newPassword.getText()).toString(),
                    Objects.requireNonNull(confirmPassword.getText()).toString());
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.alert.mustering.databinding.ActivityChangePasswordBinding binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        passwordViewModel = new ViewModelProvider(this).get(com.alert.mustering.ui.changePassword.ChangePasswordViewModel.class);
        setContentView(binding.getRoot());
        // after validations , enable submit button
        newPassword = binding.newPasswordEdit;
        confirmPassword = binding.confirmPasswordEdit;
        MaterialButton submitButton = binding.changePwdBtn;
        newPassword.addTextChangedListener(newPasswordTextWatcher);
        confirmPassword.addTextChangedListener(confirmPasswordTextWatcher);
        ImageButton backBtn = binding.backButton;

        passwordViewModel.getIsDataValid().observe(this, check -> {
            Log.i(TAG, "isDataValid -> " + check);
            binding.changePwdBtn.setEnabled(check);
        });
        passwordViewModel.getCheckLengthChar().observe(this, check ->
                changeToCheck(check, binding.lengthCheckImg));

        passwordViewModel.getCheckNumericChar().observe(this, check ->
                changeToCheck(check, binding.numericCheckImg));

        passwordViewModel.getCheckSpecialChar().observe(this, check ->
                changeToCheck(check, binding.specialCheckImg));

        passwordViewModel.getCheckUpperChar().observe(this, check ->
                changeToCheck(check, binding.upperCheckImg));

        submitButton.setOnClickListener(v -> {
            runOnUiThread(() -> {
                binding.progressBar.setVisibility(View.VISIBLE);
                submitButton.setClickable(false);
            });
            SharedPreferences userCredentialPreferences = getSharedPreferences(USER_DATA_STORAGE_KEY, MODE_PRIVATE);
            new APIService().changePassword(new ChangePasswordPayload(Objects.requireNonNull(newPassword.getText()).toString()), getApplicationContext()).enqueue(
                    new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            runOnUiThread(() -> {
                                submitButton.setClickable(true);
                                ComponentUtil.displayTopMessage(getApplicationContext(), "Error: Something went wrong");
                                binding.progressBar.setVisibility(View.GONE);
                            });
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) {
                            if (response.isSuccessful()) {
                                runOnUiThread(() -> {
                                    submitButton.setClickable(true);
                                    ComponentUtil.displayTopMessage(getApplicationContext(), "Please login with new Password");
                                    binding.progressBar.setVisibility(View.GONE);
                                });
                                SharedPreferences.Editor editor = userCredentialPreferences.edit();
                                editor.remove(AppConstants.STORAGE_KEY_TOKEN_INFO);
                                editor.putString(AppConstants.STORAGE_KEY_SKIP_LOGIN, "FALSE");
                                editor.putString(AppConstants.STORAGE_KEY_ALLOW_BIOMETRIC_LOGIN, "FALSE");
                                editor.apply();
                                startActivity(new Intent(ChangePasswordActivity.this, LoginActivity.class));
                            } else {
                                runOnUiThread(() -> {
                                    submitButton.setClickable(true);
                                    binding.progressBar.setVisibility(View.GONE);
                                    ComponentUtil.displayTopMessage(getApplicationContext(), "Error ! Changing credentials");
                                });
                            }
                        }
                    }
            );
        });

        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ChangePasswordActivity.this, OtpActivity.class);
            intent.putExtra(AppConstants.RESET_PASSWORD_FLOW,AppConstants.RESET_PASSWORD_FLOW);
            startActivity(intent);
        });
    }

    public void changeToCheck(Boolean check, ImageView img) {
        if (check) {
            img.setImageDrawable(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.baseline_task_alt_24));
        } else {
            img.setImageDrawable(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.baseline_cancel_24));
        }
    }

}