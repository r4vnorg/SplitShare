package com.example.splitshare.data

import android.util.Log
import com.example.splitshare.data.local.AuthDao
import com.example.splitshare.data.local.ExpenseDao
import com.example.splitshare.data.local.ExpenseSplitDao
import com.example.splitshare.data.local.PersonDao
import com.example.splitshare.data.sync.SyncScheduler
import com.example.splitshare.domain.model.Person
import com.example.splitshare.network.NetworkBoundResource
import com.example.splitshare.network.NetworkClient
import com.example.splitshare.network.NetworkStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


data class AuthRequest (val email: String)
data class RegisterRequest (val name: String, val email: String, val phone: String?)

class AuthProvider (private val authDao: AuthDao,
                    private val peopleDao: PersonDao,
                    private val networkClient: NetworkClient) {

    private var authorizedUsed: Person? = null

    fun hasUser(): Boolean = authorizedUsed != null
    fun getUser(): Flow<Person> = flow { authorizedUsed }

    suspend fun logOut() {}

    suspend fun tryCreateAccount(name: String, email: String, phone: String?) : NetworkStatus<Unit?> {
        try {
            authorizedUsed = networkClient.api.register(RegisterRequest(name, email, phone))
        } catch (e: Exception) {

            if (e.message != null) return NetworkStatus.Error(e.message!!)
            return NetworkStatus.Error("Unknown error occurred")
        }

        return NetworkStatus.Success(null)
    }

    suspend fun tryAuth(email: String): NetworkStatus<Unit?> {

        try {
            authorizedUsed = networkClient.api.auth(AuthRequest(email))
        } catch (e: Exception) {
            if (e.message != null) return NetworkStatus.Error(e.message!!)
            return NetworkStatus.Error("Unknown error occurred")
        }

        return NetworkStatus.Success(null)
    }
}