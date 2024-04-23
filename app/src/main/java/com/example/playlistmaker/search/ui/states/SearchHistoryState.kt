package com.example.playlistmaker.search.ui.states

import com.example.playlistmaker.search.domain.models.Track

sealed interface SearchHistoryState {
    data class Empty(
        val tracks: MutableList<Track>
    ) : SearchHistoryState

    data class Content(
        val tracks: MutableList<Track>
    ) : SearchHistoryState
}