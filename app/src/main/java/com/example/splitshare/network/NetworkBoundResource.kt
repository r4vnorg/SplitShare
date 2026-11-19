package com.example.splitshare.network

/**
 * Result of a cache-first network operation with automatic polling.
 *
 * - [Loading] – initial network fetch in progress (cache may already have data).
 * - [Success] – fresh data from the network, saved to cache.
 * - [Stale] – cached data is available but the latest poll failed; [lastError] explains why.
 * - [Error] – no cached data and the network fetch failed.
 */
sealed interface NetworkBoundResource<out T> {
    object Loading : NetworkBoundResource<Nothing>
    data class Success<out T>(val data: T, val timestamp: Long = System.currentTimeMillis()) : NetworkBoundResource<T>
    data class Stale<out T>(
        val data: T,
        val timestamp: Long = System.currentTimeMillis(),
        val lastError: String? = null,
    ) : NetworkBoundResource<T>
    data class Error(val message: String) : NetworkBoundResource<Nothing>
}

sealed interface NetworkStatus<out T> {

    data class Success<out T>(val data: T) : NetworkStatus<T>
    data class Error(val message: String) : NetworkStatus<Nothing>
}