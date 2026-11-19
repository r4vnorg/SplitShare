package com.example.splitshare.network

import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds

class NetworkClient(
    private val baseUrl: String = "http://10.0.2.2:8080/",
) {
    private val retrofit by lazy {

        val retryInterceptor = RetryInterceptor(maxRetries = 3)
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(retryInterceptor)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .client(client)
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: NetworkAPI by lazy {
        retrofit.create(NetworkAPI::class.java)
    }

    /**
     * Local-first flow: emits cached data immediately, then fetches from the network once.
     * The existing lightweight variant — no polling.
     */
    inline fun <Cache, Network> networkBoundResource(
        crossinline cache: () -> Flow<Cache>,
        crossinline fetch: suspend () -> Network?,
        crossinline saveFetchResult: suspend (Network) -> Unit,
    ): Flow<Cache> = flow {

        emitAll(cache())

        try {
            Log.w("ASDASD", "GOT HERE")
            val remote = fetch() ?: return@flow
            saveFetchResult(remote)
        } catch (_: Exception) {
            // Network failure is silent; the UI already has cached data.
        }
    }

    /**
     * Local-first flow with automatic polling.
     *
     * Lifecycle:
     * 1. Emits [NetworkBoundResource.Loading] while the initial network fetch starts.
     * 2. Emits cached data immediately so the UI is never blank.
     * 3. On a successful network fetch: saves to cache, emits [NetworkBoundResource.Success].
     * 4. On a network failure: emits [NetworkBoundResource.Stale] with cached data (or [Error]
     *    if there is no cache), so the UI can show "data may be outdated".
     * 5. Every [pollIntervalMillis], repeats steps 3-4 in the background.
     */
    fun <Cache, Network> networkBoundResourceWithPolling(
        cache: () -> Flow<Cache>,
        fetch: suspend () -> Network,
        save: suspend (Network) -> Unit,
        pollIntervalMillis: Long = 30_000,
    ): Flow<NetworkBoundResource<Cache>> = flow {

        emit(NetworkBoundResource.Loading)

        var lastCached: Any? = null

        // Emit cached data immediately so the UI is never blank
        cache().take(1).collect { value ->
            lastCached = value
            @Suppress("UNCHECKED_CAST")
            emit(NetworkBoundResource.Success(value as Cache))
        }

        var lastError: String? = null

        // Initial fetch — RetryInterceptor handles transient failures automatically
        try {
            val remote = fetch()
            save(remote)
            cache().take(1).collect { value ->
                lastCached = value
                @Suppress("UNCHECKED_CAST")
                emit(NetworkBoundResource.Success(value as Cache))
            }
            lastError = null
        } catch (e: Exception) {
            lastError = e.localizedMessage ?: "Network error"
            @Suppress("UNCHECKED_CAST")
            lastCached?.let { emit(NetworkBoundResource.Stale(it as Cache, lastError = lastError)) }
                ?: emit(NetworkBoundResource.Error(lastError))
        }

        // Automatic polling loop — continues until the flow is cancelled (ViewModel cleared)
        while (true) {
            delay(pollIntervalMillis.milliseconds)
            try {
                val remote = fetch()
                save(remote)
                cache().take(1).collect { value ->
                    lastCached = value
                    @Suppress("UNCHECKED_CAST")
                    emit(NetworkBoundResource.Success(value as Cache))
                }
                lastError = null
            } catch (e: Exception) {
                lastError = e.localizedMessage ?: "Polling failed"
                @Suppress("UNCHECKED_CAST")
                lastCached?.let { emit(NetworkBoundResource.Stale(it as Cache, lastError = lastError)) }
            }
        }
    }
}
