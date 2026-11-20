package com.example.splitshare.domain.model

/** A [Person]'s membership in one group (US5). Removed participants are kept for expense history. */
data class Participant(
    val person: Person,
    val isActive: Boolean,
)

data class ParticipantEntity(
    val groupId: String,
    val personId: String,
    val addedAt: Long,
    val removedAt: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
)

/** A participant add/remove change still queued for background sync (worker-only). */
data class PendingParticipantChange(
    val groupId: String,
    val personId: String,
    val removed: Boolean,
)
