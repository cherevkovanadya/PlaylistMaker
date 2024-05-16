package com.example.playlistmaker.player.ui.states

sealed interface PlayerState {
    object Start : PlayerState
    data class Play(val trackTimePlaying: Long) : PlayerState
    object Pause : PlayerState
    object Finish : PlayerState
}