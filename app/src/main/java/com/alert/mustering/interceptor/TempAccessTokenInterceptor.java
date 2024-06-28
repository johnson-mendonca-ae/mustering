package com.alert.mustering.interceptor;

import static android.content.Context.MODE_PRIVATE;
import static com.alert.mustering.ui.login.LoginActivity.USER_DATA_STORAGE_KEY;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.alert.mustering.constants.AppConstants;
import com.alert.mustering.model.LoginResponse;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TempAccessTokenInterceptor implements Interceptor {
    static final String TAG = TempAccessTokenInterceptor.class.getSimpleName();
    private final Context context;

    public TempAccessTokenInterceptor(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Log.i(TAG, " --------TempAccessTokenInterceptor ------intercept()------------------  :: BEGIN");
        SharedPreferences sharedPreferences = this.context.getSharedPreferences(USER_DATA_STORAGE_KEY, MODE_PRIVATE);
        String data = sharedPreferences.getString(AppConstants.TEMP_STORAGE_ACCESS_TOKEN, null);
        LoginResponse apiResponseInStorage;
        if (null != data) {
            Gson gson = new Gson();
            apiResponseInStorage = gson.fromJson(data, LoginResponse.class);
            if (null != apiResponseInStorage) {
                Log.d(TAG, "access_Token ==> " + apiResponseInStorage.getAccess_token());
                String accessToken = apiResponseInStorage.getAccess_token();
                String tokenType = apiResponseInStorage.getToken_type();
                Request.Builder original = chain.request().newBuilder();
                Request.Builder builder1 = original
                        .addHeader(AppConstants.HEADER_AUTHORIZATION, tokenType + " " + accessToken);
                Log.i(TAG, "Authorization is added in Header");
                return chain.proceed(builder1.build());
            }
            Log.i(TAG, "------TempAccessTokenInterceptor--------intercept()----------------   :: END");
        }
        return chain.proceed(chain.request());
    }
}
