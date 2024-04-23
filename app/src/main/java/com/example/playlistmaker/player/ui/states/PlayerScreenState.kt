package com.example.playlistmaker.player.ui.states

import com.example.playlistmaker.search.domain.models.Track

sealed interface PlayerScreenState {
    object Loading : PlayerScreenState
    data class Content(val track: Track) : PlayerScreenState
}