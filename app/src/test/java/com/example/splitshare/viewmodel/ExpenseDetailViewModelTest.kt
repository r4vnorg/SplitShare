package com.example.splitshare.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.splitshare.data.FakeExpenseRepository
import com.example.splitshare.data.FakeGroupRepository
import com.example.splitshare.domain.model.Expense
import com.example.splitshare.domain.model.ExpenseSplit
import com.example.splitshare.domain.model.ParticipantWithPerson
import com.example.splitshare.domain.model.Person
import com.example.splitshare.domain.model.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExpenseDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val groupId = "g1"
    private val expenseId = "e1"
    private val groupRepository = FakeGroupRepository()
    private val expenseRepository = FakeExpenseRepository()

    private fun viewModel() = ExpenseDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf("groupId" to groupId, "expenseId" to expenseId)),
        expenseRepository = expenseRepository,
        groupRepository = groupRepository,
    )

    private fun expense(syncStatus: SyncStatus) = Expense(
        id = expenseId,
        groupId = groupId,
        description = "Groceries",
        amountMinor = 200L,
        payerId = "p1",
        createdAt = 0L,
        syncStatus = syncStatus,
        splits = listOf(ExpenseSplit("p1", 100L), ExpenseSplit("p2", 100L)),
    )

    @Test
    fun isLoadingWhenExpenseIsNotYetObserved() {
        assertTrue(viewModel().uiState.value.isLoading)
    }

    @Test
    fun uiStateResolvesPayerAndSplitNamesFromParticipants() {
        groupRepository.emitParticipants(
            listOf(
                ParticipantWithPerson(Person("p1", "Alice", "a@x.com", ""), true),
                ParticipantWithPerson(Person("p2", "Bob", "b@x.com", ""), true),
            )
        )
        expenseRepository.emitExpenses(listOf(expense(SyncStatus.SYNCED)))

        val state = viewModel().uiState.value

        assertEquals("Alice", state.payerName)
        assertEquals(setOf("Alice", "Bob"), state.splits.map { it.personName }.toSet())
        assertFalse(state.isLoading)
    }

    @Test
    fun canRetryIsFalseWhenPending() {
        expenseRepository.emitExpenses(listOf(expense(SyncStatus.PENDING)))

        assertFalse(viewModel().uiState.value.canRetry)
    }

    @Test
    fun canRetryIsFalseWhenSynced() {
        expenseRepository.emitExpenses(listOf(expense(SyncStatus.SYNCED)))

        assertFalse(viewModel().uiState.value.canRetry)
    }

    @Test
    fun canRetryIsTrueOnlyWhenFailed() {
        expenseRepository.emitExpenses(listOf(expense(SyncStatus.FAILED)))

        assertTrue(viewModel().uiState.value.canRetry)
    }

    @Test
    fun retryDelegatesToRepository() {
        expenseRepository.emitExpenses(listOf(expense(SyncStatus.FAILED)))
        val viewModel = viewModel()

        viewModel.retry()

        assertEquals(listOf(expenseId), expenseRepository.retriedExpenseIds)
    }
}
