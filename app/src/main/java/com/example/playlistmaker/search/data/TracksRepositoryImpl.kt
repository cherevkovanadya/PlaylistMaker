package com.example.playlistmaker.search.data

import com.example.playlistmaker.search.data.dto.TracksSearchRequest
import com.example.playlistmaker.search.data.dto.TracksSearchResponse
import com.example.playlistmaker.search.domain.api.TracksRepository
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.util.Resource
import java.text.SimpleDateFormat
import java.util.Locale

class TracksRepositoryImpl(private val networkClient: NetworkClient) : TracksRepository {

    companion object {
        const val ERROR_EMPTY_DATA = 0
        const val ERROR_CONNECTION = 1
    }

    override fun searchTracks(expression: String): Resource<List<Track>> {
        val response = networkClient.doRequest(TracksSearchRequest(expression))
        return when (response.resultCode) {

            200 -> {
                if ((response as TracksSearchResponse).results.isEmpty()) {
                    Resource.Error(ERROR_EMPTY_DATA)
                } else {
                    Resource.Success(response.results.map {
                        Track(
                            it.trackName,
                            it.artistName,
                            SimpleDateFormat(
                                "mm:ss",
                                Locale.getDefault()
                            ).format(it.trackTimeMillis),
                            it.artworkUrl100,
                            it.trackId,
                            it.collectionName,
                            it.releaseDate?.substring(0, 4),
                            it.primaryGenreName,
                            it.country,
                            it.previewUrl
                        )
                    })
                }
            }

            else -> {
                Resource.Error(ERROR_CONNECTION)
            }
        }
    }
}