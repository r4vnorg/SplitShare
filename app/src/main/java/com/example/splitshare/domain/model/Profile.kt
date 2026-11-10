package com.example.splitshare.domain.model

/** The signed-in user's profile (US1). Phone is optional. */
data class Profile(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
)
