package com.example.splitshare.data

import com.example.splitshare.domain.model.Expense
import com.example.splitshare.domain.model.SyncStatus
import com.example.splitshare.network.NetworkBoundResource
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {

    fun observeExpenses(groupId: String): Flow<List<Expense>>

    fun observeExpense(expenseId: String): Flow<Expense?>

    /**
     * Local-first flow with automatic network polling for expenses in a group.
     * See [GroupRepository.observeWithSync] for the full lifecycle description.
     */
    fun observeWithSync(
        groupId: String,
        pollIntervalMillis: Long = 30_000,
    ): Flow<NetworkBoundResource<List<Expense>>>

    /** [splits] must sum exactly to [amountMinor] (callers validate via SplitValidator; this is a defensive re-check). */
    suspend fun addExpense(
        groupId: String,
        description: String,
        amountMinor: Long,
        payerId: String,
        splits: Map<String, Long>,
    ): Expense

    /** Flips a FAILED expense back to PENDING and re-enqueues sync immediately (Expense Detail's Retry). */
    suspend fun retrySync(expenseId: String)

    // Worker-only: background sync plumbing.
    suspend fun getPendingExpenses(): List<Expense>
    suspend fun setExpenseSyncStatus(expenseId: String, status: SyncStatus)
}
