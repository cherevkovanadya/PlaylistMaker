package com.example.playlistmaker.di

import com.example.playlistmaker.sharing.data.ExternalNavigatorImpl
import com.example.playlistmaker.sharing.domain.api.ExternalNavigator
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val navigatorModule = module {
    single<ExternalNavigator> {
        ExternalNavigatorImpl(androidContext())
    }
}