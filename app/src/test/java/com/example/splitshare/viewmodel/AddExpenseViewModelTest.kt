package com.example.splitshare.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.splitshare.data.FakeGroupRepository
import com.example.splitshare.domain.model.Participant
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

    private fun viewModel() = AddExpenseViewModel(
        savedStateHandle = SavedStateHandle(mapOf("groupId" to groupId)),
        groupRepository = groupRepository,
    )

    @Test
    fun uiStateReflectsGroupParticipants() {
        groupRepository.emitParticipants(
            listOf(Participant(Person("p1", "Alice", "alice@example.com", ""), isActive = true))
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
}
