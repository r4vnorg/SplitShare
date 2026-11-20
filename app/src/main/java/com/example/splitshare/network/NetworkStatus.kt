package com.example.splitshare.network

enum class NStatus {
    SUCCESS, FAILED, PENDING
}

sealed interface NetworkStatus<out T> {
    data class Pending(val startTimestamp: Long) : NetworkStatus<Nothing>
    data class Success<out T>(val data: T) : NetworkStatus<T>
    data class Error(val message: String) : NetworkStatus<Nothing>
}