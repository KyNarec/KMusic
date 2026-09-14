package com.kynarec.kmusic.service.innertube

sealed interface NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>
    sealed interface Failure : NetworkResult<Nothing> {
        object NetworkError : Failure
        object ParsingError : Failure
        object NotFound : Failure
    }
}