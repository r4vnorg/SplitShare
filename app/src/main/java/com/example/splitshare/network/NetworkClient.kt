package com.example.splitshare.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NetworkClient {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://example.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api : NetworkAPI by lazy {
        retrofit.create(NetworkAPI::class.java)
    }

    inline fun <Cache, Network> networkBoundResource(
        crossinline cache: () -> Flow<Cache>,
        crossinline fetch: suspend () -> Network,
        crossinline saveFetchResult: suspend (Network) -> Unit
    ): Flow<Cache> = flow {

        emitAll(cache())

        try {
            val remote = fetch()
            saveFetchResult(remote)
        } catch (_: Exception) { }
    }
}