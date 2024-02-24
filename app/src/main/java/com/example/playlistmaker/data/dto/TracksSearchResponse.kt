package com.example.playlistmaker.data.dto

data class TracksSearchResponse(
    val results: MutableList<TrackDto>
) : Response()
