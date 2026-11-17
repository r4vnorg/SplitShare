package com.example.splitshare.data.sync

import com.example.splitshare.domain.model.Expense
import com.example.splitshare.domain.model.Group

/**
 * Stands in for the backend this app doesn't have yet. [SyncWorker] calls this for every
 * locally-queued write; swapping in a real network-backed implementation later needs no other
 * change (repositories, the worker, and the retry/backoff plumbing stay the same).
 */
interface SyncService {
    suspend fun syncGroup(group: Group): Boolean
    suspend fun syncParticipant(groupId: String, personId: String, removed: Boolean): Boolean
    suspend fun syncExpense(expense: Expense): Boolean

    /** US12: delivers a payment reminder to [toPersonId]. */
    suspend fun sendReminder(groupId: String, toPersonId: String, amountMinor: Long): Boolean
}
