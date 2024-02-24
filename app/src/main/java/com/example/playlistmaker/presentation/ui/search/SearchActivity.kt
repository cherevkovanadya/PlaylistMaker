package com.example.playlistmaker.presentation.ui.search

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.Creator
import com.example.playlistmaker.presentation.ui.player.PlayerActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.data.SEARCH_HISTORY_KEY
import com.example.playlistmaker.data.SEARCH_HISTORY_PREFERENCES
import com.example.playlistmaker.data.SearchHistoryRepositoryImpl
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.data.dto.TracksSearchResponse
import com.example.playlistmaker.data.network.ITunesApi
import com.example.playlistmaker.databinding.ActivitySearchBinding
import com.example.playlistmaker.domain.api.TracksInteractor
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {

    private val tracksInteractor = Creator.provideTracksInteractor()

    private companion object {
        const val SEARCH_TEXT = "SEARCH_TEXT"
        const val EMPTY = ""
        const val NUMBER_OF_SONGS_IN_HISTORY = 10
        const val CLICK_DEBOUNCE_DELAY = 1000L
        const val SEARCH_DEBOUNCE_DELAY = 2000L
    }

    private var searchText: String = EMPTY

    private lateinit var binding: ActivitySearchBinding

    private var tracks: MutableList<Track> = mutableListOf()
    private var tracksHistory: MutableList<Track> = mutableListOf()

    private val searchHistorySharedPreferences by lazy {
        Creator.provideSearchHistoryInteractor(
            applicationContext
        )
    }

    private val searchAdapter = SearchAdapter { track ->
        if (clickDebounce()) {
            handleTrackClick(track)
        }
    }

    private val searchHistoryAdapter = SearchHistoryAdapter { track ->
        if (clickDebounce()) {
            handleTrackClick(track)
        }
    }

    private var isClickAllowed = true

    private val handler = Handler(Looper.getMainLooper())

    private val searchRunnable = Runnable { searchRequest() }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT, searchText)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadSavedInstanceState(savedInstanceState)

        setupReturnBackImageView()

        setupClearIconImageView()

        setupEditText()

        setupHistorySearchRecyclerView()

        setupTracksRecyclerView()
    }

    override fun onStop() {
        super.onStop()

        val sharedPreferences = getSharedPreferences(SEARCH_HISTORY_PREFERENCES, MODE_PRIVATE)
        sharedPreferences.edit()
            .putString(
                SEARCH_HISTORY_KEY,
                createJsonFromTracksList(searchHistoryAdapter.tracksHistory)
            )
            .apply()
    }

    private fun loadSavedInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            searchText = savedInstanceState.getString(SEARCH_TEXT, "")
            binding.inputEditText.setText(searchText)
        }
    }

    private fun setupReturnBackImageView() {
        binding.returnBackImageView.setOnClickListener {
            finish()
        }
    }

    private fun setupClearIconImageView() {
        binding.clearIconImageView.setOnClickListener {
            binding.inputEditText.setText(EMPTY)
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(binding.inputEditText.windowToken, 0)
            tracks.clear()
            searchAdapter.notifyDataSetChanged()
            binding.placeholderNoSearchResults.isVisible = false
            binding.placeholderServerError.isVisible = false
        }
    }

    private fun handleTrackClick(track: Track) {
        tracksHistory.removeAll { it.trackId == track.trackId }
        tracksHistory.add(0, track)
        if (tracksHistory.size > NUMBER_OF_SONGS_IN_HISTORY) {
            tracksHistory.removeAt(tracksHistory.size - 1)
        }
        searchHistorySharedPreferences.write(tracksHistory)
        val playerIntent = Intent(this, PlayerActivity::class.java)
        playerIntent.putExtra(getString(R.string.track), track)
        startActivity(playerIntent)
    }

    private fun setupClearHistoryButton() {
        binding.clearHistoryButton.setOnClickListener {
            binding.searchHistory.isVisible = false
            searchHistorySharedPreferences.clear()
            tracksHistory.clear()
        }
    }

    private fun setupEditText() {
        binding.inputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && binding.inputEditText.text.isEmpty()) {
                setupHistorySearchRecyclerView()
                binding.searchHistory.isVisible = tracksHistory.isNotEmpty()
                setupClearHistoryButton()
            } else {
                binding.searchHistory.isVisible = false
            }
        }

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.clearIconImageView.visibility = clearButtonVisibility(s)
                searchText = s.toString()
                searchDebounce()
                handleSearchTextChanged(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        }
        binding.inputEditText.addTextChangedListener(simpleTextWatcher)
    }

    private fun handleSearchTextChanged(s: CharSequence?) {
        if (binding.inputEditText.hasFocus() && s?.isEmpty() == true) {
            binding.tracksRecyclerView.isVisible = false
            tracks.clear()
            searchAdapter.notifyDataSetChanged()
            setupHistorySearchRecyclerView()
            binding.searchHistory.isVisible = tracksHistory.isNotEmpty()
            setupClearHistoryButton()
        } else {
            binding.tracksRecyclerView.isVisible = true
            binding.searchHistory.isVisible = false
        }
    }

    private fun setupTracksRecyclerView() {
        searchAdapter.tracks = tracks
        binding.tracksRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.tracksRecyclerView.adapter = searchAdapter
    }

    private fun setupHistorySearchRecyclerView() {
        tracksHistory = searchHistorySharedPreferences.read().toMutableList()
        searchHistoryAdapter.tracksHistory = tracksHistory
        binding.historySearchRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.historySearchRecyclerView.adapter = searchHistoryAdapter
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    private fun createJsonFromTracksList(tracksHistory: MutableList<Track>): String {
        return Gson().toJson(tracksHistory)
    }

    private fun searchRequest() {
        if (searchText.isNotEmpty()) {
            binding.placeholderNoSearchResults.isVisible = false
            binding.placeholderServerError.isVisible = false
            binding.tracksRecyclerView.isVisible = false
            binding.searchProgressBar.isVisible = true

            tracksInteractor.searchTracks(
                searchText,
                object : TracksInteractor.TracksConsumer {
                    override fun consume(foundTracks: List<Track>) {
                        handler.post {
                            binding.searchProgressBar.isVisible = false
                            tracks.clear()
                            binding.placeholderNoSearchResults.isVisible = false
                            binding.placeholderServerError.isVisible = false
                            tracks.addAll(foundTracks)
                            binding.tracksRecyclerView.isVisible = true
                            searchAdapter.notifyDataSetChanged()
                            if (tracks.isEmpty()) {
                                tracks.clear()
                                searchAdapter.notifyDataSetChanged()
                                binding.placeholderServerError.isVisible = false
                                binding.placeholderNoSearchResults.isVisible = true
                            }

                            //else {
//                                tracks.clear()
//                                searchAdapter.notifyDataSetChanged()
//                                binding.placeholderNoSearchResults.isVisible = false
//                                binding.placeholderServerError.isVisible = true
//                            }
                        }
                    }
                })

//                    override fun onFailure(call: Call<TracksSearchResponse>, t: Throwable) {
//                        t.printStackTrace()
//                        tracks.clear()
//                        searchAdapter.notifyDataSetChanged()
//                        binding.placeholderServerError.isVisible = true
//                        binding.searchProgressBar.isVisible = false
//                        binding.placeholderNoSearchResults.isVisible = false
//                        binding.refreshSearch.setOnClickListener {
//                            binding.placeholderServerError.isVisible = false
//                            searchRequest()
//                        }
//                    }
        }
    }


    private fun clearButtonVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }
}