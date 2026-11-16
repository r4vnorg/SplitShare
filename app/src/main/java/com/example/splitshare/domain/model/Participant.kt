package com.example.splitshare.domain.model

/** A [Person]'s membership in one group (US5). Removed participants are kept for expense history. */
data class Participant(
    val person: Person,
    val isActive: Boolean,
)

/** A participant add/remove change still queued for background sync (worker-only). */
data class PendingParticipantChange(
    val groupId: String,
    val personId: String,
    val removed: Boolean,
)
