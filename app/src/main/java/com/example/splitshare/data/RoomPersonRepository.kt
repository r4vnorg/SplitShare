package com.example.splitshare.data

import android.util.Log
import com.example.splitshare.data.local.PersonDao
import com.example.splitshare.data.local.entity.PersonEntity
import com.example.splitshare.data.local.toDomain
import com.example.splitshare.data.local.toEntity
import com.example.splitshare.domain.model.Person
import com.example.splitshare.network.NetworkClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.UUID

class RoomPersonRepository(private val personDao: PersonDao,
                           private val networkClient: NetworkClient) : PersonRepository {

    override fun observeAll(): Flow<List<Person>> =
        personDao.observeAll().map { people -> people.map { it.toDomain() } }

    override suspend fun findByEmail(email: String): Person? =
        personDao.findByEmail(email.normalized())?.toDomain()

    override suspend fun findById(id: String) : Person? {
        personDao.findById(id.normalized()).first()?.let{ return it.toDomain() }

        try {
            val person = networkClient.api.fetchPerson(id)
            personDao.insert(person.toEntity())
            return person
        } catch (e: Exception) {
            return null //Error or not found - todo: retry
        }
    }

    override suspend fun upsertAndGet(name: String, email: String, phone: String): Person {
        val normalizedEmail = email.normalized()
        personDao.findByEmail(normalizedEmail)?.let { return it.toDomain() }
        val person = PersonEntity(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            email = normalizedEmail,
            phone = phone.trim(),
        )
        personDao.insert(person)
        return person.toDomain()
    }

    private fun String.normalized(): String = trim().lowercase()
}
