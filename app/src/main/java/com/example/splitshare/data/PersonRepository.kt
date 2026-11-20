package com.example.splitshare.data

import com.example.splitshare.domain.model.Person
import kotlinx.coroutines.flow.Flow

/** The local People directory (US5): reusable across groups, separate from the device owner's own Profile. */
interface PersonRepository {
    suspend fun findById(id: String): Person?
    suspend fun findByEmail(email: String): Person?
}
