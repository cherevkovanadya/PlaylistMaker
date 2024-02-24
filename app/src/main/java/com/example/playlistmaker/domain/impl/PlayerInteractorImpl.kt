package com.example.playlistmaker.domain.impl

import com.example.playlistmaker.domain.api.PlayerInteractor
import com.example.playlistmaker.domain.api.PlayerRepository

class PlayerInteractorImpl(private val playerRepository: PlayerRepository) : PlayerInteractor {
    override fun setDataSource(trackUrl: String?) {
        playerRepository.setDataSource(trackUrl)
    }

    override fun prepareAsync() {
        playerRepository.prepareAsync()
    }

    override fun setOnPreparedListener(param: () -> Unit) {
        playerRepository.setOnPreparedListener(param)
    }

    override fun setOnCompletionListener(param: () -> Unit) {
        playerRepository.setOnCompletionListener(param)
    }

    override fun start() {
        playerRepository.start()
    }

    override fun pause() {
        playerRepository.pause()
    }

    override fun release() {
        playerRepository.release()
    }

    override fun currentPosition(): Int {
        return playerRepository.currentPosition()
    }
}