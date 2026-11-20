package com.example.splitshare.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.splitshare.data.FakeExpenseRepository
import com.example.splitshare.data.FakeGroupRepository
import com.example.splitshare.domain.model.ParticipantWithPerson
import com.example.splitshare.domain.model.Person
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AddExpenseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val groupId = "g1"
    private val groupRepository = FakeGroupRepository()
    private val expenseRepository = FakeExpenseRepository()

    private fun viewModel() = AddExpenseViewModel(
        savedStateHandle = SavedStateHandle(mapOf("groupId" to groupId)),
        groupRepository = groupRepository,
        expenseRepository = expenseRepository,
    )

    private fun twoParticipants() = listOf(
        ParticipantWithPerson(Person("p1", "Alice", "alice@example.com", ""), isActive = true),
        ParticipantWithPerson(Person("p2", "Bob", "bob@example.com", ""), isActive = true),
    )

    @Test
    fun uiStateReflectsGroupParticipants() {
        groupRepository.emitParticipants(
            listOf(ParticipantWithPerson(Person("p1", "Alice", "alice@example.com", ""), isActive = true))
        )

        val viewModel = viewModel()

        assertEquals(1, viewModel.uiState.value.participants.size)
    }

    @Test
    fun missingPayerAndAmountAreBothRejected() {
        val viewModel = viewModel()

        val isValid = viewModel.validatePayerAndAmount()

        assertFalse(isValid)
        assertEquals("Select who paid", viewModel.uiState.value.payerError)
        assertEquals("Enter an amount", viewModel.uiState.value.amountError)
    }

    @Test
    fun invalidAmountTextIsRejected() {
        val viewModel = viewModel()
        viewModel.onPayerSelected("p1")
        viewModel.onAmountChange("not a number")

        val isValid = viewModel.validatePayerAndAmount()

        assertFalse(isValid)
        assertEquals("Enter an amount", viewModel.uiState.value.amountError)
    }

    @Test
    fun zeroAmountIsRejected() {
        val viewModel = viewModel()
        viewModel.onPayerSelected("p1")
        viewModel.onAmountChange("0")

        assertFalse(viewModel.validatePayerAndAmount())
        assertEquals("Amount must be greater than zero", viewModel.uiState.value.amountError)
    }

    @Test
    fun validPayerAndAmountPasses() {
        val viewModel = viewModel()
        viewModel.onPayerSelected("p1")
        viewModel.onAmountChange("123.45")

        assertTrue(viewModel.validatePayerAndAmount())
        assertEquals(null, viewModel.uiState.value.payerError)
        assertEquals(null, viewModel.uiState.value.amountError)
    }

    @Test
    fun changingPayerClearsItsOwnError() {
        val viewModel = viewModel()
        viewModel.validatePayerAndAmount()
        assertEquals("Select who paid", viewModel.uiState.value.payerError)

        viewModel.onPayerSelected("p1")

        assertEquals(null, viewModel.uiState.value.payerError)
    }

    @Test
    fun allParticipantsAreSelectedByDefault() {
        groupRepository.emitParticipants(twoParticipants())

        val viewModel = viewModel()

        assertTrue(viewModel.uiState.value.splitMembers.all { it.isSelected })
    }

    @Test
    fun togglingAMemberDeselectsThem() {
        groupRepository.emitParticipants(twoParticipants())
        val viewModel = viewModel()

        viewModel.toggleMemberSelected("p2")

        val member = viewModel.uiState.value.splitMembers.single { it.personId == "p2" }
        assertFalse(member.isSelected)
    }

    @Test
    fun deselectingEveryoneBlocksSave() {
        groupRepository.emitParticipants(twoParticipants())
        val viewModel = viewModel()
        viewModel.onPayerSelected("p1")
        viewModel.onAmountChange("10")
        viewModel.toggleMemberSelected("p1")
        viewModel.toggleMemberSelected("p2")
        var saved = false

        viewModel.saveSplit { saved = true }

        assertFalse(saved)
        assertEquals("Select at least one member", viewModel.uiState.value.splitError)
    }

    @Test
    fun equalSplitWithRemainderSavesSharesSummingToTheTotal() {
        groupRepository.emitParticipants(twoParticipants())
        val viewModel = viewModel()
        viewModel.onPayerSelected("p1")
        viewModel.onAmountChange("1") // 100 øre / 2 members

        viewModel.saveSplit {}

        val saved = expenseRepository.addedExpenses.single()
        assertEquals(100L, saved.splits.sumOf { it.shareMinor })
        assertEquals(2, saved.splits.size)
    }

    @Test
    fun customSplitNotSummingToTotalIsRejected() {
        groupRepository.emitParticipants(twoParticipants())
        val viewModel = viewModel()
        viewModel.onPayerSelected("p1")
        viewModel.onAmountChange("10")
        viewModel.setSplitMode(SplitMode.CUSTOM)
        viewModel.onCustomShareChange("p1", "4")
        viewModel.onCustomShareChange("p2", "4") // 8 != 10
        var saved = false

        viewModel.saveSplit { saved = true }

        assertFalse(saved)
        assertEquals("Shares must add up to the total amount", viewModel.uiState.value.splitError)
        assertTrue(expenseRepository.addedExpenses.isEmpty())
    }

    @Test
    fun customSplitWithBlankShareIsRejected() {
        groupRepository.emitParticipants(twoParticipants())
        val viewModel = viewModel()
        viewModel.onPayerSelected("p1")
        viewModel.onAmountChange("10")
        viewModel.setSplitMode(SplitMode.CUSTOM)
        viewModel.onCustomShareChange("p1", "10")
        // p2's share left blank
        var saved = false

        viewModel.saveSplit { saved = true }

        assertFalse(saved)
        assertEquals("Enter a valid amount for every selected member", viewModel.uiState.value.splitError)
    }

    @Test
    fun customSplitSummingToTotalSaves() {
        groupRepository.emitParticipants(twoParticipants())
        val viewModel = viewModel()
        viewModel.onPayerSelected("p1")
        viewModel.onAmountChange("10")
        viewModel.setSplitMode(SplitMode.CUSTOM)
        viewModel.onCustomShareChange("p1", "6")
        viewModel.onCustomShareChange("p2", "4")
        var saved = false

        viewModel.saveSplit { saved = true }

        assertTrue(saved)
        val savedExpense = expenseRepository.addedExpenses.single()
        assertEquals(600L, savedExpense.splits.single { it.personId == "p1" }.shareMinor)
        assertEquals(400L, savedExpense.splits.single { it.personId == "p2" }.shareMinor)
    }
}
