package com.example.playlistmaker.player.ui.states

sealed interface PlayerState {
    object Loading : PlayerState
    data class Play(val progress: Int) : PlayerState
    object Pause : PlayerState
    object Finish : PlayerState
}