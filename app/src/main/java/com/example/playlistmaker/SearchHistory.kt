package com.example.playlistmaker

import android.content.SharedPreferences
import com.google.gson.Gson

const val SEARCH_HISTORY_PREFERENCES = "search_history_preferences"
const val SEARCH_HISTORY_KEY = "key_for_search_history"

class SearchHistory(
    private val sharedPreferences: SharedPreferences
) {
    fun read(): Array<Track> {
        val json = sharedPreferences.getString(SEARCH_HISTORY_KEY, null) ?: return emptyArray()
        return Gson().fromJson(json, Array<Track>::class.java)
    }

    fun write(searchHistory: MutableList<Track>) {
        val json = Gson().toJson(searchHistory)
        sharedPreferences.edit()
            .putString(SEARCH_HISTORY_KEY, json)
            .apply()
    }

    fun clear() {
        sharedPreferences.edit()
            .clear()
            .apply()
    }
}