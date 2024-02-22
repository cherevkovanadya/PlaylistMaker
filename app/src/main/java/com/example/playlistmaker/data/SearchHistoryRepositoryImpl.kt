package com.example.playlistmaker.data

import android.content.Context
import android.content.SharedPreferences
import com.example.playlistmaker.domain.api.SearchHistoryRepository
import com.example.playlistmaker.domain.models.Track
import com.google.gson.Gson

const val SEARCH_HISTORY_PREFERENCES = "search_history_preferences"
const val SEARCH_HISTORY_KEY = "key_for_search_history"

class SearchHistoryRepositoryImpl(
    private val context: Context
) : SearchHistoryRepository {

    private val sharedPreferences =
        context.getSharedPreferences(SEARCH_HISTORY_PREFERENCES, Context.MODE_PRIVATE)

    override fun read(): Array<Track> {
        val json = sharedPreferences.getString(SEARCH_HISTORY_KEY, null) ?: return emptyArray()
        return Gson().fromJson(json, Array<Track>::class.java)
    }

    override fun write(searchHistory: MutableList<Track>) {
        val json = Gson().toJson(searchHistory)
        sharedPreferences.edit()
            .putString(SEARCH_HISTORY_KEY, json)
            .apply()
    }

    override fun clear() {
        sharedPreferences.edit()
            .clear()
            .apply()
    }
}