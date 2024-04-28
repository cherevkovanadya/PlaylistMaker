package com.example.playlistmaker.search.data.dto

data class TracksSearchResponse(
    val results: MutableList<TrackDto>
) : Response()
