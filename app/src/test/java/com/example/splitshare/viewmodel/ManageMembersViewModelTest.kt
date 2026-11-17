package com.example.splitshare.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.splitshare.data.FakeExpenseRepository
import com.example.splitshare.data.FakeGroupRepository
import com.example.splitshare.data.FakePersonRepository
import com.example.splitshare.domain.model.Expense
import com.example.splitshare.domain.model.ExpenseSplit
import com.example.splitshare.domain.model.Participant
import com.example.splitshare.domain.model.Person
import com.example.splitshare.domain.model.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ManageMembersViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val groupId = "g1"
    private val groupRepository = FakeGroupRepository()
    private val expenseRepository = FakeExpenseRepository()
    private val personRepository = FakePersonRepository()

    private fun viewModel() = ManageMembersViewModel(
        savedStateHandle = SavedStateHandle(mapOf("groupId" to groupId)),
        groupRepository = groupRepository,
        expenseRepository = expenseRepository,
        personRepository = personRepository,
    )

    @Test
    fun membersReflectActiveParticipants() {
        groupRepository.emitParticipants(
            listOf(Participant(Person("p1", "Alice", "alice@example.com", ""), isActive = true))
        )

        val viewModel = viewModel()

        assertEquals(1, viewModel.uiState.value.members.size)
        assertEquals("Alice", viewModel.uiState.value.members.single().name)
    }

    @Test
    fun removingAMemberWithZeroBalanceSucceeds() {
        groupRepository.emitParticipants(listOf(Participant(Person("p1", "Alice", "a@x.com", ""), true)))
        expenseRepository.emitExpenses(emptyList())

        val viewModel = viewModel()
        viewModel.removeParticipant("p1")

        assertEquals(listOf("g1" to "p1"), groupRepository.removedParticipants)
        assertNull(viewModel.uiState.value.removeError)
    }

    @Test
    fun removingAMemberWithNonZeroBalanceIsBlocked() {
        groupRepository.emitParticipants(listOf(Participant(Person("p1", "Alice", "a@x.com", ""), true)))
        expenseRepository.emitExpenses(
            listOf(
                Expense(
                    id = "e1",
                    groupId = groupId,
                    description = "Dinner",
                    amountMinor = 1000L,
                    payerId = "p1",
                    createdAt = 0L,
                    syncStatus = SyncStatus.PENDING,
                    splits = listOf(ExpenseSplit("p1", 500L), ExpenseSplit("p2", 500L)),
                )
            )
        )

        val viewModel = viewModel()
        viewModel.removeParticipant("p1")

        assertTrue(groupRepository.removedParticipants.isEmpty())
        assertEquals("Cannot remove: outstanding balance of 5,00 kr", viewModel.uiState.value.removeError)
    }

    @Test
    fun blankNameBlocksAddingAParticipant() {
        val viewModel = viewModel()
        viewModel.onNewPersonNameChange("")
        viewModel.onNewPersonEmailChange("new@example.com")

        viewModel.addParticipant()

        assertEquals("Name is required", viewModel.uiState.value.nameError)
        assertTrue(personRepository.people.isEmpty())
        assertTrue(groupRepository.addedParticipants.isEmpty())
    }

    @Test
    fun validInputAddsParticipantAndClearsForm() {
        val viewModel = viewModel()
        viewModel.onNewPersonNameChange("Bob")
        viewModel.onNewPersonEmailChange("bob@example.com")

        viewModel.addParticipant()

        assertEquals(1, groupRepository.addedParticipants.size)
        assertEquals("Bob", groupRepository.addedParticipants.single().second.name)
        assertEquals(groupId, groupRepository.addedParticipants.single().first)
        assertEquals("", viewModel.uiState.value.newPersonName)
    }

    @Test
    fun addingAParticipantWithAnExistingEmailReusesThePerson() {
        val viewModel = viewModel()
        viewModel.onNewPersonNameChange("Bob")
        viewModel.onNewPersonEmailChange("bob@example.com")
        viewModel.addParticipant()

        viewModel.onNewPersonNameChange("Bobby")
        viewModel.onNewPersonEmailChange("Bob@Example.com")
        viewModel.addParticipant()

        assertEquals(1, personRepository.people.size)
        assertEquals(2, groupRepository.addedParticipants.size)
    }
}
