package com.example.splitshare.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.splitshare.data.FakeExpenseRepository
import com.example.splitshare.data.FakeGroupRepository
import com.example.splitshare.domain.model.Expense
import com.example.splitshare.domain.model.ExpenseSplit
import com.example.splitshare.domain.model.Participant
import com.example.splitshare.domain.model.Person
import com.example.splitshare.domain.model.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class GroupDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val groupId = "g1"
    private val groupRepository = FakeGroupRepository()
    private val expenseRepository = FakeExpenseRepository()

    private fun viewModel() = GroupDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf("groupId" to groupId)),
        groupRepository = groupRepository,
        expenseRepository = expenseRepository,
    )

    @Test
    fun startsOnTheTransactionsTab() {
        assertEquals(GroupDetailTab.TRANSACTIONS, viewModel().uiState.value.selectedTab)
    }

    @Test
    fun selectTabSwitchesToBalances() {
        val viewModel = viewModel()
        viewModel.selectTab(GroupDetailTab.BALANCES)
        assertEquals(GroupDetailTab.BALANCES, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun noExpensesMeansEmptyList() {
        assertTrue(viewModel().uiState.value.expenses.isEmpty())
    }

    @Test
    fun expenseSummaryResolvesPayerNameFromParticipants() {
        groupRepository.emitParticipants(
            listOf(Participant(Person("p1", "Alice", "alice@example.com", ""), isActive = true))
        )
        expenseRepository.emitExpenses(
            listOf(
                Expense(
                    id = "e1",
                    groupId = groupId,
                    description = "Groceries",
                    amountMinor = 200L,
                    payerId = "p1",
                    createdAt = 0L,
                    syncStatus = SyncStatus.SYNCED,
                    splits = listOf(ExpenseSplit("p1", 200L)),
                )
            )
        )

        val summary = viewModel().uiState.value.expenses.single()

        assertEquals("Alice", summary.payerName)
        assertEquals("Groceries", summary.description)
        assertEquals(SyncStatus.SYNCED, summary.syncStatus)
    }
}
