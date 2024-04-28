package com.example.playlistmaker.search.domain.impl

import com.example.playlistmaker.search.domain.api.SearchHistoryInteractor
import com.example.playlistmaker.search.domain.api.SearchHistoryRepository
import com.example.playlistmaker.search.domain.models.Track

class SearchHistoryInteractorImpl(private val searchHistoryRepository: SearchHistoryRepository):
    SearchHistoryInteractor {
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