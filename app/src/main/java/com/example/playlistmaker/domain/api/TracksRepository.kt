package com.example.playlistmaker.domain.api

import com.example.playlistmaker.domain.models.Track
import javax.xml.xpath.XPathExpression

interface TracksRepository {
    fun searchTracks(expression: String): List<Track>
}