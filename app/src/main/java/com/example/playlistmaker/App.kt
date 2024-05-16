package com.example.playlistmaker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.di.interactorModule
import com.example.playlistmaker.di.navigatorModule
import com.example.playlistmaker.di.networkModule
import com.example.playlistmaker.di.repositoryModule
import com.example.playlistmaker.di.viewModelModule
import com.example.playlistmaker.settings.domain.api.SettingsInteractor
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.android.ext.android.inject

class App : Application() {
    private var darkTheme: Boolean = false
    override fun onCreate() {
        super.onCreate()
        val settingsInteractor by inject<SettingsInteractor>()

        startKoin {
            androidContext(this@App)
            modules(
                networkModule,
                repositoryModule,
                interactorModule,
                navigatorModule,
                viewModelModule
            )
        }

        darkTheme = settingsInteractor.getThemeSettings()
        switchTheme(darkTheme)
    }

    private fun switchTheme(darkThemeEnabled: Boolean) {
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