package com.example.splitshare.data.sync

import com.example.splitshare.domain.model.Expense
import com.example.splitshare.domain.model.Group
import kotlinx.coroutines.delay

/** No backend exists yet; this simulates one succeeding after a short round-trip delay. */
class StubSyncService : SyncService {

    override suspend fun syncGroup(group: Group): Boolean {
        delay(SIMULATED_LATENCY_MS)
        return true
    }

    override suspend fun syncParticipant(groupId: String, personId: String, removed: Boolean): Boolean {
        delay(SIMULATED_LATENCY_MS)
        return true
    }

    override suspend fun syncExpense(expense: Expense): Boolean {
        delay(SIMULATED_LATENCY_MS)
        return true
    }

    private companion object {
        const val SIMULATED_LATENCY_MS = 800L
    }
}
