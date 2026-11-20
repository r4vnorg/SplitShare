package com.example.splitshare.network

import android.util.Log
import com.example.splitshare.data.local.toDomain
import com.example.splitshare.data.local.toEntity
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.Unit
import kotlin.time.Duration.Companion.milliseconds

class NetworkClient(
    private val baseUrl: String = "http://10.0.2.2:8080/"
) {
    private val retrofit by lazy {

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .client(client)
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: NetworkAPI by lazy { retrofit.create(NetworkAPI::class.java) }


    /* Simple error handler wrapper for requests */
    suspend inline fun <T> executeRequest(
        crossinline fetch: suspend (id: String) -> Response<T>,
        reqID: String = UUID.randomUUID().toString()
        ) : T {

        val res = fetch(reqID)
        if (res.isSuccessful)
            return res.body()!!
        else
            res.errorBody()?.string()?.let { throw Exception(it) }

        throw Exception(res.code().toString())
    }

    /**
     * Function to fetch a single entity, either from cache or if null, from network
     */
    suspend inline fun <T> networkBoundEntity(
        crossinline cache: suspend () -> T?,
        crossinline fetch: suspend () -> T,
        crossinline save: suspend (T) -> Unit,
    ): T? {
         cache()?.let { return it }
         try {
             val entity = fetch()
             save(entity)
             return null
         } catch (e: Exception) {
             return null
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
    fun <T> networkBoundContentWithPolling(
        cache: suspend () -> Flow<T>,
        fetch: suspend () -> T,
        save: suspend (T) -> Unit,
        pollIntervalMillis: Long = 30_000,
    ): Flow<T> = channelFlow {

        // Emit cached data immediately so the UI is never blank
        launch {
            cache().collect { value -> send(value) }
        }

        // Automatic polling loop — continues until the flow is cancelled (ViewModel cleared)
        while (true) {
            try {
                val remote = fetch()
                save(remote)
            } catch (e: Exception) {
                e.message?.let { Log.e("NBRPoll", it) } //Do not care about errors here, just log them
            }
            delay(pollIntervalMillis.milliseconds)
        }
    }
}
