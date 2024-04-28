package com.example.playlistmaker.settings.domain.impl

import com.example.playlistmaker.settings.domain.api.SettingsInteractor
import com.example.playlistmaker.settings.domain.api.SettingsRepository

class SettingsInteractorImpl(private val settingsRepository: SettingsRepository) :
    SettingsInteractor {
    override fun getThemeSettings(): Boolean {
        return settingsRepository.getThemeSettings()
    }

    override fun updateThemeSetting(switchTheme: Boolean) {
        settingsRepository.updateThemeSettings(switchTheme)
    }
}