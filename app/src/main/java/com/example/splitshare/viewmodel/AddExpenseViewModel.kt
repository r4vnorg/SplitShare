package com.example.splitshare.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitshare.data.ExpenseRepository
import com.example.splitshare.data.GroupRepository
import com.example.splitshare.domain.ExpenseValidator
import com.example.splitshare.domain.MoneyFormatter
import com.example.splitshare.domain.MoneyParser
import com.example.splitshare.domain.SplitCalculator
import com.example.splitshare.domain.SplitValidator
import com.example.splitshare.domain.model.Participant
import com.example.splitshare.ui.navigation.Routes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SplitMode { EQUAL, CUSTOM }

private data class AddExpenseFormState(
    // Step 1 (US6): payer + amount.
    val payerId: String? = null,
    val amountText: String = "",
    val payerError: String? = null,
    val amountError: String? = null,
    // Step 2 (US7): who's included and how it's split.
    val selectedMemberIds: Set<String> = emptySet(),
    val splitMode: SplitMode = SplitMode.EQUAL,
    val customShareTexts: Map<String, String> = emptyMap(),
    val splitError: String? = null,
)

data class SplitMemberRow(
    val personId: String,
    val name: String,
    val isSelected: Boolean,
    val shareText: String,
)

data class AddExpenseUiState(
    val participants: List<Participant> = emptyList(),
    val payerId: String? = null,
    val amountText: String = "",
    val payerError: String? = null,
    val amountError: String? = null,
    val splitMembers: List<SplitMemberRow> = emptyList(),
    val splitMode: SplitMode = SplitMode.EQUAL,
    val splitError: String? = null,
)

/**
 * Shared across both Add Expense steps (US6 payer+amount, US7 select split members) - scoped to
 * the ADD_EXPENSE_GRAPH nav graph's back-stack entry so both screens see the same instance.
 */
class AddExpenseViewModel(
    savedStateHandle: SavedStateHandle,
    private val groupRepository: GroupRepository,
    private val expenseRepository: ExpenseRepository,
) : ViewModel() {

    private val groupId: String = checkNotNull(savedStateHandle[Routes.GROUP_ID_ARG])

    private val formState = MutableStateFlow(AddExpenseFormState())

    init {
        // Default to splitting among everyone once participants first load (US7: "all or selected").
        viewModelScope.launch {
            val allIds = groupRepository.observeParticipants(groupId).first().map { it.person.id }.toSet()
            formState.update { it.copy(selectedMemberIds = allIds) }
        }
    }

    val uiState: StateFlow<AddExpenseUiState> = combine(
        groupRepository.observeParticipants(groupId),
        formState,
    ) { participants, form ->
        val amountMinor = MoneyParser.parseToMinorUnits(form.amountText)
        val equalShares: Map<String, Long> = if (amountMinor != null && form.selectedMemberIds.isNotEmpty()) {
            SplitCalculator.equalSplit(amountMinor, form.selectedMemberIds.toList())
        } else {
            emptyMap()
        }

        AddExpenseUiState(
            participants = participants,
            payerId = form.payerId,
            amountText = form.amountText,
            payerError = form.payerError,
            amountError = form.amountError,
            splitMembers = participants.map { participant ->
                val id = participant.person.id
                val isSelected = id in form.selectedMemberIds
                val shareText = when {
                    !isSelected -> ""
                    form.splitMode == SplitMode.EQUAL -> equalShares[id]?.let { MoneyFormatter.formatPlain(it) }.orEmpty()
                    else -> form.customShareTexts[id].orEmpty()
                }
                SplitMemberRow(personId = id, name = participant.person.name, isSelected = isSelected, shareText = shareText)
            },
            splitMode = form.splitMode,
            splitError = form.splitError,
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

    fun toggleMemberSelected(personId: String) = formState.update { form ->
        val updated = if (personId in form.selectedMemberIds) form.selectedMemberIds - personId else form.selectedMemberIds + personId
        form.copy(selectedMemberIds = updated, splitError = null)
    }

    fun setSplitMode(mode: SplitMode) = formState.update { it.copy(splitMode = mode, splitError = null) }

    fun onCustomShareChange(personId: String, text: String) = formState.update {
        it.copy(customShareTexts = it.customShareTexts + (personId to text), splitError = null)
    }

    /** Validates the split (US7) and, if valid, saves the expense locally (PENDING, queued for sync). */
    fun saveSplit(onSaved: () -> Unit) {
        val form = formState.value
        val amountMinor = MoneyParser.parseToMinorUnits(form.amountText) ?: return
        val selectedIds = form.selectedMemberIds.toList()

        val selectionError = SplitValidator.selectionError(selectedIds)
        if (selectionError != null) {
            formState.update { it.copy(splitError = selectionError) }
            return
        }

        val shares: Map<String, Long> = when (form.splitMode) {
            SplitMode.EQUAL -> SplitCalculator.equalSplit(amountMinor, selectedIds)
            SplitMode.CUSTOM -> {
                val parsed = selectedIds.associateWith { MoneyParser.parseToMinorUnits(form.customShareTexts[it].orEmpty()) }
                if (parsed.values.any { it == null }) {
                    formState.update { it.copy(splitError = "Enter a valid amount for every selected member") }
                    return
                }
                parsed.mapValues { (_, shareMinor) -> checkNotNull(shareMinor) }
            }
        }

        val splitError = SplitValidator.customSplitError(amountMinor, shares)
        if (splitError != null) {
            formState.update { it.copy(splitError = splitError) }
            return
        }

        viewModelScope.launch {
            expenseRepository.addExpense(
                groupId = groupId,
                description = "",
                amountMinor = amountMinor,
                payerId = checkNotNull(form.payerId),
                splits = shares,
            )
            onSaved()
        }
    }
}
