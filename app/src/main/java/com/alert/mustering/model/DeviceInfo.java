package com.alert.mustering.model;

import androidx.annotation.NonNull;

import java.util.TimeZone;

public class DeviceInfo {
    String deviceId;
    String osBuild;
    String appVersion;
    String deviceType;
    String country;
    String location;
    String latitude;
    String longitude;
    String timeZone;

    public DeviceInfo(String deviceId, String osBuild, String appVersion, String deviceType,
                      String country, String location, String latitude, String longitude, String timeZone) {
        this.deviceId = deviceId;
        this.osBuild = osBuild;
        this.appVersion = appVersion;
        this.deviceType = deviceType;
        this.country = country;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeZone = timeZone;
    }

    public String getOsBuild() {
        return osBuild;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getTimeZone() {
        return String.valueOf(TimeZone.getDefault().getID());
    }

    @NonNull
    @Override
    public String toString() {
        return "DeviceInfo{" +
                "deviceId='" + deviceId + '\'' +
                ", osBuild='" + osBuild + '\'' +
                ", appVersion='" + appVersion + '\'' +
                '}';
    }
}
