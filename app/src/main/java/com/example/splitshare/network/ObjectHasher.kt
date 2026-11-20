package com.example.splitshare.network

import java.security.MessageDigest
import com.google.gson.Gson

object ObjectHasher {
    private val gson = Gson()

    fun sha256(obj: Any): String {
        val json = gson.toJson(obj)
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(json.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}