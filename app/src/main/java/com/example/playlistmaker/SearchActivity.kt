package com.example.playlistmaker

import SearchAdapter
import Track
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
    private var tracks = ArrayList<Track>()
    private lateinit var searchAdapter: SearchAdapter

    private companion object {
        const val SEARCH_TEXT = "SEARCH_TEXT"
        const val EMPTY = ""
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

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.clearIconImageView.visibility = clearButtonVisibility(s)
                searchText = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }
        binding.inputEditText.addTextChangedListener(simpleTextWatcher)

        searchAdapter = SearchAdapter(tracks)
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