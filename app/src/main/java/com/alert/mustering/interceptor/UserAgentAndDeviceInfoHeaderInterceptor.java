package com.alert.mustering.interceptor;

import static android.content.Context.MODE_PRIVATE;
import static com.alert.mustering.ui.login.LoginActivity.USER_DATA_STORAGE_KEY;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.alert.mustering.R;
import com.alert.mustering.constants.AppConstants;
import com.alert.mustering.model.DeviceInfo;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.TimeZone;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class UserAgentAndDeviceInfoHeaderInterceptor implements Interceptor {
    static final String TAG = UserAgentAndDeviceInfoHeaderInterceptor.class.getSimpleName();
    static String deviceInfo = null;
    static DeviceInfo deviceInfoDetails;
    private final Context context;

    public UserAgentAndDeviceInfoHeaderInterceptor(Context context) {
        this.context = context;
    }

    public static DeviceInfo getDeviceInfoDetails() {
        return deviceInfoDetails;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        getDeviceInfoHeaderObjectValue(this.context);
        Request original = chain.request();
        Request.Builder builder1 = original.newBuilder()
                .addHeader(AppConstants.HEADER_USER_AGENT, AppConstants.USER_AGENT)
                .addHeader(AppConstants.HEADER_DEVICE_INFORMATION, deviceInfo);
        Request request = builder1.build();
        return chain.proceed(request);

    }

    private void getDeviceInfoHeaderObjectValue(Context context) {
        Log.i(TAG, "getDeviceInfoHeaderObjectValue: Begin");
        SharedPreferences userCredentialPreferences = context.getSharedPreferences(USER_DATA_STORAGE_KEY, MODE_PRIVATE);
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        PackageManager pm = context.getPackageManager();
        String pkgName = context.getPackageName();
        PackageInfo pkgInfo;
        String appVersion;
        String latitude = userCredentialPreferences.getString(AppConstants.LOCATION_LATITUDE, "");
        String longitude = userCredentialPreferences.getString(AppConstants.LOCATION_LONGITUDE, "");
        Log.i(TAG, "getDeviceInfoHeaderObjectValue: >>" + latitude + " " + longitude);
        try {
            pkgInfo = pm.getPackageInfo(pkgName, 0);
            appVersion = pkgInfo.versionName;
            String timeZone = String.valueOf(TimeZone.getDefault().getID());
            String countryCodeValue = tm.getNetworkCountryIso().toUpperCase();
            String deviceType = context.getString(R.string.android);
            String location = TimeZone.getDefault().getID().replaceAll("^(.*?)/.*$", "$1");
            deviceInfo = new Gson().toJson(new DeviceInfo("123123", Build.VERSION.RELEASE_OR_CODENAME, appVersion, deviceType,
                    countryCodeValue, location, latitude, longitude, timeZone));
            Log.i(TAG, "getDeviceInfoHeaderObjectValue: " + deviceInfo);
            deviceInfoDetails = new Gson().fromJson(deviceInfo, DeviceInfo.class);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
