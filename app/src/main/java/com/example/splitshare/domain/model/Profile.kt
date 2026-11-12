package com.example.splitshare.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** The signed-in user's profile (US1). Phone is optional. */
@Entity(tableName = "Profile")
data class Profile(

    @PrimaryKey
    val id: String,

    val name: String,
    val email: String,
    val phone: String,
)
