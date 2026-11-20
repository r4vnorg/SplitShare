package com.example.splitshare.domain.model

/** A shared occasion (US4): a named group of people splitting costs. */
data class Group(
    val id: String,
    val name: String,
    val description: String,
    val createdAt: Long,
    val syncStatus: SyncStatus
)

/** Group List row (S4): name plus how many active participants it has. */
data class GroupInfo(
    val group: Group,
    val participants: List<ParticipantEntity>
)