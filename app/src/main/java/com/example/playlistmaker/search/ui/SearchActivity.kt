package com.example.playlistmaker.search.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.player.ui.PlayerActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.search.data.SEARCH_HISTORY_KEY
import com.example.playlistmaker.search.data.SEARCH_HISTORY_PREFERENCES
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.databinding.ActivitySearchBinding
import com.example.playlistmaker.search.ui.states.SearchHistoryState
import com.example.playlistmaker.search.ui.states.TracksState
import com.google.gson.Gson

class SearchActivity : AppCompatActivity() {

    private companion object {
        const val SEARCH_TEXT = "SEARCH_TEXT"
        const val EMPTY = ""
        const val CLICK_DEBOUNCE_DELAY = 1000L
    }

    private var searchText: String = EMPTY

    private lateinit var binding: ActivitySearchBinding
    private lateinit var textWatcher: TextWatcher

    private var isClickAllowed = true

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var viewModel: TracksSearchViewModel

    private val searchAdapter = SearchAdapter { track ->
        if (clickDebounce()) {
            viewModel.onTrackClick(track)
            startPlayerIntent(track)
        }
    }

    private val searchHistoryAdapter = SearchHistoryAdapter { track ->
        if (clickDebounce()) {
            startPlayerIntent(track)
        }
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

        viewModel = ViewModelProvider(
            this,
            TracksSearchViewModel.getViewModelFactory()
        )[TracksSearchViewModel::class.java]

        viewModel.getStateTracksLiveData().observe(this) {
            renderTracksState(it)
        }

        viewModel.getStateSearchHistoryLiveData().observe(this) {
            renderSearchHistoryState(it)
        }

        loadSavedInstanceState(savedInstanceState)

        setupReturnBackImageView()

        setupRefreshButton()

        setupClearIconImageView()

        setupEditText()

        setupClearHistoryButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.inputEditText.removeTextChangedListener(textWatcher)
    }

    private fun startPlayerIntent(track: Track) {
        val playerIntent = Intent(this, PlayerActivity::class.java)
        playerIntent.putExtra(getString(R.string.track), track)
        startActivity(playerIntent)
    }

    private fun renderTracksState(state: TracksState) {
        when (state) {
            is TracksState.Content -> showContent(state.tracks)
            is TracksState.Empty -> showEmpty()
            is TracksState.Loading -> showLoading()
            is TracksState.Error -> showError()
        }
    }

    private fun renderSearchHistoryState(state: SearchHistoryState) {
        when (state) {
            is SearchHistoryState.Content -> {
                if (state.tracks.isNotEmpty()) {
                    binding.searchHistory.isVisible = true
                    setupHistorySearchRecyclerView(state.tracks)
                }
            }

            is SearchHistoryState.Empty -> {
                binding.searchHistory.isVisible = false
                setupHistorySearchRecyclerView(state.tracks)
            }
        }
    }

    private fun showLoading() {
        binding.placeholderNoSearchResults.isVisible = false
        binding.placeholderServerError.isVisible = false
        binding.tracksRecyclerView.isVisible = false
        binding.searchProgressBar.isVisible = true
    }

    private fun showContent(newTrackList: List<Track>) {
        binding.placeholderNoSearchResults.isVisible = false
        binding.placeholderServerError.isVisible = false
        binding.tracksRecyclerView.isVisible = true
        binding.searchProgressBar.isVisible = false
        searchAdapter.tracks.clear()
        searchAdapter.tracks.addAll(newTrackList)
        searchAdapter.notifyDataSetChanged()
    }

    private fun showEmpty() {
        binding.placeholderNoSearchResults.isVisible = true
        binding.placeholderServerError.isVisible = false
        binding.tracksRecyclerView.isVisible = false
        binding.searchProgressBar.isVisible = false
    }

    private fun showError() {
        binding.placeholderNoSearchResults.isVisible = false
        binding.placeholderServerError.isVisible = true
        binding.tracksRecyclerView.isVisible = false
        binding.searchProgressBar.isVisible = false
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

    private fun setupRefreshButton() {
        binding.refreshSearch.isVisible = false
        viewModel.searchRequest(searchText)
    }

    private fun setupClearIconImageView() {
        binding.clearIconImageView.setOnClickListener {
            binding.inputEditText.setText(EMPTY)
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(binding.inputEditText.windowToken, 0)
            searchAdapter.notifyDataSetChanged()
            binding.placeholderNoSearchResults.isVisible = false
            binding.placeholderServerError.isVisible = false
        }
    }

    private fun setupClearHistoryButton() {
        binding.clearHistoryButton.setOnClickListener {
            viewModel.clearSearchHistory()
        }
    }

    private fun setupEditText() {
        binding.inputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && binding.inputEditText.text.isEmpty()) {
                viewModel.showSearchHistory()
            } else {
                binding.searchHistory.isVisible = false
            }
        }

        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.clearIconImageView.visibility = clearButtonVisibility(s)
                searchText = s.toString()
                handleSearchTextChanged(s)
                viewModel.searchDebounce(searchText)
            }

            override fun afterTextChanged(s: Editable?) {}
        }
        binding.inputEditText.addTextChangedListener(textWatcher)
        setupTracksRecyclerView()
    }

    private fun handleSearchTextChanged(s: CharSequence?) {
        if (binding.inputEditText.hasFocus() && s?.isEmpty() == true) {
            viewModel.showSearchHistory()
            binding.tracksRecyclerView.isVisible = false
            binding.placeholderNoSearchResults.isVisible = false
            binding.placeholderServerError.isVisible = false
        } else {
            searchAdapter.tracks.clear()
            searchAdapter.notifyDataSetChanged()
            binding.placeholderNoSearchResults.isVisible = false
            binding.placeholderServerError.isVisible = false
            binding.tracksRecyclerView.isVisible = true
            binding.searchHistory.isVisible = false
        }
    }

    private fun setupTracksRecyclerView() {
        binding.tracksRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.tracksRecyclerView.adapter = searchAdapter
    }

    private fun setupHistorySearchRecyclerView(tracksHistory: MutableList<Track>) {
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

    private fun createJsonFromTracksList(tracksHistory: MutableList<Track>): String {
        return Gson().toJson(tracksHistory)
    }

    private fun clearButtonVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }
}