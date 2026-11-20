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

    override suspend fun findByEmail(email: String): Person? =
        networkClient.networkBoundEntity(
            cache = suspend { personDao.findByEmail(email.normalized())?.toDomain() },
            fetch = { networkClient.api.fetchPerson("id") }, //TODO:
            save  = { person -> personDao.insert(person.toEntity()) }
        )
    override suspend fun findById(id: String) : Person? =
        networkClient.networkBoundEntity(
            cache = suspend { personDao.findById(id.normalized())?.toDomain() },
            fetch = { networkClient.api.fetchPerson(id) },
            save  = { person -> personDao.insert(person.toEntity()) }
        )

    private fun String.normalized(): String = trim().lowercase()
}
