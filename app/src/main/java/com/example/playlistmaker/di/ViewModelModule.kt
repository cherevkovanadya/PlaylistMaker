package com.example.playlistmaker.di

import com.example.playlistmaker.search.ui.TracksSearchViewModel
import com.example.playlistmaker.settings.ui.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        TracksSearchViewModel(get())
    }

    viewModel {
        SettingsViewModel(get(), get())
    }
}