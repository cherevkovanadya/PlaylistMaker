package com.example.playlistmaker.data.dto

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.presentation.ui.settings.SWITCH_KEY
import com.example.playlistmaker.presentation.ui.settings.THEME_SETTINGS_PREFERENCES

class App : Application() {
    var darkTheme: Boolean = false
    override fun onCreate() {
        super.onCreate()
        val sharedPrefs = getSharedPreferences(THEME_SETTINGS_PREFERENCES, MODE_PRIVATE)
        darkTheme = sharedPrefs.getBoolean(SWITCH_KEY, false)
        switchTheme(darkTheme)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}