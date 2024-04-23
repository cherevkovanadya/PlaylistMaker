package com.example.playlistmaker.util

sealed class Resource<T>(
    val data: T? = null,
    val errorType: Int? = null,
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(errorType: Int, data: T? = null) : Resource<T>(data, errorType)
}