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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class RoomExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val expenseSplitDao: ExpenseSplitDao,
    private val syncScheduler: SyncScheduler,
) : ExpenseRepository {

    override fun observeExpenses(groupId: String): Flow<List<Expense>> =
        expenseDao.observeExpenses(groupId).map { expenses -> expenses.map { it.toDomain() } }

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
}
