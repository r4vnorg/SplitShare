package com.example.splitshare.data.local.entity

import androidx.room.Embedded
import androidx.room.ForeignKey
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "user")
data class UserEntity (
    @PrimaryKey val pk: Int = 0,
    val id: String,
)
