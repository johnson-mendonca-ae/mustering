package com.alert.mustering.shared;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Boolean> showBottomMenu;

    public SharedViewModel() {
        showBottomMenu = new MutableLiveData<>();
        showBottomMenu.setValue(true);
        MutableLiveData<Integer> recordCount = new MutableLiveData<>();
        recordCount.setValue(10);
    }

    public LiveData<Boolean> getShowBottomMenu() {
        return showBottomMenu;
    }

    public void setShowBottomMenu(Boolean showBottomMenuData) {
        showBottomMenu.setValue(showBottomMenuData);
    }
}