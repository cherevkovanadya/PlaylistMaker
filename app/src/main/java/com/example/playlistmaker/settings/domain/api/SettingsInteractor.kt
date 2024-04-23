package com.example.playlistmaker.settings.domain.api

interface SettingsInteractor {
    fun getThemeSettings(): Boolean
    fun updateThemeSetting(switchTheme: Boolean)
}