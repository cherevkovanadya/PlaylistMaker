package com.example.playlistmaker.domain.api

import com.example.playlistmaker.domain.models.Track

interface SearchHistoryRepository {
    fun read(): Array<Track>

    fun write(searchHistory: MutableList<Track>)

    fun clear()
}