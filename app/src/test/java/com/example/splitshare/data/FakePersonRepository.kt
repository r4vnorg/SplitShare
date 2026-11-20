package com.example.splitshare.data

import com.example.splitshare.domain.model.Person
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory test double for ViewModel tests; avoids needing a real Room database. */
class FakePersonRepository : PersonRepository {

    val people = mutableListOf<Person>()
    private val peopleFlow = MutableStateFlow<List<Person>>(emptyList())

    override fun observeAll(): Flow<List<Person>> = peopleFlow
    override suspend fun findById(id: String): Person? {
        TODO("Not yet implemented")
    }

    override suspend fun findByEmail(email: String): Person? =
        people.find { it.email.equals(email.trim(), ignoreCase = true) }

    override suspend fun upsertAndGet(name: String, email: String, phone: String): Person {
        findByEmail(email)?.let { return it }
        val person = Person(id = "person-${people.size}", name = name.trim(), email = email.trim().lowercase(), phone = phone.trim())
        people += person
        peopleFlow.value = people.toList()
        return person
    }
}
