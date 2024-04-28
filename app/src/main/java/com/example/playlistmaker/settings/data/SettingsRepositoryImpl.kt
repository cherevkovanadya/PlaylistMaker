package com.example.playlistmaker.settings.data

import android.content.Context
import com.example.playlistmaker.settings.domain.api.SettingsRepository

const val THEME_SETTINGS_PREFERENCES = "theme_switcher_preferences"
const val SWITCH_KEY = "key_for_switch"

class SettingsRepositoryImpl(context: Context) : SettingsRepository {
    private val sharedPreferences = context.getSharedPreferences(THEME_SETTINGS_PREFERENCES, Context.MODE_PRIVATE)
    override fun getThemeSettings(): Boolean {
        return sharedPreferences.getBoolean(SWITCH_KEY, true)
    }

    override fun updateThemeSettings(switchTheme: Boolean) {
        sharedPreferences.edit().putBoolean(SWITCH_KEY, switchTheme).apply()
    }
}