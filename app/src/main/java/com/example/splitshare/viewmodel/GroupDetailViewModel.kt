package com.example.splitshare.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitshare.data.GroupRepository
import com.example.splitshare.data.ReminderRepository
import com.example.splitshare.domain.BalanceCalculator
import com.example.splitshare.domain.model.GroupDetailData
import com.example.splitshare.domain.model.SyncStatus
import com.example.splitshare.ui.navigation.Routes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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

/** Holds the four inputs that the UI state depends on. */
private data class Inputs(
    val data: GroupDetailData,
    val tab: GroupDetailTab,
    val email: String?,
    val msg: String?,
)

/** S6 Group detail (Transactions / Balances tabs). Balances is US9 - this just reserves the tab.
 *
 * The repository owns all network and caching logic via [GroupRepository.observeDetailData]:
 * - Group is fetched from the network with automatic retries and polled every 30 s.
 * - Participants and expenses are read from the local cache (updated by their own observeWithSync).
 * - The ViewModel only maps the combined data to UI state.
 */
class GroupDetailViewModel(
    savedStateHandle: SavedStateHandle,
    groupRepository: GroupRepository,
    private val reminderRepository: ReminderRepository,
    currentUserEmail: Flow<String?>,
) : ViewModel() {

    private val groupId: String = checkNotNull(savedStateHandle[Routes.GROUP_ID_ARG])

    private val selectedTab = MutableStateFlow(GroupDetailTab.TRANSACTIONS)
    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<GroupDetailUiState> = groupRepository.observeDetailData(groupId)
        .combine(selectedTab) { data, tab -> Inputs(data, tab, null, null) }
        .combine(currentUserEmail) { inputs, email -> inputs.copy(email = email) }
        .combine(message) { inputs, msg -> inputs.copy(msg = msg) }
        .map { inputs ->
            val data = inputs.data
            val tab = inputs.tab
            val email = inputs.email
            val msg = inputs.msg
            val group = data.group
            val nameByPersonId = data.participants.associate { it.person.id to it.person.name }
            val me = data.participants
                .firstOrNull { it.person.email.equals(email, ignoreCase = true) }
                ?.person?.id
            val debtors = me?.let {
                BalanceCalculator.debtorsOf(it, data.participants.map { p -> p.person.id }, data.expenses)
            }.orEmpty()
            GroupDetailUiState(
                groupName = group?.name.orEmpty(),
                selectedTab = tab,
                expenses = data.expenses.map { expense ->
                    ExpenseSummary(
                        id = expense.id,
                        description = expense.description,
                        amountMinor = expense.amountMinor,
                        payerName = nameByPersonId[expense.payerId] ?: "Unknown",
                        syncStatus = expense.syncStatus,
                    )
                },
                balances = data.participants.map { participant ->
                    MemberBalance(
                        personId = participant.person.id,
                        name = participant.person.name,
                        netMinor = BalanceCalculator.netBalance(participant.person.id, data.expenses),
                        owedToMeMinor = debtors[participant.person.id] ?: 0L,
                    )
                },
                message = msg,
                isLoading = group == null,
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
