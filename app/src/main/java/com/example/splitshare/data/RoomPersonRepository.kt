package com.example.splitshare.data

import com.example.splitshare.data.local.PersonDao
import com.example.splitshare.data.local.toDomain
import com.example.splitshare.data.local.toEntity
import com.example.splitshare.domain.model.Person
import com.example.splitshare.network.NetworkClient

class RoomPersonRepository(private val personDao: PersonDao,
                           private val networkClient: NetworkClient) : PersonRepository {

    override suspend fun findByEmail(email: String): Person? =
        networkClient.networkBoundEntity(
            cache = suspend { personDao.findByEmail(email.normalized())?.toDomain() },
            fetch = { networkClient.api.fetchPerson("id") }, //TODO:
            save  = { person -> personDao.upsert(person.toEntity()) }
        )
    override suspend fun findById(id: String) : Person? =
        networkClient.networkBoundEntity(
            cache = suspend { personDao.findById(id.normalized())?.toDomain() },
            fetch = { networkClient.api.fetchPerson(id) },
            save  = { person -> personDao.upsert(person.toEntity()) }
        )

    private fun String.normalized(): String = trim().lowercase()
}
