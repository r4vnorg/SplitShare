package com.example.splitshare.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.splitshare.data.local.entity.PersonEntity
import com.example.splitshare.data.local.entity.UserEntity
import com.example.splitshare.data.local.entity.UserWithPerson
import com.example.splitshare.domain.model.Person
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthDao {

    @Transaction
    @Query("SELECT * FROM user WHERE pk = 0 LIMIT 1")
    fun observeUser() : Flow<UserWithPerson?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userEntity: UserEntity)

    @Query("DELETE FROM user")
    suspend fun flushCache()
}