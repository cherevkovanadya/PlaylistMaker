package com.example.playlistmaker.player.data

import android.media.MediaPlayer
import com.example.playlistmaker.player.domain.api.PlayerRepository

class PlayerRepositoryImpl(private var mediaPlayer: MediaPlayer): PlayerRepository {
    override fun setDataSource(trackUrl: String?) {
        mediaPlayer.setDataSource(trackUrl)
    }

    override fun prepareAsync() {
        mediaPlayer.prepareAsync()
    }

    override fun setOnPreparedListener(param: () -> Unit) {
        mediaPlayer.setOnPreparedListener { param.invoke() }
    }

    override fun setOnCompletionListener(param: () -> Unit) {
        mediaPlayer.setOnCompletionListener { param.invoke() }
    }

    override fun start() {
        mediaPlayer.start()
    }

    override fun pause() {
        mediaPlayer.pause()
    }

    override fun release() {
        mediaPlayer.release()
    }

    override fun currentPosition(): Int {
        return mediaPlayer.currentPosition
    }
}