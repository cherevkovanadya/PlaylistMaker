package com.example.playlistmaker.search.domain.api

import com.example.playlistmaker.search.domain.models.Track

interface SearchHistoryInteractor {
    fun read(): Array<Track>

    fun write(searchHistory: MutableList<Track>)

    fun clear()
}