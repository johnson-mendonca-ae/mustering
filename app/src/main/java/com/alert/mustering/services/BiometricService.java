package com.alert.mustering.services;

import android.content.pm.PackageManager;

import com.alert.mustering.R;

public class BiometricService {
    public static int getBiometricSettings(PackageManager packageManager) {
        boolean hasFaceDetection = packageManager.hasSystemFeature(PackageManager.FEATURE_FACE);
        boolean hasFingerPrint = packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT);
        if (hasFaceDetection) {
            return R.drawable.faceid;
        } else if (hasFingerPrint) {
            return R.drawable.baseline_fingerprint_90;
        } else {
            return 0;
        }
    }
}
