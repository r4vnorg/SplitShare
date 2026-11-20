package com.example.splitshare.data

import android.util.Log
import com.example.splitshare.data.local.AppDatabase
import com.example.splitshare.data.local.AuthDao
import com.example.splitshare.data.local.ExpenseDao
import com.example.splitshare.data.local.ExpenseSplitDao
import com.example.splitshare.data.local.PersonDao
import com.example.splitshare.data.local.entity.UserEntity
import com.example.splitshare.data.local.entity.UserWithPerson
import com.example.splitshare.data.local.toDomain
import com.example.splitshare.data.local.toEntity
import com.example.splitshare.data.sync.SyncScheduler
import com.example.splitshare.domain.model.Person
import com.example.splitshare.network.NetworkClient
import com.example.splitshare.network.NetworkStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import retrofit2.Response


data class AuthRequest (val email: String)
data class RegisterRequest (val name: String, val email: String, val phone: String?)

class AuthProvider (private val authDao: AuthDao,
                    private val peopleDao: PersonDao,
                    private val flushDatabases: suspend () -> Unit,
                    private val networkClient: NetworkClient) {

    suspend fun hasUser(): Boolean = authDao.observeUser().first() != null
    suspend fun getUser(): Person? = authDao.observeUser().first()?.profile?.toDomain()

    suspend fun logOut() {
        flushDatabases()
    }

    suspend fun tryCreateAccount(name: String, email: String, phone: String?) : NetworkStatus<Unit?> {

        try {
            val authorizedUsed = networkClient.executeRequest(
                reqID = "",
                fetch = { id: String -> networkClient.api.register(RegisterRequest(name, email, phone)) }
            )

            peopleDao.insert(authorizedUsed.toEntity())
            authDao.insert(UserEntity(0, authorizedUsed.id)) //Literal jank heaven but hopefully works?
        } catch (e: Exception) {

            if (e.message != null) return NetworkStatus.Error(e.message!!)
            return NetworkStatus.Error("Unknown error occurred")
        }

        return NetworkStatus.Success(null)
    }

    suspend fun tryAuth(email: String): NetworkStatus<Unit?> {

        try {
            val authorizedUsed = networkClient.executeRequest(
                reqID = "",
                fetch = { id: String -> networkClient.api.auth(AuthRequest(email)) }
            )
            peopleDao.insert(authorizedUsed.toEntity())
            authDao.insert(UserEntity(0, authorizedUsed.id)) //Literal jank heaven but hopefully works?
        } catch (e: Exception) {
            if (e.message != null) return NetworkStatus.Error(e.message!!)
            return NetworkStatus.Error("Unknown error occurred")
        }

        return NetworkStatus.Success(null)
    }
}