package com.example.playlistmaker.domain.api

interface PlayerInteractor {
    fun setDataSource(trackUrl: String?)

    fun prepareAsync()

    fun setOnPreparedListener(param: () -> Unit)

    fun setOnCompletionListener(param: () -> Unit)

    fun start()

    fun pause()

    fun release()

    fun currentPosition(): Int
}