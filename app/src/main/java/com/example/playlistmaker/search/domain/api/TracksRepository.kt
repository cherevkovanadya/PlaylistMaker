package com.example.playlistmaker.search.domain.api

import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.util.Resource
import javax.xml.xpath.XPathExpression

interface TracksRepository {
    fun searchTracks(expression: String): Resource<List<Track>>
}