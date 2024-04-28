package com.example.playlistmaker.player.domain.api

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