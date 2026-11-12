package com.example.splitshare.network

import com.example.splitshare.domain.model.Profile
import retrofit2.http.GET
import retrofit2.http.Path

interface NetworkAPI {

    @GET("/profiles/{id}")
    suspend fun fetchProfile(@Path("id") id: String): Profile

}