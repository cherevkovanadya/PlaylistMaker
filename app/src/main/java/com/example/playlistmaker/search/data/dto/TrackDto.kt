package com.example.playlistmaker.search.data.dto

data class TrackDto(
    val trackName: String? = "",
    val artistName: String? = "",
    val trackTimeMillis: Int = 0,
    val artworkUrl100: String? = "",
    val trackId: Int = 0,
    val collectionName: String? = "",
    val releaseDate: String? = "",
    val primaryGenreName: String? = "",
    val country: String? = "",
    val previewUrl: String? = "",
)