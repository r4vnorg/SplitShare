package com.example.splitshare.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitshare.data.ExpenseRepository
import com.example.splitshare.data.GroupRepository
import com.example.splitshare.domain.model.SyncStatus
import com.example.splitshare.ui.navigation.Routes
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SplitLine(val personName: String, val shareMinor: Long)

data class ExpenseDetailUiState(
    val description: String = "",
    val amountMinor: Long = 0L,
    val payerName: String = "",
    val splits: List<SplitLine> = emptyList(),
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val canRetry: Boolean = false,
    val isLoading: Boolean = true,
)

/** S10 Expense detail: shows sync status, with Retry shown only when it's FAILED. */
class ExpenseDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val expenseRepository: ExpenseRepository,
    groupRepository: GroupRepository,
) : ViewModel() {

    private val groupId: String = checkNotNull(savedStateHandle[Routes.GROUP_ID_ARG])
    private val expenseId: String = checkNotNull(savedStateHandle[Routes.EXPENSE_ID_ARG])

    val uiState: StateFlow<ExpenseDetailUiState> = combine(
        expenseRepository.observeExpense(expenseId),
        groupRepository.observeParticipants(groupId),
    ) { expense, participants ->
        if (expense == null) {
            ExpenseDetailUiState(isLoading = true)
        } else {
            val nameByPersonId = participants.associate { it.person.id to it.person.name }
            ExpenseDetailUiState(
                description = expense.description,
                amountMinor = expense.amountMinor,
                payerName = nameByPersonId[expense.payerId] ?: "Unknown",
                splits = expense.splits.map { split ->
                    SplitLine(nameByPersonId[split.personId] ?: "Unknown", split.shareMinor)
                },
                syncStatus = expense.syncStatus,
                canRetry = expense.syncStatus == SyncStatus.FAILED,
                isLoading = false,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ExpenseDetailUiState())

    fun retry() {
        viewModelScope.launch { expenseRepository.retrySync(expenseId) }
    }
}
