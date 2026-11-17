package com.example.splitshare.data

import com.example.splitshare.domain.model.Expense
import com.example.splitshare.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory test double for ViewModel tests; avoids needing a real Room database. */
class FakeExpenseRepository : ExpenseRepository {

    val addedExpenses = mutableListOf<Expense>()
    val retriedExpenseIds = mutableListOf<String>()

    private val expensesFlow = MutableStateFlow<List<Expense>>(emptyList())

    fun emitExpenses(expenses: List<Expense>) {
        expensesFlow.value = expenses
    }

    override fun observeExpenses(groupId: String): Flow<List<Expense>> = expensesFlow

    override fun observeExpense(expenseId: String): Flow<Expense?> =
        expensesFlow.map { expenses -> expenses.find { it.id == expenseId } }

    override suspend fun addExpense(
        groupId: String,
        description: String,
        amountMinor: Long,
        payerId: String,
        splits: Map<String, Long>,
    ): Expense {
        require(splits.values.sum() == amountMinor)
        val expense = Expense(
            id = "expense-${addedExpenses.size}",
            groupId = groupId,
            description = description,
            amountMinor = amountMinor,
            payerId = payerId,
            createdAt = 0L,
            syncStatus = SyncStatus.PENDING,
            splits = splits.map { (personId, shareMinor) ->
                com.example.splitshare.domain.model.ExpenseSplit(personId, shareMinor)
            },
        )
        addedExpenses += expense
        return expense
    }

    override suspend fun retrySync(expenseId: String) {
        retriedExpenseIds += expenseId
    }

    override suspend fun getPendingExpenses(): List<Expense> = emptyList()

    override suspend fun setExpenseSyncStatus(expenseId: String, status: SyncStatus) = Unit
}
