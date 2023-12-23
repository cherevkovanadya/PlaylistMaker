package com.example.playlistmaker

import SearchAdapter
import Track
import android.content.Intent
import android.os.Bundle
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
    private var searchText: String = ""
    private lateinit var binding: ActivitySearchBinding
    private val iTunesBaseUrl: String = "https://itunes.apple.com"
    private val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC)
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()
    private val retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(iTunesBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val iTunesService = retrofit.create(ITunesApi::class.java)
    private var tracks: MutableList<Track> = mutableListOf()
    private var tracksHistory: MutableList<Track> = mutableListOf()
    private var searchAdapter = SearchAdapter(tracks, { position -> onSearchListItemClick(position) })
    private var searchHistoryAdapter = SearchHistoryAdapter(tracksHistory, { position -> onHistoryListItemClick(position) })

    private companion object {
        const val SEARCH_TEXT = "SEARCH_TEXT"
        const val EMPTY = ""
        const val NUMBER_OF_SONGS_IN_HISTORY = 10
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
            binding.inputEditText.setText("")
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
                searchHistoryAdapter = SearchHistoryAdapter(tracksHistory, { position -> onHistoryListItemClick(position) })
                binding.historySearchRecyclerView.adapter = searchHistoryAdapter
                binding.searchHistory.isVisible = tracksHistory.isNotEmpty()
                binding.clearHistoryButton.setOnClickListener {
                    binding.searchHistory.isVisible = false
                    searchHistory.clear()
                    tracksHistory = searchHistory.read().toMutableList()
                    searchHistoryAdapter = SearchHistoryAdapter(tracksHistory, { position -> onHistoryListItemClick(position) })
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
                if (binding.inputEditText.hasFocus() && s?.isEmpty() == true) {
                    binding.tracksRecyclerView.isVisible = false
                    tracks.clear()
                    searchAdapter.notifyDataSetChanged()
                    tracksHistory = searchHistory.read().toMutableList()
                    if (tracksHistory.isNotEmpty()) {
                        searchHistoryAdapter = SearchHistoryAdapter(tracksHistory, { position -> onHistoryListItemClick(position) })
                        binding.historySearchRecyclerView.adapter = searchHistoryAdapter
                        binding.searchHistory.isVisible = true
                    } else {
                        binding.searchHistory.isVisible = false
                    }
                    binding.clearHistoryButton.setOnClickListener {
                        binding.searchHistory.isVisible = false
                        searchHistory.clear()
                        tracksHistory = searchHistory.read().toMutableList()
                        searchHistoryAdapter = SearchHistoryAdapter(tracksHistory, { position -> onHistoryListItemClick(position) })
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

        searchAdapter = SearchAdapter(tracks, { position -> onSearchListItemClick(position) })
        searchAdapter.tracks = tracks
        binding.tracksRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.tracksRecyclerView.adapter = searchAdapter

        binding.inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchTracks()
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

    private fun onHistoryListItemClick(position: Int) {
        val playerIntent = Intent(this, PlayerActivity::class.java)
        startActivity(playerIntent)
    }

    private fun onSearchListItemClick(position: Int) {
        val sharedPreferences = getSharedPreferences(SEARCH_HISTORY_PREFERENCES, MODE_PRIVATE)
        val searchHistory = SearchHistory(sharedPreferences)
        tracksHistory.removeIf { it.trackId == tracks[position].trackId }
        tracksHistory.add(0, tracks[position])
        if (tracksHistory.size > NUMBER_OF_SONGS_IN_HISTORY) {
            tracksHistory.removeAt(tracksHistory.size - 1)
        }
        searchHistory.write(tracksHistory)
        val playerIntent = Intent(this, PlayerActivity::class.java)
        startActivity(playerIntent)
    }

    private fun createJsonFromTracksList(tracksHistory: MutableList<Track>): String {
        return Gson().toJson(tracksHistory)
    }

    private fun createTracksListFromJson(json: String): Array<Track> {
        return Gson().fromJson(json, Array<Track>::class.java)
    }

    private fun searchTracks() {
        iTunesService.getTracks(binding.inputEditText.text.toString())
            .enqueue(object : Callback<TracksResponse> {
                override fun onResponse(
                    call: Call<TracksResponse>,
                    response: Response<TracksResponse>
                ) {
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
                        searchTracks()
                    }
                }
            })
    }

    private fun clearButtonVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }
}