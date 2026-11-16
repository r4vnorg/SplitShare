package com.example.splitshare.data

import com.example.splitshare.data.local.PersonDao
import com.example.splitshare.data.local.entity.PersonEntity
import com.example.splitshare.data.local.toDomain
import com.example.splitshare.domain.model.Person
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class RoomPersonRepository(private val personDao: PersonDao) : PersonRepository {

    override fun observeAll(): Flow<List<Person>> =
        personDao.observeAll().map { people -> people.map { it.toDomain() } }

    override suspend fun findByEmail(email: String): Person? =
        personDao.findByEmail(email.normalized())?.toDomain()

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
