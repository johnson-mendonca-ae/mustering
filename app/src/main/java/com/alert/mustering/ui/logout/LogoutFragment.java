package com.alert.mustering.ui.logout;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.alert.mustering.constants.AppConstants;
import com.alert.mustering.databinding.FragmentLogoutBinding;
import com.alert.mustering.ui.login.LoginActivity;

import java.time.Instant;


public class LogoutFragment extends Fragment {

    private FragmentLogoutBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        LogoutViewModel notificationsViewModel =
                new ViewModelProvider(this).get(LogoutViewModel.class);

        binding = FragmentLogoutBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textLogout;
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences userCredentialPreferences = requireContext().getSharedPreferences(LoginActivity.USER_DATA_STORAGE_KEY, MODE_PRIVATE);
        SharedPreferences.Editor editor = userCredentialPreferences.edit();
        editor.putString(AppConstants.STORAGE_KEY_SKIP_LOGIN, "FALSE");
        editor.putLong(AppConstants.STORAGE_KEY_TOKEN_EXPIRY_TIME, Instant.now().toEpochMilli());
        editor.apply();
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}