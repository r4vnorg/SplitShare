package com.example.splitshare.data

import com.example.splitshare.data.local.ExpenseDao
import com.example.splitshare.data.local.ExpenseSplitDao
import com.example.splitshare.data.local.entity.ExpenseEntity
import com.example.splitshare.data.local.entity.ExpenseSplitEntity
import com.example.splitshare.data.local.toDomain
import com.example.splitshare.data.sync.SyncScheduler
import com.example.splitshare.domain.model.Expense
import com.example.splitshare.domain.model.ExpenseSplit
import com.example.splitshare.domain.model.SyncStatus
import com.example.splitshare.network.NetworkBoundResource
import com.example.splitshare.network.NetworkClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

class RoomExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val expenseSplitDao: ExpenseSplitDao,
    private val syncScheduler: SyncScheduler,
    private val networkClient: NetworkClient,
) : ExpenseRepository {

    override fun observeExpenses(groupId: String): Flow<List<Expense>> =
        expenseDao.observeExpenses(groupId).map { withSplits -> withSplits.map { it.toDomain() } }

    override fun observeExpense(expenseId: String): Flow<Expense?> =
        expenseDao.observeExpense(expenseId).map { it?.toDomain() }

    override suspend fun addExpense(
        groupId: String,
        description: String,
        amountMinor: Long,
        payerId: String,
        splits: Map<String, Long>,
    ): Expense {
        require(splits.values.sum() == amountMinor) { "Split shares must sum to the expense amount" }

        val id = UUID.randomUUID().toString()
        val entity = ExpenseEntity(
            id = id,
            groupId = groupId,
            description = description.trim(),
            amountMinor = amountMinor,
            payerId = payerId,
            createdAt = System.currentTimeMillis(),
        )
        expenseDao.insert(entity)
        expenseSplitDao.insertAll(
            splits.map { (personId, shareMinor) -> ExpenseSplitEntity(id, personId, shareMinor) }
        )
        syncScheduler.scheduleSync()

        return Expense(
            id = entity.id,
            groupId = entity.groupId,
            description = entity.description,
            amountMinor = entity.amountMinor,
            payerId = entity.payerId,
            createdAt = entity.createdAt,
            syncStatus = entity.syncStatus,
            splits = splits.map { (personId, shareMinor) -> ExpenseSplit(personId, shareMinor) },
        )
    }

    override suspend fun retrySync(expenseId: String) {
        expenseDao.updateSyncStatus(expenseId, SyncStatus.PENDING)
        syncScheduler.retryNow()
    }

    override suspend fun getPendingExpenses(): List<Expense> =
        expenseDao.getWithSplitsByStatus(SyncStatus.PENDING).map { it.toDomain() }

    override suspend fun setExpenseSyncStatus(expenseId: String, status: SyncStatus) =
        expenseDao.updateSyncStatus(expenseId, status)

    // ── Network-synced observe ────────────────────────────────────────────

    override fun observeWithSync(
        groupId: String,
        pollIntervalMillis: Long,
    ): Flow<NetworkBoundResource<List<Expense>>> = networkClient.networkBoundResourceWithPolling(
        cache = { expenseDao.observeExpenses(groupId).map { it.map { es -> es.toDomain() } } },
        fetch = { networkClient.api.fetchExpenses(groupId) },
        save = { networkExpenses -> saveExpenses(networkExpenses, groupId) },
        pollIntervalMillis = pollIntervalMillis,
    )

    /**
     * Saves expenses fetched from the network to the local cache.
     * Each expense and its splits are upserted; existing sync status is preserved.
     */
    private suspend fun saveExpenses(networkExpenses: Array<Expense>, groupId: String) {
        for (networkExpense in networkExpenses) {
            val existing = expenseDao.observeExpense(networkExpense.id).first()
            val entity = ExpenseEntity(
                id = networkExpense.id,
                groupId = groupId,
                description = networkExpense.description,
                amountMinor = networkExpense.amountMinor,
                payerId = networkExpense.payerId,
                createdAt = networkExpense.createdAt,
                syncStatus = existing?.expense?.syncStatus ?: SyncStatus.SYNCED,
                receiptUri = null,
            )
            expenseDao.upsert(entity)
            // Upsert splits
            expenseSplitDao.upsertAll(
                networkExpense.splits.map { split ->
                    ExpenseSplitEntity(
                        expenseId = entity.id,
                        personId = split.personId,
                        shareMinor = split.shareMinor,
                    )
                }
            )
        }
    }
}
