package com.alert.mustering.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.alert.mustering.MainActivity;
import com.alert.mustering.R;
import com.alert.mustering.constants.AppConstants;
import com.alert.mustering.databinding.ActivityLoginBinding;
import com.alert.mustering.helper.ComponentUtil;
import com.alert.mustering.model.Environment;
import com.alert.mustering.model.LoginErrorResponse;
import com.alert.mustering.model.LoginRequest;
import com.alert.mustering.model.LoginResponse;
import com.alert.mustering.services.APIService;
import com.alert.mustering.services.BiometricService;
import com.alert.mustering.ui.forgotPassword.ForgotPasswordActivity;
import com.alert.mustering.ui.otp.OtpActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    public static final String USER_DATA_STORAGE_KEY = "userData";
    static final String TAG = LoginActivity.class.getSimpleName();
    private static final long MIN_CLICK_INTERVAL = 2000;
    public List<Environment> environments;
    TextInputLayout userNameLayout, passwordLayout, tenantLayout;
    TextInputEditText usernameTextInput, passwordTextInput, environmentUrl;
    ProgressBar loadingProgressBar;
    int permission_code = 99;
    String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
    MaterialButton loginButton;
    Boolean remember = false;
    private long lastClickTime = 0;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private String url;
    private String environmentName;
    private LoginViewModel loginViewModel;
    TextView forgotPasswordLink;

    private final TextWatcher afterTenantTextChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String emailInput = null != usernameTextInput.getText() ? usernameTextInput.getText().toString() : null;
            String passwordInput = null != passwordTextInput.getText() ? passwordTextInput.getText().toString() : null;
            String tenantInput = null != environmentUrl.getText() ? environmentUrl.getText().toString() : null;
            loginViewModel.validateTenant(emailInput, passwordInput, tenantInput);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
    private final TextWatcher afterUserNameTextChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String emailInput = null != usernameTextInput.getText() ? usernameTextInput.getText().toString() : null;
            String passwordInput = null != passwordTextInput.getText() ? passwordTextInput.getText().toString() : null;
            String tenantInput = null != environmentUrl.getText() ? environmentUrl.getText().toString() : null;
            environmentUrl.addTextChangedListener(afterTenantTextChangedListener);
            loginViewModel.validateUserName(emailInput, passwordInput, tenantInput);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
    private final TextWatcher afterPasswordTextChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String emailInput = null != usernameTextInput.getText() ? usernameTextInput.getText().toString() : null;
            String passwordInput = null != passwordTextInput.getText() ? passwordTextInput.getText().toString() : null;
            String tenantInput = null != environmentUrl.getText() ? environmentUrl.getText().toString() : null;
            loginViewModel.validatePassword(emailInput, passwordInput, tenantInput);
            environmentUrl.addTextChangedListener(afterTenantTextChangedListener);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate   >>>> Begin");
        //TODO : Bluetooth Permission --Phase II
        ActivityLoginBinding binding;
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Activity activity = null;
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        PackageManager packageManager = getPackageManager();
        loadingProgressBar = binding.loading;
        loadingProgressBar.setVisibility(View.GONE);
        userNameLayout = binding.usernameLayout;
        passwordLayout = binding.passwordLayout;
        ImageButton alertPowerBy = binding.alertPowerBy;
        tenantLayout = binding.tenantInputLayout;
        usernameTextInput = binding.username;
        passwordTextInput = binding.password;
        environmentUrl = binding.environmentUrl;
        loginButton = binding.loginBtn;
        forgotPasswordLink = binding.forgotPassword;
        forgotPasswordLink.setText(ComponentUtil.getSpanLink(getApplicationContext(), R.string.forgot_password));
        LinearLayout tenantGroupLayout = binding.tenantLayout;
        MaterialButtonToggleGroup tenantBtnToggle = binding.toggleButtonTenant;
        CheckBox rememberMe = binding.rememberMe;
        ImageButton biometricButton = binding.biometricButton;
        SharedPreferences userCredentialPreferences = getSharedPreferences(USER_DATA_STORAGE_KEY, MODE_PRIVATE);
        String storedEnvUrl = userCredentialPreferences.getString(AppConstants.STORAGE_KEY_ENV_URL, "null");
        String storedEnvName = userCredentialPreferences.getString(AppConstants.STORAGE_KEY_ENV_NAME, "null");
        String showBioMetricLogin = userCredentialPreferences.getString(AppConstants.STORAGE_KEY_ALLOW_BIOMETRIC_LOGIN, "FALSE");
        String skipLogin = userCredentialPreferences.getString(AppConstants.STORAGE_KEY_SKIP_LOGIN, "FALSE");
        Log.i(TAG, "onCreate skipLogin   >>>>" + skipLogin);
        if (skipLogin.equals("TRUE") && showBioMetricLogin.equals("FALSE")){
            Log.i(TAG, "onCreate skipLogin   >>>>   TRUE");
            APIService.serverUrl = null;
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        // ENVIRONMENTS , toggle BUTTON :begin
        Log.i(TAG, "LoginViewModel.getEnvironments   >>>>   Begin");
        environments = LoginViewModel.getEnvironments(getApplicationContext());
        Log.i(TAG, "onCreate environments   >>>>   " + environments);
        Log.i(TAG, "onCreate: environment size value >>>> " + environments.size());
        if (null != environments && !environments.isEmpty()) {
            for (Environment eee : environments) {
                Log.i(TAG, "NAME >>>>>" + eee.getName() + "  URL >>>>>" + eee.getUrl());
                Log.i(TAG, "env  " + eee);
            }

            Log.i(TAG, "onCreate: inside if >>>>>");
            tenantBtnToggle.removeAllViews();
            Log.i(TAG, "buttons removed >>>>");
            for (int index = 0; index < environments.size(); index++) {
                Log.i(TAG, "onCreate: index >>> " + index);
                Environment environment = environments.get(index);
                Log.i(TAG, "onCreate: environment >>>" + environment.getName());
                Log.i(TAG, "onCreate: Environment class>>>" + environment.getClass());
                MaterialButton btnTenant = new MaterialButton(this, null, R.attr.materialButtonOutlinedStyle);
                Log.i(TAG, "onCreate: Btn created >>>");
                btnTenant.setId(index);
                btnTenant.setText(environment.getName());
                if ("null".equals(storedEnvName) && "PROD".equals(environment.getName())) {
                    url = environment.getUrl();
                    if (environmentUrl != null) {
                        Log.i(TAG, "onCreate: environment text" + url);
                        environmentUrl.setText(url);
                    }
                    tenantBtnToggle.check(index);
                    Log.i(TAG, "onCreate: checked >>>>");
                    environmentName = "PROD";
                } else if (storedEnvName.equals(environment.getName())) {
                    tenantBtnToggle.check(index);
                    environmentName = environment.getName();
                    if (environmentUrl != null && storedEnvUrl!=null && !storedEnvUrl.equals("null")) {
                        environmentUrl.setText(storedEnvUrl);
                    }else if(environmentUrl != null) {
                        environmentUrl.setText(environment.getUrl());
                    }
                    if ("Others".equals(storedEnvName)) {
                        if (tenantLayout != null) {
                            tenantLayout.setVisibility(View.VISIBLE);
                            if (environmentUrl != null) {
                                environmentUrl.addTextChangedListener(afterTenantTextChangedListener);
                            }
                        }
                    } else {
                        if (tenantLayout != null) {
                            tenantLayout.setVisibility(View.GONE);
                        }
                    }
                }

                btnTenant.setPadding(5, 5, 5, 5);
                Log.i(TAG, "onCreate: Adding btn to group");
                tenantBtnToggle.addView(btnTenant);
                Log.i(TAG, "onCreate: btn added");
            }
        }

        tenantBtnToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            Log.i(TAG, "onCreate: tenantBtnToggle.addOnButtonCheckedListener");
            url = environments.get(tenantBtnToggle.getCheckedButtonId()).getUrl();
            Log.i(TAG, "onCreate: tenantBtnToggle.addOnButtonCheckedListener url" + url);
            environmentName = environments.get(tenantBtnToggle.getCheckedButtonId()).getName();
            Log.i(TAG, "onCreate: tenantBtnToggle.addOnButtonCheckedListener environmentName ---" + environmentName);
            if (environments.get(tenantBtnToggle.getCheckedButtonId()).getName().equals("Others") && isChecked) {
                tenantLayout.setVisibility(View.VISIBLE);
                if (storedEnvName.equals("Others")) {
                    environmentUrl.setText(storedEnvUrl);
                } else {
                    environmentUrl.setText("");
                }
                environmentUrl.addTextChangedListener(afterTenantTextChangedListener);
            } else {
                tenantLayout.setVisibility(View.GONE);
                environmentUrl.setText(url);
            }
        });
        //Toggle button code ends
        Log.i(TAG, "onCreate getRememberMe   >>>>   ");
        //Required
        loginViewModel.getRememberMe().observe(this, status -> this.remember = status);
        rememberMe.setOnCheckedChangeListener((compoundButton, isChecked) -> loginViewModel.setRememberMe(isChecked));

        //Remember Me Begin
        loginButton.setEnabled(false);
        Log.i(TAG, "onCreate usernameTextInput   >>>>   ");
        usernameTextInput.setOnFocusChangeListener((v, hasFocus) -> {
            usernameTextInput.addTextChangedListener(afterUserNameTextChangedListener);
            if (!hasFocus) {
                usernameTextInput.setText(usernameTextInput.getText());
            }
        });
        Log.i(TAG, "onCreate passwordTextInput   >>>>   ");
        passwordTextInput.setOnFocusChangeListener((v, hasFocus) -> {
            passwordTextInput.addTextChangedListener(afterPasswordTextChangedListener);
            if (!hasFocus) {
                passwordTextInput.setText(passwordTextInput.getText());
            }
        });
        Log.i(TAG, "onCreate environmentUrl   >>>>   ");
        environmentUrl.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                environmentUrl.setText(environmentUrl.getText());
            }
        });
        Log.i(TAG, "onCreate passwordTextInput setOnEditorActionListener  >>>>   ");
        passwordTextInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(passwordTextInput.getApplicationWindowToken(), 0);
                return true;
            }
            return false;
        });
        Log.i(TAG, "onCreate loginButton.setOnClickListener  >>>>   ");
        loginButton.setOnClickListener(view -> {
            hideKeyboard(view);
            loadingProgressBar.setVisibility(View.VISIBLE);
            loginButton.setClickable(false);
            //Clear image cache during login
            SharedPreferences userImagePreferences = getApplicationContext().getSharedPreferences("UserImage", MODE_PRIVATE);
            userImagePreferences.edit().clear().commit();
            performLogin(null != usernameTextInput.getText() ? usernameTextInput.getText().toString() : null,
                    null != passwordTextInput.getText() ? passwordTextInput.getText().toString() : null,
                    this.remember, null != environmentUrl.getText() ? environmentUrl.getText().toString() : null,
                    environmentName
            );
        });

        forgotPasswordLink.setOnClickListener(view -> {
            hideKeyboard(view);
            loadingProgressBar.setVisibility(View.VISIBLE);
            loginButton.setClickable(false);
            String serverUrl = null != environmentUrl.getText() ? environmentUrl.getText().toString() : null;
            Log.d(TAG, "Environment Url -->serverUrl =" +serverUrl);
            forgotPasswordClicked(serverUrl);
        });

        Log.i(TAG, "onCreate getLoginFormState() getLoginFormState  >>>>   ");
        loginViewModel.getLoginFormState().observe(this, loginFormState -> {
            Log.i(TAG, "Environment Url" + Objects.requireNonNull(environmentUrl.getText()));
            if (loginFormState == null) {
                return;
            }
            loginButton.setEnabled(loginFormState.isDataValid());
            if (loginFormState.getUsernameError() != null) {
                userNameLayout.setError(getString(loginFormState.getUsernameError()));
            } else {
                userNameLayout.setError(null);
            }
            if (loginFormState.getPasswordError() != null) {
                passwordLayout.setError(getString(loginFormState.getPasswordError()));
            } else {
                passwordLayout.setError(null);
            }
            if (loginFormState.getTenantError() != null) {
                tenantLayout.setError(getString(loginFormState.getTenantError()));
            } else {
                tenantLayout.setError(null);
            }
        });
        Log.i(TAG, "onCreate alertPowerBy.setOnClickListener >>>>   ");
        alertPowerBy.setOnClickListener(new View.OnClickListener() {
            int tapCount = 5;

            @Override
            public void onClick(View v) {
                if (tenantGroupLayout.getVisibility() == View.VISIBLE) {
                    ComponentUtil.displayMessage(getApplicationContext(), "Already in developer mode");
                } else {
                    --tapCount;
                    long current = System.currentTimeMillis();
                    long timeDiff = current - lastClickTime;
                    lastClickTime = current;
                    if (timeDiff <= MIN_CLICK_INTERVAL) {
                        if (tapCount == 0) {
                            ComponentUtil.displayMessage(getApplicationContext(), "You are now developer");
                            tenantGroupLayout.setVisibility(View.VISIBLE);
                        }
                    } else {
                        tapCount = 5;
                    }
                }
            }
        });
        Log.i(TAG, "onCreate ACCESS_FINE_LOCATION permission >>>>   " + ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION));
        Log.i(TAG, "onCreate ACCESS_COARSE_LOCATION permission >>>>   " + ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION));
        // TODO: 14-09-2023 Anurag -> If doesnt have location permission disable login button
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "onCreate App has location permissions");
            FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            Log.i(TAG, "Got the location" + location.getLatitude() + " " + location.getLongitude());
                        }
                    });
        } else {
            Log.i(TAG, "onCreate LocationServices.requestPermissions()   ");
            requestPermissions(permissions, permission_code);
        }

        Log.i(TAG, "onCreate BiometricService.getBiometricSettings >>>>   ");
        biometricButton.setVisibility(View.GONE);
        int biometric = BiometricService.getBiometricSettings(packageManager);
        if (biometric > 0) {
            Executor executor = ContextCompat.getMainExecutor(this);
            biometricPrompt = new BiometricPrompt(LoginActivity.this, executor,
                    new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationError(int errorCode,
                                                          @NonNull CharSequence errString) {
                            super.onAuthenticationError(errorCode, errString);
                        }

                        @Override
                        public void onAuthenticationSucceeded(
                                @NonNull BiometricPrompt.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);
                            loadingProgressBar.setVisibility(View.VISIBLE);
                            SharedPreferences sharedPreferences = getSharedPreferences(USER_DATA_STORAGE_KEY, MODE_PRIVATE);
                            String data = sharedPreferences.getString(AppConstants.STORAGE_KEY_TOKEN_INFO, null);
                            LoginResponse apiResponseInStorage;
                            if (null != data) {
                                Gson gson = new Gson();
                                apiResponseInStorage = gson.fromJson(data, LoginResponse.class);
                                if (null != apiResponseInStorage) {
                                    String refreshToken = apiResponseInStorage.getRefresh_token();
                                    new APIService().getNewAccessTokenUsingRefreshToken(refreshToken, getApplicationContext()).enqueue(
                                            new Callback() {
                                                @Override
                                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                    runOnUiThread(() -> {
                                                        loadingProgressBar.setVisibility(View.GONE);
                                                        ComponentUtil.displayMessage(getApplicationContext(), "Error ! while authenticating Biometric");
                                                    });
                                                }

                                                @Override
                                                public void onResponse(@NonNull Call call, @NonNull Response response) {
                                                    if (!response.isSuccessful()) {
                                                        runOnUiThread(() -> {
                                                            loadingProgressBar.setVisibility(View.GONE);
                                                            ComponentUtil.displayMessage(getApplicationContext(), "Error ! Please login with credentials");
                                                        });
                                                    } else {
                                                        runOnUiThread(() -> {
                                                            loadingProgressBar.setVisibility(View.GONE);
                                                            //showToast("Successful Login");
                                                            boolean rememberMe = userCredentialPreferences.getBoolean(AppConstants.TEMP_STORAGE_REMEMBER_ME, false);
                                                            if (rememberMe) {
                                                                SharedPreferences.Editor credsEditor = userCredentialPreferences.edit();
                                                                credsEditor.putString(AppConstants.STORAGE_KEY_SKIP_LOGIN, "TRUE");
                                                                credsEditor.apply();
                                                            }
                                                            APIService.serverUrl = null;
                                                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                            finish();
                                                        });
                                                    }
                                                }
                                            }
                                    );
                                }
                            } else {
                                loadingProgressBar.setVisibility(View.GONE);
                                ComponentUtil.displayMessage(getApplicationContext(), "Data Expired , Please login");
                            }
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                            ComponentUtil.displayMessage(getApplicationContext(), "Please try again");
                        }
                    });

            promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle(getString(R.string.login_for_mustering_app))
                    .setSubtitle(getString(R.string.login_using_your_biometrics))
                    .setNegativeButtonText(getString(R.string.cancel))
                    .setConfirmationRequired(false)
                    .build();
            if (skipLogin.equals("TRUE") && showBioMetricLogin.equals("TRUE")) {
                biometricPrompt.authenticate(promptInfo);
            }
        }
    }

    //overriding the onRequestPermissionResult for getting result of permission granting
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult: requestCode   -->  " + requestCode);
        Log.i(TAG, "onRequestPermissionsResult: permissions   -->  " + permissions);
        Log.i(TAG, "onRequestPermissionsResult: grantResults  -->  " + grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
      
        SharedPreferences userCredentialPreferences = getSharedPreferences(USER_DATA_STORAGE_KEY, MODE_PRIVATE);
        if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED )) {
            Log.i(TAG, "Permissions granted");
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                fusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                SharedPreferences.Editor credsEditor = userCredentialPreferences.edit();
                                credsEditor.putString(AppConstants.LOCATION_LATITUDE, String.valueOf(location.getLatitude()));
                                credsEditor.putString(AppConstants.LOCATION_LONGITUDE, String.valueOf(location.getLongitude()));
                                Log.i(TAG, "Got the location" + location.getLatitude() + " " + location.getLongitude());
                                credsEditor.apply();
                            }
                        });
            }
        }
    }

    private void forgotPasswordClicked(String environmentUrl) {
        Log.i(TAG, "forgotPasswordClicked , Begin");
        SharedPreferences userCredentialPreferences = getSharedPreferences(USER_DATA_STORAGE_KEY, MODE_PRIVATE);
        SharedPreferences.Editor editor = userCredentialPreferences.edit();
        String storedTempEnvUrl = userCredentialPreferences.getString(AppConstants.TEMP_STORAGE_ENV_URL, "null");
        Log.i(TAG, "forgotPasswordClicked , storedTempEnvUrl ===" + storedTempEnvUrl +" environmentUrl="+environmentUrl );
        if(null == storedTempEnvUrl)
            editor.putString(AppConstants.STORAGE_KEY_ENV_URL, environmentUrl);
        else {
            editor.putString(AppConstants.STORAGE_KEY_ENV_URL, storedTempEnvUrl);
        }
        Log.i(TAG, "forgotPasswordClicked , environmentUrl ===" + environmentUrl);
        editor.putString(AppConstants.STORAGE_KEY_SKIP_LOGIN, "FALSE");
        editor.putString(AppConstants.STORAGE_KEY_ALLOW_BIOMETRIC_LOGIN, "FALSE");
        editor.apply();

        Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        intent.putExtra(AppConstants.STORAGE_KEY_ENV_URL,environmentUrl);
        startActivity(intent);
        finish();
    }

    private void performLogin(String userName, String password, Boolean rememberMe, String envUrl, String envName) {
        Log.i(TAG, "performLogin , Begin");
        SharedPreferences userCredentialPreferences = getSharedPreferences(USER_DATA_STORAGE_KEY, MODE_PRIVATE);
        new APIService().login(new LoginRequest(userName, password, rememberMe), getApplicationContext(), envUrl).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.i(TAG, "getUser()  error" + e.getLocalizedMessage());
                runOnUiThread(() -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    Log.e(TAG, Objects.requireNonNull(e.getLocalizedMessage()));
                    loginButton.setClickable(true);
                    ComponentUtil.displayMessage(getApplicationContext(), getResources().getString(R.string.invalid_tenant_contact_admin));
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    if (response.code() == 401 || response.code() == 403 || response.code() == 404 || response.code() == 405) {
                        Log.i(TAG, "onResponse: " + response.code() + " " + response.message());
                        runOnUiThread(() -> {
                            loginButton.setClickable(true);
                            loadingProgressBar.setVisibility(View.GONE);
                            ComponentUtil.displayMessage(getApplicationContext(), "Please check if Data is Valid");
                        });
                    } else {
                        String responseString = Objects.requireNonNull(response.body()).string();
                        Log.i(TAG, "getUser() : response is unsuccessful body ==>" + responseString);
                        LoginErrorResponse responseErrorBody = new Gson().fromJson(responseString, LoginErrorResponse.class);
                        if (null != responseErrorBody && null != responseErrorBody.getMessages() && responseErrorBody.getMessages().length > 0) {
                            runOnUiThread(() -> {
                                loginButton.setClickable(true);
                                loadingProgressBar.setVisibility(View.GONE);
                                Log.i(TAG, "ERROR in responseErrorBody->" + responseErrorBody.getMessages()[0].getMessageDisplayText());
                                ComponentUtil.displayMessage(getApplicationContext(), responseErrorBody.getMessages()[0].getMessageDisplayText());
                            });

                        } else {
                            runOnUiThread(() -> {
                                loginButton.setClickable(true);
                                loadingProgressBar.setVisibility(View.GONE);
                                ComponentUtil.displayMessage(getApplicationContext(), "Error ! while authenticating User");
                            });
                        }
                    }
                } else {
                    LoginResponse loginResponse = new Gson().fromJson(response.peekBody(Long.MAX_VALUE).charStream(), LoginResponse.class);
                    Log.i(TAG, "performLogin() : onResponse  ==>" + loginResponse);
                    if (null != loginResponse) {
                        SharedPreferences.Editor credsEditor = userCredentialPreferences.edit();
                        credsEditor.putBoolean(AppConstants.TEMP_STORAGE_REMEMBER_ME, rememberMe);
                        if (loginResponse.getRequire2fa() != null && loginResponse.getRequire2fa()) {
                            credsEditor.putString(AppConstants.TEMP_STORAGE_ENV_NAME, envName);
                            credsEditor.putString(AppConstants.TEMP_STORAGE_ENV_URL, envUrl);
                            credsEditor.putString(AppConstants.TEMP_STORAGE_ACCESS_TOKEN, new Gson().toJson(loginResponse));
                            credsEditor.apply();
                            runOnUiThread(() -> {
                                loadingProgressBar.setVisibility(View.GONE);
                                //showToast("Successful Login");
                                startActivity(new Intent(LoginActivity.this, OtpActivity.class));
                                finish();
                            });
                        } else {
                            APIService.serverUrl = null;
                            credsEditor.putString(AppConstants.STORAGE_KEY_ENV_NAME, envName);
                            credsEditor.putString(AppConstants.STORAGE_KEY_ENV_URL, envUrl);
                            credsEditor.putString(AppConstants.STORAGE_KEY_TOKEN_INFO, new Gson().toJson(loginResponse));
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
                            runOnUiThread(() -> {
                                loadingProgressBar.setVisibility(View.GONE);
                                //showToast("Successful Login");
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            });
                        }
                    }
                }
            }
        });

    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }
}
