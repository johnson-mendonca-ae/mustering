package com.alert.mustering.ui.login;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alert.mustering.R;
import com.alert.mustering.model.Environment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;


public class LoginViewModel extends ViewModel {
    static final String TAG = LoginViewModel.class.getSimpleName();
    final LoginFormState loginForm;
    private final MutableLiveData<Boolean> rememberMe = new MutableLiveData<>();
    private final MutableLiveData<LoginFormState> loginFormState;

    public LoginViewModel() {
        loginForm = new LoginFormState();
        loginFormState = new MutableLiveData<>();
        loginFormState.setValue(loginForm);
    }

    public static List<Environment> getEnvironments(Context context) {
        Log.i(TAG, "getEnvironments ->>   Begin ");
        try {
            StringBuilder jsonStringBuilder = new StringBuilder();
            Log.i(TAG, "getEnvironments ->>   1 ");
            Resources resources = context.getResources();
            Log.i(TAG, "getEnvironments ->>   2 ");
            InputStream inputStream = resources.openRawResource(R.raw.env);
            Log.i(TAG, "getEnvironments ->>   3 ");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                jsonStringBuilder.append(line);
            }
            reader.close();
            Log.i(TAG, "getEnvironments ->>   4 ");

            String jsonString = jsonStringBuilder.toString();
            Log.i(TAG, "getEnvironments ->>   4.1 " + jsonString);

            Type listType = new TypeToken<List<Environment>>() {
            }.getType();
            List<Environment> environmentsList = new Gson().fromJson(jsonString, listType);
            Log.i(TAG, "getEnvironments ->>   5 ");
            for (Environment env : environmentsList) {
                Log.i(TAG, "name  " + env.getName() + " URL" + env.getUrl());
                Log.i(TAG, "env  " + env);
            }
            return environmentsList;

        } catch (Exception e) {
            Log.i(TAG, "getEnvironments ->>   6 " + e.getLocalizedMessage());
            e.printStackTrace();
            Log.e(TAG, "getEnvironments ->>   Exception   -->> " + e.getLocalizedMessage());
        }
        return null;
    }

    public LiveData<Boolean> getRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(Boolean mode) {
        rememberMe.postValue(mode);
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    public void validateUserName(String username, String password, String tenant) {
        if (username == null || (username.isEmpty())) {
            loginForm.setUsernameError(R.string.invalid_username);
        } else {
            loginForm.setUsernameError(null);
        }
        validateAllMandatoryAttributes(username, password, tenant);
    }

    public boolean isValidUserName(String username) {
        return username != null && (!username.isEmpty());
    }

    public void validatePassword(String username, String password, String tenant) {
        if (password == null || (password.isEmpty())) {
            loginForm.setPasswordError(R.string.invalid_password);
        } else {
            loginForm.setPasswordError(null);
        }
        validateAllMandatoryAttributes(username, password, tenant);
    }

    private void validateAllMandatoryAttributes(String username, String password, String tenant) {
        loginForm.setDataValid(isValidUserName(username) && isValidPassword(password) && isValidTenant(tenant));
        loginFormState.postValue(loginForm);
    }

    public boolean isValidPassword(String password) {
        if (password == null || (password.isEmpty())) {
            return false;
        } else {
            return true;
        }

    }

    //TODO- allow only specific tenants to be valid
    public void validateTenant(String username, String password, String tenant) {
        if (tenant == null || (tenant.isEmpty())) {
            loginForm.setTenantError(R.string.invalid_tenant);
        } else if (!Patterns.WEB_URL.matcher(tenant).matches()) {
            loginForm.setTenantError(R.string.tenant_domain_error);
        } else {
            loginForm.setTenantError(null);
        }
        validateAllMandatoryAttributes(username, password, tenant);
    }

    public boolean isValidTenant(String tenant) {
        return tenant != null && (!tenant.isEmpty()) && Patterns.WEB_URL.matcher(tenant).matches();
    }

}