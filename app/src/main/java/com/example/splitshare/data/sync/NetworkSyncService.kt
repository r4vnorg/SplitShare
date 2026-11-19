package com.example.splitshare.data.sync

import com.example.splitshare.domain.model.Expense
import com.example.splitshare.domain.model.Group
import com.example.splitshare.network.NetworkAPI
import java.util.UUID

/**
 * Real implementation of [SyncService] backed by the [NetworkAPI].
 *
 * All operations are idempotent — each request carries a client-generated UUID as [request_id],
 * so retries never duplicate data (AGENTS.md: idempotent requests).
 *
 * The [NetworkAPI] calls are already retry-aware via the RetryInterceptor in [NetworkClient],
 * so transient network failures are handled transparently. This layer only translates domain
 * models to API payloads and maps HTTP success/failure to a boolean result.
 */
class NetworkSyncService(
    private val api: NetworkAPI,
) : SyncService {

    override suspend fun syncGroup(group: Group): Boolean = runCatching {
        api.createGroup(group, UUID.randomUUID().toString())
        true
    }.getOrDefault(false)

    override suspend fun syncParticipant(groupId: String, personId: String, removed: Boolean): Boolean = runCatching {
        if (removed) {
            api.removeParticipant(groupId, personId, UUID.randomUUID().toString())
        } else {
            // Adding a participant requires a Person payload; the person must already exist
            // in the directory. We use a minimal Person object — the backend uses the ID.
            api.addParticipant(groupId, com.example.splitshare.domain.model.Person(personId, "", "", ""), UUID.randomUUID().toString())
        }
        true
    }.getOrDefault(false)

    override suspend fun syncExpense(expense: Expense): Boolean = runCatching {
        api.createExpense(expense, UUID.randomUUID().toString())
        true
    }.getOrDefault(false)

    override suspend fun sendReminder(groupId: String, toPersonId: String, amountMinor: Long): Boolean = runCatching {
        api.sendReminder(
            groupId = groupId,
            request = com.example.splitshare.network.SendReminderRequest(toPersonId, amountMinor),
            requestId = UUID.randomUUID().toString(),
        )
        true
    }.getOrDefault(false)
}
