package com.example.playlistmaker.search.domain.api

import com.example.playlistmaker.search.domain.models.Track

interface SearchHistoryRepository {
    fun read(): Array<Track>

    fun write(searchHistory: MutableList<Track>)

    fun clear()
}