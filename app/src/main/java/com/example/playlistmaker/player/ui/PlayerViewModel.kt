package com.example.playlistmaker.player.ui

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.playlistmaker.player.ui.states.PlayerState
import com.example.playlistmaker.search.domain.models.Track

class PlayerViewModel(private val track: Track) : ViewModel() {

    private val mediaPlayer = MediaPlayer()

    private val handler = Handler(Looper.getMainLooper())

    private var playerStateLiveData = MutableLiveData<PlayerState>()

    init {
        preparePlayer()
    }
    fun getPlayerStateLiveData(): LiveData<PlayerState> = playerStateLiveData

    private var trackTimePlaying = object : Runnable {
        override fun run() {
            playerStateLiveData.postValue(PlayerState.Play(mediaPlayer.currentPosition.toLong()))
            handler.postDelayed(this, TIMER_DELAY)
        }
    }

    private fun preparePlayer() {
        mediaPlayer.setDataSource(track.previewUrl)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            playerStateLiveData.postValue(PlayerState.Start)
        }
        mediaPlayer.setOnCompletionListener {
            playerStateLiveData.postValue(PlayerState.Finish)
            handler.removeCallbacks(trackTimePlaying)
        }
    }

    fun startPlayer() {
        mediaPlayer.start()
        timer()
    }

    fun pausePlayer() {
        mediaPlayer.pause()
        handler.removeCallbacks(trackTimePlaying)
        playerStateLiveData.postValue(PlayerState.Pause)
    }

    private fun timer() {
        handler.post(trackTimePlaying)
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer.release()
    }

    companion object {
        const val TIMER_DELAY = 500L
        fun getViewModelFactory(track: Track): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PlayerViewModel(
                    track
                )
            }
        }
    }
}