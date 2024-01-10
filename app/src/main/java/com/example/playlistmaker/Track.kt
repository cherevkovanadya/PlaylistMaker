package com.example.playlistmaker

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize

@Parcelize
data class Track(
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
): Parcelable