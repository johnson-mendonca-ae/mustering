package com.alert.mustering.services;

import static android.content.Context.MODE_PRIVATE;
import static com.alert.mustering.ui.login.LoginActivity.USER_DATA_STORAGE_KEY;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.alert.mustering.constants.AppConstants;
import com.alert.mustering.interceptor.AccessTokenInterceptor;
import com.alert.mustering.interceptor.TempAccessTokenInterceptor;
import com.alert.mustering.interceptor.UserAgentAndDeviceInfoHeaderInterceptor;
import com.alert.mustering.model.ChangePasswordPayload;
import com.alert.mustering.model.ForgotPasswordPayload;
import com.alert.mustering.model.LoginRequest;
import com.alert.mustering.model.OtpPayload;
import com.google.gson.Gson;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;


public class APIService {
    public static final MediaType JSON = MediaType.get(AppConstants.JSON_CONTENT_TYPE);
    static final String TAG = APIService.class.getSimpleName();
    public static String serverUrl;

    public Call login(LoginRequest payload, Context context, String envUrl) {
        Log.i(TAG, "login : begin");
        try {
            String url = MessageFormat.format(AppConstants.URL_LOGIN, envUrl);
            Log.i(TAG, "login: " + url);
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder = com.alert.mustering.services.OkUnsafeClient.configureToIgnoreCertificate(builder);
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.networkInterceptors().add(httpLoggingInterceptor);
            builder.addInterceptor(new UserAgentAndDeviceInfoHeaderInterceptor(context));
            RequestBody body = RequestBody.create(new Gson().toJson(payload), JSON);
            Request request = new Request.Builder().url(url).post(body).build();
            return builder.build().newCall(request);
        } catch (Exception _e) {
            Log.e(TAG, "login : Exception" + _e.getLocalizedMessage());
        }
        return null;
    }

    public Call validateOtp(OtpPayload payload, Context context, String hostUrl) {
        Log.i(TAG, "validateOtp : begin");
        try {
            String url = MessageFormat.format(AppConstants.URL_VALIDATE_OTP, hostUrl);
            Log.i(TAG, "validateOtp " + url);
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder = com.alert.mustering.services.OkUnsafeClient.configureToIgnoreCertificate(builder);
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.networkInterceptors().add(httpLoggingInterceptor);
            builder.addInterceptor(new TempAccessTokenInterceptor(context));
            builder.addInterceptor(new UserAgentAndDeviceInfoHeaderInterceptor(context));
            RequestBody body = RequestBody.create(new Gson().toJson(payload), JSON);
            Request request = new Request.Builder().url(url).post(body).build();
            return builder.build().newCall(request);
        } catch (Exception _e) {
            Log.e(TAG, "validateOtp" + _e.getLocalizedMessage());
        }
        return null;
    }

    public Call resendOtp(Context context, String hostUrl) {
        Log.i(TAG, "resendOtp : begin");
        try {
            String url = MessageFormat.format(AppConstants.URL_RESEND_OTP, hostUrl);
            Log.i(TAG, "resendOtp " + url);
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder = com.alert.mustering.services.OkUnsafeClient.configureToIgnoreCertificate(builder);
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.networkInterceptors().add(httpLoggingInterceptor);
            builder.addInterceptor(new TempAccessTokenInterceptor(context));
            builder.addInterceptor(new UserAgentAndDeviceInfoHeaderInterceptor(context));
            RequestBody body = RequestBody.create("{}", JSON);
            Request request = new Request.Builder().url(url).post(body).build();
            return builder.build().newCall(request);
        } catch (Exception _e) {
            Log.e(TAG, "resendOtp" + _e.getLocalizedMessage());
        }
        return null;
    }

    public Call getUser(Context context) {
        Log.i(TAG, "getUser : begin");
        getServerUrl(context);
        try {
            String url = MessageFormat.format(AppConstants.URL_GET_USER, serverUrl);
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder = com.alert.mustering.services.OkUnsafeClient.configureToIgnoreCertificate(builder);
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.networkInterceptors().add(httpLoggingInterceptor);
            builder.addInterceptor(new AccessTokenInterceptor(context));
            builder.addInterceptor(new UserAgentAndDeviceInfoHeaderInterceptor(context));
            Request request = new Request.Builder().cacheControl(new CacheControl.Builder().maxAge(0, TimeUnit.SECONDS).build())
                    .url(url)
                    .build();
            return builder.build().newCall(request);
        } catch (Exception e) {
            Log.e(TAG, "getUser" + e.getLocalizedMessage());
        }
        return null;
    }

    public Call getRoutes(Context context) {
        getServerUrl(context);
        Log.i(TAG, "getRoutes : begin");
        try {
            String url = MessageFormat.format(AppConstants.URL_GET_ROUTES, serverUrl);
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder = com.alert.mustering.services.OkUnsafeClient.configureToIgnoreCertificate(builder);
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.networkInterceptors().add(httpLoggingInterceptor);
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            return builder.build().newCall(request);
        } catch (Exception e) {
            Log.e(TAG, "getRoutes" + e.getLocalizedMessage());
        }
        return null;
    }

    public Call changePassword(ChangePasswordPayload payload, Context context) {
        getServerUrl(context);
        Log.i(TAG, "changePassword: begin");
        try {
            String url = MessageFormat.format(AppConstants.URL_CHANGE_PASSWORD, serverUrl);
            OkHttpClient.Builder builder = com.alert.mustering.services.OkUnsafeClient.configureToIgnoreCertificate(new OkHttpClient.Builder());
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.networkInterceptors().add(httpLoggingInterceptor);
            builder.addInterceptor(new AccessTokenInterceptor(context));
            RequestBody body = RequestBody.create(new Gson().toJson(payload), JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            return builder.build().newCall(request);
        } catch (Exception e) {
            Log.e(TAG, "changePassword" + e.getLocalizedMessage());
        }
        return null;
    }

    public Call getUserImage(Context context, long userImageId) {
        Log.i(TAG, "getUserImage : begin");
        getServerUrl(context);
        try {
            String url = MessageFormat.format(AppConstants.URL_GET_USER_IMAGE, serverUrl, userImageId);
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder = com.alert.mustering.services.OkUnsafeClient.configureToIgnoreCertificate(builder);
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.networkInterceptors().add(httpLoggingInterceptor);
            builder.addInterceptor(new AccessTokenInterceptor(context));
            builder.addInterceptor(new UserAgentAndDeviceInfoHeaderInterceptor(context));
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            return builder.build().newCall(request);
        } catch (Exception e) {
            Log.e(TAG, "getUserImage" + e.getLocalizedMessage());
        }
        return null;
    }

    public Call getNewAccessTokenUsingRefreshToken(String refreshToken, Context context) {
        Log.i(TAG, "getNewAccessTokenUsingRefreshToken : begin");
        getServerUrl(context);
        try {
            String url = MessageFormat.format(AppConstants.URL_GET_NEW_TOKEN, serverUrl, refreshToken);
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder = com.alert.mustering.services.OkUnsafeClient.configureToIgnoreCertificate(builder);
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.networkInterceptors().add(httpLoggingInterceptor);
            builder.addInterceptor(new UserAgentAndDeviceInfoHeaderInterceptor(context));
            RequestBody body = RequestBody.create(new Gson().toJson("{}"), JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            return builder.build().newCall(request);
        } catch (Exception e) {
            Log.e(TAG, "getNewAccessTokenUsingRefreshToken" + e.getLocalizedMessage());
        }
        return null;
    }

    public Call getAccessTokenSSO(Context context) {
        Log.i(TAG, "getNewAccessTokenSSO: Begin");
        getServerUrl(context);
        try {
            String url = MessageFormat.format(AppConstants.URL_AUTH_TOKEN_SSO, serverUrl);
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder = com.alert.mustering.services.OkUnsafeClient.configureToIgnoreCertificate(builder);
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.networkInterceptors().add(httpLoggingInterceptor);
            builder.addInterceptor(new AccessTokenInterceptor(context));
            RequestBody body = RequestBody.create(new Gson().toJson("{}"), JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            return builder.build().newCall(request);
        } catch (Exception e) {
            Log.e(TAG, "getAccessTokenSSO" + e.getLocalizedMessage());
        }
        return null;
    }
    public Call verify2FA(ForgotPasswordPayload payload, Context context,  String serverUrl) {
        Log.i(TAG, "verify2FA: Begin");
        try {
            String url = MessageFormat.format(AppConstants.URL_FORGOT_PASSWORD_VERIFY_2FA, serverUrl);
            Log.d(TAG, "verify2FA: url="+url +" payload="+payload);
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder = com.alert.mustering.services.OkUnsafeClient.configureToIgnoreCertificate(builder);
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.networkInterceptors().add(httpLoggingInterceptor);
            builder.addInterceptor(new AccessTokenInterceptor(context));
            RequestBody body = RequestBody.create(new Gson().toJson(payload), JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            return builder.build().newCall(request);
        } catch (Exception e) {
            Log.e(TAG, "verify2FA" + e.getLocalizedMessage());
        }
        return null;
    }

    public Call sendOtp(Context context, String hostUrl) {
        Log.i(TAG, "sendOtp : begin");
        try {
            String url = MessageFormat.format(AppConstants.URL_RESEND_OTP, hostUrl);
            Log.i(TAG, "sendOtp " + url);
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder = com.alert.mustering.services.OkUnsafeClient.configureToIgnoreCertificate(builder);
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.networkInterceptors().add(httpLoggingInterceptor);
            builder.addInterceptor(new TempAccessTokenInterceptor(context));
           // builder.addInterceptor(new AccessTokenInterceptor(context));
            builder.addInterceptor(new UserAgentAndDeviceInfoHeaderInterceptor(context));
            RequestBody body = RequestBody.create("{}", JSON);
            Request request = new Request.Builder().url(url).post(body).build();
            return builder.build().newCall(request);
        } catch (Exception _e) {
            Log.e(TAG, "sendOtp" + _e.getLocalizedMessage());
        }
        return null;
    }

    public void getServerUrl(Context context) {
        if (null == serverUrl || serverUrl.isEmpty()) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(USER_DATA_STORAGE_KEY, MODE_PRIVATE);
            String data = sharedPreferences.getString(AppConstants.STORAGE_KEY_ENV_URL, null);
            if (null != data) {
                serverUrl = data;
            }
        }
    }

}
