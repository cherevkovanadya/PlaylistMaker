package com.example.playlistmaker.search.ui

import android.app.Application
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.playlistmaker.R
import com.example.playlistmaker.search.domain.api.TracksInteractor
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.search.ui.states.SearchHistoryState
import com.example.playlistmaker.search.ui.states.TracksState
import com.example.playlistmaker.util.Creator

class TracksSearchViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val NUMBER_OF_SONGS_IN_HISTORY = 10
        const val SEARCH_DEBOUNCE_DELAY = 2000L
        private val SEARCH_REQUEST_TOKEN = Any()

        fun getViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                TracksSearchViewModel(this[APPLICATION_KEY] as Application)
            }
        }
    }

    private val tracksInteractor = Creator.provideTracksInteractor(getApplication())

    private val searchHistorySharedPreferences by lazy {
        Creator.provideSearchHistoryInteractor(
            getApplication<Application>().applicationContext
        )
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
                    tracks = mutableListOf()
                    if (foundTracks != null) {
                        tracks.addAll(foundTracks)
                    }

                    when (errorType) {
                        0 -> {
                            renderState(
                                TracksState.Empty(
                                    message = getApplication<Application>().getString(R.string.no_search_results),
                                )
                            )
                        }
                        1 -> {
                            renderState(
                                TracksState.Error(
                                    errorMessage = getApplication<Application>().getString(R.string.server_error)
                                )
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
        stateSearchHistoryLiveData.postValue(SearchHistoryState.Content(tracksHistory))
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