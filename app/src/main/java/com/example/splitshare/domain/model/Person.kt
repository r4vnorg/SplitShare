package com.example.splitshare.domain.model

/** A person who can be a group participant: the device owner's own Profile, or anyone added to a group. */
data class Person(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
)

data class PersonRef(
    val id: String
)