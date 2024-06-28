package com.alert.mustering.constants;

public interface AppConstants {
    String URL_GET_ROUTES = "{0}/nfcconfig.json";
    String URL_LOGIN = "{0}/api/auth/token?grant_type=password";
    String URL_GET_USER = "{0}/api/mobilecred/user/me";
    String URL_GET_USER_IMAGE = "{0}/api/binaryresource/download/{1,number,#}";
    String URL_GET_NEW_TOKEN = "{0}/api/auth/token?grant_type=refresh_token&refresh_token={1}";
    String URL_VALIDATE_OTP = "{0}/api/auth/twofactor/validate";
    String URL_RESEND_OTP = "{0}/api/auth/twofactor/generate";
    String URL_CHANGE_PASSWORD = "{0}/api/user/changePassword";
    String URL_AUTH_TOKEN_SSO = "{0}/api/mobilecred/user/sso";
    String URL_NFC_ADD_CARD = "{0}/api/mobilecred/card/add";
    String URL_FORGOT_PASSWORD_VERIFY_2FA = "{0}/api/auth/verifyuser2f";
    String URL_MOB_LOGIN_DEMO = "&random_id=";
    String URL_REPLACE_STRING = "##random_id##";
    String HEADER_AUTHORIZATION = "Authorization";
    String HEADER_DEVICE_INFORMATION = "x-device-data";
    String HEADER_USER_AGENT = "User-Agent";
    String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
    String USER_AGENT = "Android-App";
    String STORAGE_KEY_TOKEN_INFO = "TokenInformation";
    String STORAGE_KEY_TOKEN_EXPIRY_TIME = "TokenExpiryTime";
    String STORAGE_KEY_ENV_NAME = "EnvironmentName";
    String STORAGE_KEY_ENV_URL = "EnvironmentURL";
    String STORAGE_KEY_USER_IMAGE_ID = "ImageId";
    String STORAGE_KEY_USER_IMAGE = "ImageBitmapCache";
    String STORAGE_KEY_ALLOW_BIOMETRIC_LOGIN = "AllowBiometricLogin";
    String STORAGE_KEY_LINK_SERVER_URL = "LinkHostURL";
    String STORAGE_KEY_SKIP_LOGIN = "SkipLoginNextTime";
    String TEMP_STORAGE_ENV_NAME = "TemporaryEnvironmentName";
    String TEMP_STORAGE_ENV_URL = "TemporaryEnvironmentURL";
    String TEMP_STORAGE_ACCESS_TOKEN = "TemporaryAccessToken";
    String TEMP_STORAGE_REMEMBER_ME = "TemporaryRememberMe";
    String LOCATION_LATITUDE = "LocationLatitude";
    String LOCATION_LONGITUDE = "LocationLongitude";
    String RESET_PASSWORD_FLOW = "ResetPassword";
}
