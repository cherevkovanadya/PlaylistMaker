package com.example.playlistmaker

import Track

data class TracksResponse(
    val resultCount: Int,
    val results: ArrayList<Track>
)
