package com.example.splitshare.data.cahce

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.splitshare.domain.model.Profile
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileCache {

    @Query("SELECT * FROM users")
    fun getProfiles(): Flow<List<Profile>>

    @Query("SELECT * FROM users WHERE id = :id")
    fun getProfile(id: String): Flow<Profile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(users: List<Profile>)
}