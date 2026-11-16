package com.example.splitshare.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitshare.data.ExpenseRepository
import com.example.splitshare.data.GroupRepository
import com.example.splitshare.domain.model.SyncStatus
import com.example.splitshare.ui.navigation.Routes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class GroupDetailTab { TRANSACTIONS, BALANCES }

data class ExpenseSummary(
    val id: String,
    val description: String,
    val amountMinor: Long,
    val payerName: String,
    val syncStatus: SyncStatus,
)

data class GroupDetailUiState(
    val groupName: String = "",
    val selectedTab: GroupDetailTab = GroupDetailTab.TRANSACTIONS,
    val expenses: List<ExpenseSummary> = emptyList(),
    val isLoading: Boolean = true,
)

/** S6 Group detail (Transactions / Balances tabs). Balances is US9 - this just reserves the tab. */
class GroupDetailViewModel(
    savedStateHandle: SavedStateHandle,
    groupRepository: GroupRepository,
    expenseRepository: ExpenseRepository,
) : ViewModel() {

    private val groupId: String = checkNotNull(savedStateHandle[Routes.GROUP_ID_ARG])

    private val selectedTab = MutableStateFlow(GroupDetailTab.TRANSACTIONS)

    val uiState: StateFlow<GroupDetailUiState> = combine(
        groupRepository.observeGroup(groupId),
        groupRepository.observeParticipants(groupId),
        expenseRepository.observeExpenses(groupId),
        selectedTab,
    ) { group, participants, expenses, tab ->
        val nameByPersonId = participants.associate { it.person.id to it.person.name }
        GroupDetailUiState(
            groupName = group?.name.orEmpty(),
            selectedTab = tab,
            expenses = expenses.map { expense ->
                ExpenseSummary(
                    id = expense.id,
                    description = expense.description,
                    amountMinor = expense.amountMinor,
                    payerName = nameByPersonId[expense.payerId] ?: "Unknown",
                    syncStatus = expense.syncStatus,
                )
            },
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, GroupDetailUiState())

    fun selectTab(tab: GroupDetailTab) {
        selectedTab.value = tab
    }
}
