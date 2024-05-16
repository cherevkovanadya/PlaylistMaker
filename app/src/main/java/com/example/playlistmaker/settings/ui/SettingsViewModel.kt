package com.example.playlistmaker.settings.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.settings.domain.api.SettingsInteractor
import com.example.playlistmaker.sharing.domain.api.SharingInteractor

class SettingsViewModel(
    private val sharingInteractor: SharingInteractor,
    private val settingsInteractor: SettingsInteractor,
) : ViewModel() {

    private var stateThemeLiveData = MutableLiveData<Boolean>()

    init {
        stateThemeLiveData.value = settingsInteractor.getThemeSettings()
    }

    fun getStateThemeLiveData(): LiveData<Boolean> = stateThemeLiveData

    fun updateThemeSettings() {
        settingsInteractor.updateThemeSetting(stateThemeLiveData.value!!)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        stateThemeLiveData.value = darkThemeEnabled
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }

    fun shareApp() {
        sharingInteractor.shareApp()
    }

    fun openTerms() {
        sharingInteractor.openTerms()
    }

    fun openSupport() {
        sharingInteractor.openSupport()
    }
}