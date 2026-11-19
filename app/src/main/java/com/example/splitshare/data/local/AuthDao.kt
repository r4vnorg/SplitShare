package com.example.splitshare.data.local

import androidx.room.Dao
import androidx.room.Query
import com.example.splitshare.data.local.entity.PersonEntity
import com.example.splitshare.data.local.entity.UserEntity
import com.example.splitshare.domain.model.Person


@Dao
interface AuthDao {

    @Query("SELECT * FROM user WHERE pk = 0 LIMIT 1")
    fun observeUser() : UserEntity?
}