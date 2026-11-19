package com.example.splitshare.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.splitshare.data.local.entity.PersonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {

    @Query("SELECT * FROM people ORDER BY name")
    fun observeAll(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM people WHERE email = :normalizedEmail LIMIT 1")
    suspend fun findByEmail(normalizedEmail: String): PersonEntity?

    @Query("SELECT * FROM people WHERE id = :id LIMIT 1")
    fun findById(id: String) : Flow<PersonEntity?>

    @Insert
    suspend fun insert(person: PersonEntity)
}
