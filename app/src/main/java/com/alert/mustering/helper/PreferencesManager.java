package com.alert.mustering.helper;

import static android.content.Context.MODE_PRIVATE;
import static com.alert.mustering.ui.login.LoginActivity.USER_DATA_STORAGE_KEY;

import android.content.Context;
import android.content.SharedPreferences;

import com.alert.mustering.ui.login.LoginActivity;

public class PreferencesManager {

    //private final IdentityCredentialManager credManager;
    //private final Context ctx;

    public PreferencesManager(){
        super();
    }

    public static boolean isCardAddedToWalletPrefs(String subType, String cardId, Context ctx){
        String key = getKey(subType, cardId);
        String data = getSharedPreferences(ctx).getString(key, null);
        return data != null;
    }

    private static SharedPreferences getSharedPreferences(Context ctx) {
        return ctx.getSharedPreferences(LoginActivity.USER_DATA_STORAGE_KEY, MODE_PRIVATE);
    }

    private static String getKey(String subType, String cardId) {
        return subType +":"+ cardId;
    }

    public static void addCardToWalletPrefs(String subType, String cardId, Context ctx){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(USER_DATA_STORAGE_KEY, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getKey(subType, cardId), cardId);
        editor.apply();
    }
}
