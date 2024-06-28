package com.alert.mustering;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.alert.mustering.databinding.ActivityMainBinding;
import com.alert.mustering.shared.SharedViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    static final String TAG = MainActivity.class.getSimpleName();
    BottomNavigationView bottomNavigation;
    NavController navController;
    SharedViewModel sharedViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate ,Begin");
        super.onCreate(savedInstanceState);
        com.alert.mustering.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        bottomNavigation = binding.navView;
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        sharedViewModel.getShowBottomMenu().observe(this, item -> {
            if (item) {
                bottomNavigation.setVisibility(View.VISIBLE);
            } else {
                bottomNavigation.setVisibility(View.GONE);
            }
        });
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            Log.i(TAG, "onCreate ,Got NavController");
            NavigationUI.setupWithNavController(bottomNavigation, navController);
        }


        Log.i(TAG, "onCreate ,End");
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        Log.i(TAG, "Disabled Back Button");
    }
}