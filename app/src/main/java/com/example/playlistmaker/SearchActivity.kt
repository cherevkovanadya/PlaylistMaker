package com.example.playlistmaker

import SearchAdapter
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.databinding.ActivitySearchBinding
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {
    private var searchText: String = EMPTY

    private lateinit var binding: ActivitySearchBinding

    private val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC)
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(ITUNES_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val iTunesService = retrofit.create(ITunesApi::class.java)

    private var tracks: MutableList<Track> = mutableListOf()
    private var tracksHistory: MutableList<Track> = mutableListOf()

    private val searchAdapter = SearchAdapter { track ->
        if (clickDebounce()) {
            tracksHistory.removeAll { it.trackId == track.trackId }
        }
        tracksHistory.add(0, track)
        if (tracksHistory.size > NUMBER_OF_SONGS_IN_HISTORY) {
            tracksHistory.removeAt(tracksHistory.size - 1)
        }
        val playerIntent = Intent(this, PlayerActivity::class.java)
        playerIntent.putExtra("track", track)
        startActivity(playerIntent)
    }
    private val searchHistoryAdapter = SearchHistoryAdapter {
        if (clickDebounce()) {
            val playerIntent = Intent(this, PlayerActivity::class.java)
            playerIntent.putExtra("track", it)
            startActivity(playerIntent)
        }
    }

    private var isClickAllowed = true
    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable { searchRequest() }

    private companion object {
        const val SEARCH_TEXT = "SEARCH_TEXT"
        const val EMPTY = ""
        const val ITUNES_BASE_URL = "https://itunes.apple.com"
        const val NUMBER_OF_SONGS_IN_HISTORY = 10
        const val CLICK_DEBOUNCE_DELAY = 1000L
        const val SEARCH_DEBOUNCE_DELAY = 2000L
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT, searchText)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState != null) {
            searchText = savedInstanceState.getString(SEARCH_TEXT, EMPTY)
            binding.inputEditText.setText(searchText)
        }

        binding.returnBackImageView.setOnClickListener {
            finish()
        }

        binding.clearIconImageView.setOnClickListener {
            binding.inputEditText.setText(EMPTY)
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(binding.inputEditText.windowToken, 0)
            tracks.clear()
            searchAdapter.notifyDataSetChanged()
            binding.placeholderNoSearchResults.isVisible = false
            binding.placeholderServerError.isVisible = false
        }

        val sharedPreferences = getSharedPreferences(SEARCH_HISTORY_PREFERENCES, MODE_PRIVATE)
        val searchHistory = SearchHistory(sharedPreferences)

        binding.inputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && binding.inputEditText.text.isEmpty()) {
                tracksHistory = searchHistory.read().toMutableList()
                binding.historySearchRecyclerView.adapter = searchHistoryAdapter
                binding.searchHistory.isVisible = tracksHistory.isNotEmpty()
                binding.clearHistoryButton.setOnClickListener {
                    binding.searchHistory.isVisible = false
                    searchHistory.clear()
                    tracksHistory = searchHistory.read().toMutableList()
                    binding.historySearchRecyclerView.adapter = searchHistoryAdapter
                }
            } else {
                binding.searchHistory.isVisible = false
            }
        }

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.clearIconImageView.visibility = clearButtonVisibility(s)
                searchText = s.toString()
                searchDebounce()
                if (binding.inputEditText.hasFocus() && s?.isEmpty() == true) {
                    binding.tracksRecyclerView.isVisible = false
                    tracks.clear()
                    searchAdapter.notifyDataSetChanged()
                    tracksHistory = searchHistory.read().toMutableList()
                    if (tracksHistory.isNotEmpty()) {
                        binding.historySearchRecyclerView.adapter = searchHistoryAdapter
                        binding.searchHistory.isVisible = true
                    } else {
                        binding.searchHistory.isVisible = false
                    }
                    binding.clearHistoryButton.setOnClickListener {
                        binding.searchHistory.isVisible = false
                        searchHistory.clear()
                        tracksHistory = searchHistory.read().toMutableList()
                        binding.historySearchRecyclerView.adapter = searchHistoryAdapter
                    }
                } else {
                    binding.tracksRecyclerView.isVisible = true
                    binding.searchHistory.isVisible = false
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }
        binding.inputEditText.addTextChangedListener(simpleTextWatcher)

        binding.historySearchRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        searchAdapter.tracks = tracks
        binding.tracksRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.tracksRecyclerView.adapter = searchAdapter

        binding.inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchRequest()
            }
            false
        }
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

    private fun createTracksListFromJson(json: String): Array<Track> {
        return Gson().fromJson(json, Array<Track>::class.java)
    }

    private fun searchRequest() {
        if (searchText.isNotEmpty()) {
            binding.placeholderNoSearchResults.isVisible = false
            binding.placeholderServerError.isVisible = false
            binding.tracksRecyclerView.isVisible = false
            binding.searchProgressBar.isVisible = true
            iTunesService.getTracks(binding.inputEditText.text.toString())
                .enqueue(object : Callback<TracksResponse> {
                    override fun onResponse(
                        call: Call<TracksResponse>,
                        response: Response<TracksResponse>
                    ) {
                        binding.searchProgressBar.isVisible = false
                        when (response.code()) {
                            200 -> {
                                if (response.body()?.results?.isNotEmpty() == true) {
                                    tracks.clear()
                                    binding.placeholderNoSearchResults.isVisible = false
                                    binding.placeholderServerError.isVisible = false
                                    tracks.addAll(response.body()?.results!!)
                                    searchAdapter.notifyDataSetChanged()
                                } else {
                                    tracks.clear()
                                    searchAdapter.notifyDataSetChanged()
                                    binding.placeholderServerError.isVisible = false
                                    binding.placeholderNoSearchResults.isVisible = true
                                }
                            }

                            else -> {
                                tracks.clear()
                                searchAdapter.notifyDataSetChanged()
                                binding.placeholderNoSearchResults.isVisible = false
                                binding.placeholderServerError.isVisible = true
                            }
                        }
                    }

                    override fun onFailure(call: Call<TracksResponse>, t: Throwable) {
                        t.printStackTrace()
                        tracks.clear()
                        searchAdapter.notifyDataSetChanged()
                        binding.placeholderServerError.isVisible = true
                        binding.placeholderNoSearchResults.isVisible = false
                        binding.refreshSearch.setOnClickListener {
                            binding.placeholderServerError.isVisible = false
                            searchRequest()
                        }
                    }
                })
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