package com.example.splitshare.data

import com.example.splitshare.domain.model.Person
import kotlinx.coroutines.flow.Flow

/** The local People directory (US5): reusable across groups, separate from the device owner's own Profile. */
interface PersonRepository {

    fun observeAll(): Flow<List<Person>>

    suspend fun findByEmail(email: String): Person?

    /** Returns the existing person if [email] matches one, otherwise creates and returns a new one. */
    suspend fun upsertAndGet(name: String, email: String, phone: String): Person
}
