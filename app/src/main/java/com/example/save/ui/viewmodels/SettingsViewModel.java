package com.example.save.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.save.utils.SettingsManager;

public class SettingsViewModel extends AndroidViewModel {

    private final SettingsManager settingsManager;

    private final MutableLiveData<Boolean> pushNotificationsEnabled = new MutableLiveData<>();
    private final MutableLiveData<Boolean> darkModeEnabled = new MutableLiveData<>();
    private final MutableLiveData<Boolean> biometricLoginEnabled = new MutableLiveData<>();

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        settingsManager = SettingsManager.getInstance(application);
        loadSettings();
    }

    private void loadSettings() {
        pushNotificationsEnabled.setValue(settingsManager.isPushNotificationsEnabled());
        darkModeEnabled.setValue(settingsManager.isDarkModeEnabled());
        biometricLoginEnabled.setValue(settingsManager.isBiometricLoginEnabled());
    }

    // --- Getters for UI ---

    public LiveData<Boolean> getPushNotificationsEnabled() {
        return pushNotificationsEnabled;
    }

    public LiveData<Boolean> getDarkModeEnabled() {
        return darkModeEnabled;
    }

    public LiveData<Boolean> getBiometricLoginEnabled() {
        return biometricLoginEnabled;
    }

    // --- Setters (Updates UI and SharedPreferences) ---

    public void setPushNotificationsEnabled(boolean enabled) {
        settingsManager.setPushNotificationsEnabled(enabled);
        pushNotificationsEnabled.setValue(enabled);
    }

    public void setDarkModeEnabled(boolean enabled) {
        settingsManager.setDarkModeEnabled(enabled);
        darkModeEnabled.setValue(enabled);
    }

    public void setBiometricLoginEnabled(boolean enabled) {
        settingsManager.setBiometricLoginEnabled(enabled);
        biometricLoginEnabled.setValue(enabled);
    }

    public void setAppPin(String pin) {
        settingsManager.setAppPin(pin);
    }
}
