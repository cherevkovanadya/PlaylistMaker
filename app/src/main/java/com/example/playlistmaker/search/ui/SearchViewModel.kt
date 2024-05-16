package com.example.playlistmaker.search.ui

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.search.domain.api.SearchHistoryInteractor
import com.example.playlistmaker.search.domain.api.TracksInteractor
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.search.ui.states.SearchHistoryState
import com.example.playlistmaker.search.ui.states.TracksState

class SearchViewModel(
    private val searchHistorySharedPreferences: SearchHistoryInteractor,
    private val tracksInteractor: TracksInteractor
) : ViewModel() {

    companion object {
        const val NUMBER_OF_SONGS_IN_HISTORY = 10
        const val SEARCH_DEBOUNCE_DELAY = 2000L
        private val SEARCH_REQUEST_TOKEN = Any()
    }

    private val handler = Handler(Looper.getMainLooper())

    private val stateTracksLiveData = MutableLiveData<TracksState>()
    fun getStateTracksLiveData(): LiveData<TracksState> = stateTracksLiveData

    private val stateSearchHistoryLiveData = MutableLiveData<SearchHistoryState>()
    fun getStateSearchHistoryLiveData(): LiveData<SearchHistoryState> = stateSearchHistoryLiveData

    private var tracks: MutableList<Track> = mutableListOf()
    private var tracksHistory: MutableList<Track> = mutableListOf()

    private var latestSearchText: String? = null

    override fun onCleared() {
        handler.removeCallbacksAndMessages(SEARCH_REQUEST_TOKEN)
    }

    fun searchDebounce(changedText: String) {
        if (latestSearchText == changedText) {
            return
        }

        this.latestSearchText = changedText
        handler.removeCallbacksAndMessages(SEARCH_REQUEST_TOKEN)

        val searchRunnable = Runnable { searchRequest(changedText) }

        val postTime = SystemClock.uptimeMillis() + SEARCH_DEBOUNCE_DELAY
        handler.postAtTime(
            searchRunnable,
            SEARCH_REQUEST_TOKEN,
            postTime,
        )
    }

    fun searchRequest(newSearchText: String) {
        if (newSearchText.isNotEmpty()) {

            renderState(TracksState.Loading)

            tracksInteractor.searchTracks(newSearchText, object : TracksInteractor.TracksConsumer {
                override fun consume(foundTracks: List<Track>?, errorType: Int?) {
                    tracks.clear()
                    if (foundTracks != null) {
                        tracks.addAll(foundTracks)
                    }

                    when (errorType) {
                        0 -> {
                            renderState(
                                TracksState.Empty
                            )
                        }
                        1 -> {
                            renderState(
                                TracksState.Error
                            )
                        }
                        else -> {
                            renderState(
                                TracksState.Content(
                                    tracks = tracks
                                )
                            )
                        }
                    }
                }
            })
        }
    }

    fun onTrackClick(track: Track) {
        tracksHistory.removeAll { it.trackId == track.trackId }
        tracksHistory.add(0, track)
        if (tracksHistory.size > NUMBER_OF_SONGS_IN_HISTORY) {
            tracksHistory.removeAt(tracksHistory.size - 1)
        }
        searchHistorySharedPreferences.write(tracksHistory)
    }

    fun showSearchHistory() {
        tracksHistory = searchHistorySharedPreferences.read().toMutableList()
        if (tracksHistory.isNotEmpty()){
            stateSearchHistoryLiveData.postValue(SearchHistoryState.Content(tracksHistory))
        } else {
            clearSearchHistory()
        }
    }

    fun clearSearchHistory() {
        stateSearchHistoryLiveData.postValue(SearchHistoryState.Empty(tracksHistory))
        searchHistorySharedPreferences.clear()
        tracksHistory = searchHistorySharedPreferences.read().toMutableList()
    }

    private fun renderState(state: TracksState) {
        stateTracksLiveData.postValue(state)
    }

}