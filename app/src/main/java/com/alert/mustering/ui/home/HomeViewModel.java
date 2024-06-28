package com.alert.mustering.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alert.mustering.R;
import com.alert.mustering.constants.AppConstants;
import com.alert.mustering.services.APIService;
import com.alert.mustering.ui.login.LoginActivity;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HomeViewModel extends ViewModel {
    public static final String SHARED_PREF_KEY_USER_IMAGE_DATA = "UserImage";
    private final static String TAG = HomeViewModel.class.getSimpleName();
    private final MutableLiveData<Boolean> _apiErrorStatus;
    private final MutableLiveData<Boolean> _apiDigitizationError;
    private final MutableLiveData<Bitmap> userImage = new MutableLiveData<>();
    private MutableLiveData<Boolean> _cardClickedAction;
    private String badgesJsonString;


    public HomeViewModel() {
        _apiErrorStatus = new MutableLiveData<>();
        _apiErrorStatus.setValue(false);
        _apiDigitizationError = new MutableLiveData<>();
        _apiDigitizationError.setValue(false);
        _cardClickedAction = new MutableLiveData<>(false);
    }


    public LiveData<Boolean> getApiErrorStatus() {
        return _apiErrorStatus;
    }

    public LiveData<Boolean> getApiDigitizationError() {
        return _apiDigitizationError;
    }

    public MutableLiveData<Bitmap> getUserImage() {
        return userImage;
    }

    public MutableLiveData<Boolean> isCardClicked() {
        return _cardClickedAction;
    }

    public void setCardClicked(Boolean action) {
        _cardClickedAction = new MutableLiveData<>(action);
    }

    public String getBadgesJsonString() {
        return badgesJsonString;
    }

    public void getUser(Context context) {
        Log.i(TAG, "getUser , Begin");
        try {
            Log.d(TAG, "getUser");
            Call call = new APIService().getUser(context);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.i(TAG, "getUser()  error" + e.getLocalizedMessage());
                    _apiErrorStatus.postValue(true);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        _apiErrorStatus.postValue(true);
                    } else {
                        _apiErrorStatus.postValue(false);
                        String apiResponseString = response.body().string();
                        Log.i(TAG, "getUser() : response is successful body ==>" + apiResponseString);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, Objects.requireNonNull(e.getLocalizedMessage()));
        }
    }

}