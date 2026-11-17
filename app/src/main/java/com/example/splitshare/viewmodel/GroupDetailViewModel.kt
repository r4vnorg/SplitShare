package com.example.splitshare.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitshare.data.ExpenseRepository
import com.example.splitshare.data.GroupRepository
import com.example.splitshare.data.ReminderRepository
import com.example.splitshare.domain.BalanceCalculator
import com.example.splitshare.domain.model.Expense
import com.example.splitshare.domain.model.Group
import com.example.splitshare.domain.model.Participant
import com.example.splitshare.domain.model.SyncStatus
import com.example.splitshare.ui.navigation.Routes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class GroupDetailTab { TRANSACTIONS, BALANCES }

data class ExpenseSummary(
    val id: String,
    val description: String,
    val amountMinor: Long,
    val payerName: String,
    val syncStatus: SyncStatus,
)

/** One row of the Balances tab. [owedToMeMinor] > 0 means this member owes the current user (US12: Remind). */
data class MemberBalance(
    val personId: String,
    val name: String,
    val netMinor: Long,
    val owedToMeMinor: Long,
)

data class GroupDetailUiState(
    val groupName: String = "",
    val selectedTab: GroupDetailTab = GroupDetailTab.TRANSACTIONS,
    val expenses: List<ExpenseSummary> = emptyList(),
    val balances: List<MemberBalance> = emptyList(),
    val message: String? = null,
    val isLoading: Boolean = true,
)

/** S6 Group detail (Transactions / Balances tabs). Balances is US9 - this just reserves the tab. */
class GroupDetailViewModel(
    savedStateHandle: SavedStateHandle,
    groupRepository: GroupRepository,
    expenseRepository: ExpenseRepository,
    private val reminderRepository: ReminderRepository,
    currentUserEmail: Flow<String?>,
) : ViewModel() {

    private val groupId: String = checkNotNull(savedStateHandle[Routes.GROUP_ID_ARG])

    private val selectedTab = MutableStateFlow(GroupDetailTab.TRANSACTIONS)
    private val message = MutableStateFlow<String?>(null)
    private var groupName = ""

    val uiState: StateFlow<GroupDetailUiState> = combine(
        groupRepository.observeGroup(groupId),
        groupRepository.observeParticipants(groupId),
        expenseRepository.observeExpenses(groupId),
        selectedTab,
        currentUserEmail,
        message,
    ) { values ->
        val group = values[0] as Group?
        @Suppress("UNCHECKED_CAST") val participants = values[1] as List<Participant>
        @Suppress("UNCHECKED_CAST") val expenses = values[2] as List<Expense>
        val tab = values[3] as GroupDetailTab
        val email = values[4] as String?
        val nameByPersonId = participants.associate { it.person.id to it.person.name }
        val me = participants.firstOrNull { it.person.email.equals(email, ignoreCase = true) }?.person?.id
        val debtors = me?.let { BalanceCalculator.debtorsOf(it, participants.map { p -> p.person.id }, expenses) }.orEmpty()
        groupName = group?.name.orEmpty()
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
            balances = participants.map { participant ->
                MemberBalance(
                    personId = participant.person.id,
                    name = participant.person.name,
                    netMinor = BalanceCalculator.netBalance(participant.person.id, expenses),
                    owedToMeMinor = debtors[participant.person.id] ?: 0L,
                )
            },
            message = values[5] as String?,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, GroupDetailUiState())

    fun selectTab(tab: GroupDetailTab) {
        selectedTab.value = tab
    }

    /** US12: queue a reminder to a member who owes the current user. */
    fun sendReminder(personId: String) {
        val balance = uiState.value.balances.firstOrNull { it.personId == personId } ?: return
        if (balance.owedToMeMinor <= 0L) return
        viewModelScope.launch {
            reminderRepository.sendReminder(groupId, personId, balance.name, balance.owedToMeMinor)
            message.value = "Reminder to ${balance.name} queued. You'll be notified when it is sent."
        }
    }

    fun messageShown() {
        message.value = null
    }
}
