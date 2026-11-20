package com.example.splitshare.domain.model

data class Participant(
    val groupId: String,
    val personId: String,
    val addedAt: Long,
    val removedAt: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
)

data class ParticipantWithPerson(
    val participant: Participant,
    val person: Person
)
