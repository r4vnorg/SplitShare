package com.example.splitshare.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.splitshare.data.FakeExpenseRepository
import com.example.splitshare.data.FakeGroupRepository
import com.example.splitshare.domain.model.Expense
import com.example.splitshare.domain.model.ExpenseSplit
import com.example.splitshare.domain.model.ParticipantWithPerson
import com.example.splitshare.domain.model.Person
import com.example.splitshare.domain.model.SyncStatus
import com.example.splitshare.data.FakeReminderRepository
import kotlinx.coroutines.flow.flowOf
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
    private val reminderRepository = FakeReminderRepository()

    private val alice = Person("p1", "Alice", "alice@example.com", "")
    private val bob = Person("p2", "Bob", "bob@example.com", "")

    /** Alice paid 200, split evenly: Alice is owed 100, Bob owes 100. */
    private fun aliceIsOwedByBob() {
        groupRepository.emitParticipants(listOf(ParticipantWithPerson(alice, true), ParticipantWithPerson(bob, true)))
        expenseRepository.emitExpenses(
            listOf(
                Expense("e1", groupId, "Dinner", 200L, "p1", 0L, SyncStatus.SYNCED,
                    listOf(ExpenseSplit("p1", 100L), ExpenseSplit("p2", 100L)))
            )
        )
    }

    private fun viewModel() = GroupDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf("groupId" to groupId)),
        groupRepository = groupRepository,
        expenseRepository = expenseRepository,
        reminderRepository = reminderRepository,
        currentUserEmail = flowOf("alice@example.com"),
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
            listOf(ParticipantWithPerson(Person("p1", "Alice", "alice@example.com", ""), isActive = true))
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

    @Test
    fun creditorSeesRemindOnlyForTheMemberWhoOwesThem() {
        aliceIsOwedByBob()

        val balances = viewModel().uiState.value.balances.associateBy { it.personId }

        assertEquals(0L, balances.getValue("p1").owedToMeMinor)
        assertEquals(100L, balances.getValue("p2").owedToMeMinor)
    }

    @Test
    fun debtorHasNoRemindOptions() {
        aliceIsOwedByBob()
        val bobViewModel = GroupDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("groupId" to groupId)),
            groupRepository = groupRepository,
            expenseRepository = expenseRepository,
            reminderRepository = reminderRepository,
            currentUserEmail = flowOf("bob@example.com"),
        )

        assertTrue(bobViewModel.uiState.value.balances.all { it.owedToMeMinor == 0L })
    }

    @Test
    fun sendReminderQueuesReminderForDebtorWithTheirAmount() {
        aliceIsOwedByBob()
        val viewModel = viewModel()

        viewModel.sendReminder("p2")

        assertEquals(listOf(FakeReminderRepository.Sent(groupId, "p2", "Bob", 100L)), reminderRepository.sent)
        assertEquals("Reminder to Bob queued. You'll be notified when it is sent.", viewModel.uiState.value.message)
    }

    @Test
    fun sendReminderIgnoresMembersWhoDoNotOweTheUser() {
        aliceIsOwedByBob()

        viewModel().sendReminder("p1")

        assertTrue(reminderRepository.sent.isEmpty())
    }
}
