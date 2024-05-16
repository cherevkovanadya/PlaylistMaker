package com.example.playlistmaker.search.ui.states

import com.example.playlistmaker.search.domain.models.Track

sealed interface TracksState {
    object Loading : TracksState

    data class Content(
        val tracks: List<Track>
    ) : TracksState

    object Error : TracksState

    object Empty : TracksState
}