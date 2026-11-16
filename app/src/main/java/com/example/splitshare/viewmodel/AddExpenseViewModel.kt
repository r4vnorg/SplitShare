package com.example.splitshare.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitshare.data.GroupRepository
import com.example.splitshare.domain.ExpenseValidator
import com.example.splitshare.domain.MoneyParser
import com.example.splitshare.domain.model.Participant
import com.example.splitshare.ui.navigation.Routes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

private data class AddExpenseFormState(
    val payerId: String? = null,
    val amountText: String = "",
    val payerError: String? = null,
    val amountError: String? = null,
)

data class AddExpenseUiState(
    val participants: List<Participant> = emptyList(),
    val payerId: String? = null,
    val amountText: String = "",
    val payerError: String? = null,
    val amountError: String? = null,
)

/**
 * Shared across both Add Expense steps (US6 payer+amount, US7 select split members) - scoped to
 * the ADD_EXPENSE_GRAPH nav graph's back-stack entry so both screens see the same instance.
 */
class AddExpenseViewModel(
    savedStateHandle: SavedStateHandle,
    groupRepository: GroupRepository,
) : ViewModel() {

    private val groupId: String = checkNotNull(savedStateHandle[Routes.GROUP_ID_ARG])

    private val formState = MutableStateFlow(AddExpenseFormState())

    val uiState: StateFlow<AddExpenseUiState> = combine(
        groupRepository.observeParticipants(groupId),
        formState,
    ) { participants, form ->
        AddExpenseUiState(
            participants = participants,
            payerId = form.payerId,
            amountText = form.amountText,
            payerError = form.payerError,
            amountError = form.amountError,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AddExpenseUiState())

    fun onPayerSelected(personId: String) = formState.update { it.copy(payerId = personId, payerError = null) }

    fun onAmountChange(text: String) = formState.update { it.copy(amountText = text, amountError = null) }

    /** Validates payer + amount (US6). Returns true when valid, so the caller can proceed to US7's step 2. */
    fun validatePayerAndAmount(): Boolean {
        val form = formState.value
        val amountMinor = MoneyParser.parseToMinorUnits(form.amountText)
        val payerError = ExpenseValidator.payerError(form.payerId)
        val amountError = ExpenseValidator.amountError(amountMinor)
        if (payerError != null || amountError != null) {
            formState.update { it.copy(payerError = payerError, amountError = amountError) }
            return false
        }
        return true
    }
}
