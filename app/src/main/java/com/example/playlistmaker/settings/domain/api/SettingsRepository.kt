package com.example.playlistmaker.settings.domain.api

interface SettingsRepository {
    fun getThemeSettings(): Boolean
    fun updateThemeSettings(switchTheme: Boolean)
}