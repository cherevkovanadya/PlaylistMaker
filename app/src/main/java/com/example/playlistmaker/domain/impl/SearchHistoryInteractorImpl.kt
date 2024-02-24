package com.example.playlistmaker.domain.impl

import com.example.playlistmaker.domain.api.SearchHistoryInteractor
import com.example.playlistmaker.domain.api.SearchHistoryRepository
import com.example.playlistmaker.domain.models.Track

class SearchHistoryInteractorImpl(private val searchHistoryRepository: SearchHistoryRepository): SearchHistoryInteractor {
    override fun read(): Array<Track> {
        return searchHistoryRepository.read()
    }

    override fun write(searchHistory: MutableList<Track>) {
        return searchHistoryRepository.write(searchHistory)
    }

    override fun clear() {
        return searchHistoryRepository.clear()
    }
}