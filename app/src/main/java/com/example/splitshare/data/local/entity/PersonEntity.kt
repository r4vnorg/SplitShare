package com.example.splitshare.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "people", indices = [Index(value = ["email"], unique = true)])
data class PersonEntity(
    @PrimaryKey val id: String,
    val name: String,
    /** Normalized (trim + lowercase) so lookups and the unique index are case-insensitive. */
    val email: String,
    val phone: String,
)
