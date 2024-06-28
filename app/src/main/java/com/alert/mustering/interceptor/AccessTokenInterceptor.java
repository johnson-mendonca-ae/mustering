package com.alert.mustering.interceptor;

import static android.content.Context.MODE_PRIVATE;
import static com.alert.mustering.ui.login.LoginActivity.USER_DATA_STORAGE_KEY;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.alert.mustering.constants.AppConstants;
import com.alert.mustering.model.LoginResponse;
import com.alert.mustering.services.APIService;
import com.google.gson.Gson;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AccessTokenInterceptor implements Interceptor {

    static final String TAG = AccessTokenInterceptor.class.getSimpleName();
    private final Context context;

    public AccessTokenInterceptor(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Log.i(TAG, " --------- full-----    Log.d(TAG, \"access_Token  is expired  will get New Token using  refresh token\");------------------------  :: BEGIN");
        SharedPreferences sharedPreferences = this.context.getSharedPreferences(USER_DATA_STORAGE_KEY, MODE_PRIVATE);
        String data = sharedPreferences.getString(AppConstants.STORAGE_KEY_TOKEN_INFO, null);
        LoginResponse apiResponseInStorage;
        if (null != data) {
            Gson gson = new Gson();
            apiResponseInStorage = gson.fromJson(data, LoginResponse.class);
            if (null != apiResponseInStorage) {
                String accessToken = apiResponseInStorage.getAccess_token();
                String refreshToken = apiResponseInStorage.getRefresh_token();
                String tokenType = apiResponseInStorage.getToken_type();
                Log.i(TAG, " access_Token  from storage is " + accessToken);
                Log.i(TAG, " refreshToken  from storage is " + refreshToken);
                Log.i(TAG, " tokenType  from storage is " + tokenType);
                long expiry_datetime_millis = sharedPreferences.getLong(AppConstants.STORAGE_KEY_TOKEN_EXPIRY_TIME, Instant.now().toEpochMilli());
                long current_datetime_millis = Instant.now().toEpochMilli();
                if (current_datetime_millis > expiry_datetime_millis) {
                    Log.d(TAG, "access_Token  is expired  will get New Token using  refresh token");
                    Response response = new APIService().getNewAccessTokenUsingRefreshToken(refreshToken, this.context).execute();
                    if (response.isSuccessful()) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        LoginResponse newLoginResponse = new Gson().fromJson(response.peekBody(Long.MAX_VALUE).charStream(), LoginResponse.class);
                        editor.putString(AppConstants.STORAGE_KEY_TOKEN_INFO, new Gson().toJson(newLoginResponse));
                        long new_expiry_datetime_millis = Instant.now().plus(Duration.ofSeconds(newLoginResponse.getExpires_in() - 60)).toEpochMilli();
                        editor.putLong(AppConstants.STORAGE_KEY_TOKEN_EXPIRY_TIME, new_expiry_datetime_millis);
                        editor.apply();
                        Request.Builder original = chain.request().newBuilder();
                        Request.Builder builder1 = original
                                .addHeader(AppConstants.HEADER_AUTHORIZATION, newLoginResponse.getToken_type() + " " + newLoginResponse.getAccess_token());
                        Log.i(TAG, "Authorization is added in Header inside refresh token");
                        return chain.proceed(builder1.build());
                    } else {
                        Request.Builder original = chain.request().newBuilder();
                        Request.Builder builder1 = original
                                .addHeader(AppConstants.HEADER_AUTHORIZATION, "");
                        Log.i(TAG, "Authorization is removed in Header , so it will throw 401 in actual call");
                        return chain.proceed(builder1.build());
                    }
                } else {
                    Log.i(TAG, "access_Token  is  not expired , will be added in Header");
                    Request.Builder original = chain.request().newBuilder();
                    Log.i(TAG, " access_Token  from storage is " + (tokenType + " " + accessToken));
                    Request.Builder builder1 = original
                            .addHeader(AppConstants.HEADER_AUTHORIZATION, tokenType + " " + accessToken);
                    Log.i(TAG, "Authorization is added in Header");
                    return chain.proceed(builder1.build());
                }
            }
            Log.i(TAG, "--------------intercept()------------------------   :: END");
        } else {
            Request.Builder original = chain.request().newBuilder();
            Request.Builder builder1 = original
                    .addHeader(AppConstants.HEADER_AUTHORIZATION, "");
            Log.i(TAG, "Authorization is removed in Header , so it will throw 401 in actual call");
            return chain.proceed(builder1.build());
        }
        return chain.proceed(chain.request());
    }
}
