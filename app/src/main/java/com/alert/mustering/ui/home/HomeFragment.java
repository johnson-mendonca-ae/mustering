package com.alert.mustering.ui.home;


import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.alert.mustering.R;
import com.alert.mustering.constants.AppConstants;
import com.alert.mustering.databinding.FragmentHomeBinding;

import com.alert.mustering.ui.changePassword.ChangePasswordActivity;
import com.alert.mustering.ui.login.LoginActivity;
import com.alert.mustering.helper.ComponentUtil;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;

import java.util.List;
import java.util.stream.Collectors;

import static com.alert.mustering.helper.PreferencesManager.addCardToWalletPrefs;
import static com.alert.mustering.helper.PreferencesManager.isCardAddedToWalletPrefs;

public class HomeFragment extends Fragment {
    static final String TAG = HomeFragment.class.getSimpleName();
    public static final int ACTIVE = 0;
    private FragmentHomeBinding binding;
    private HomeViewModel homeviewModel;
    private ProgressBar loadingProgressBar;
    @SuppressLint("NotifyDataSetChanged")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        //homeviewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        RelativeLayout homepageLayout = binding.homePageLayout;
        SwipeRefreshLayout homeSwipeLayout = binding.homeSwipeLayout;
        homepageLayout.setVisibility(View.GONE);
        loadingProgressBar.setVisibility(View.GONE);

        homeviewModel.getUser(getContext());
        homeviewModel.getApiErrorStatus().observe(getViewLifecycleOwner(), apiError -> {
            if (apiError) {
                SharedPreferences userCredentialPreferences = requireContext().getSharedPreferences(LoginActivity.USER_DATA_STORAGE_KEY, MODE_PRIVATE);
                SharedPreferences.Editor editor = userCredentialPreferences.edit();
                editor.putString(AppConstants.STORAGE_KEY_SKIP_LOGIN, "FALSE");
                editor.putString(AppConstants.STORAGE_KEY_ALLOW_BIOMETRIC_LOGIN, "FALSE");
                editor.putLong(AppConstants.STORAGE_KEY_TOKEN_EXPIRY_TIME, Instant.now().toEpochMilli());
                editor.apply();
                Handler toastHandler = new Handler(Looper.getMainLooper());
                toastHandler.post(() -> {
                    ComponentUtil.displayTopMessage(getContext(), "Please login with credentials");
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    startActivity(intent);
                });
            }
        });

        homeviewModel.getApiDigitizationError().observe(getViewLifecycleOwner(), apiError -> {
            if (apiError) {
                loadingProgressBar.setVisibility(View.GONE);
                Handler toastHandler = new Handler(Looper.getMainLooper());
                toastHandler.post(() -> ComponentUtil.displayTopMessage(getContext(), "Error try after sometime !"));
            }
        });
        homeSwipeLayout.setOnRefreshListener(() -> {
            homeviewModel.getUser(getContext());
        });
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}