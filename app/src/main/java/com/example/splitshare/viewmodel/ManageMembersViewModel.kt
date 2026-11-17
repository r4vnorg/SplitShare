package com.example.splitshare.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitshare.data.ExpenseRepository
import com.example.splitshare.data.GroupRepository
import com.example.splitshare.data.PersonRepository
import com.example.splitshare.domain.BalanceCalculator
import com.example.splitshare.domain.MoneyFormatter
import com.example.splitshare.domain.ProfileValidator
import com.example.splitshare.ui.navigation.Routes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MemberRow(val personId: String, val name: String, val email: String)

private data class ManageMembersFormState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val removeError: String? = null,
)

data class ManageMembersUiState(
    val members: List<MemberRow> = emptyList(),
    val newPersonName: String = "",
    val newPersonEmail: String = "",
    val newPersonPhone: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val removeError: String? = null,
)

/** S7 Manage members (US5): add/remove participants. Remove is blocked unless balance is zero. */
class ManageMembersViewModel(
    savedStateHandle: SavedStateHandle,
    private val groupRepository: GroupRepository,
    private val expenseRepository: ExpenseRepository,
    private val personRepository: PersonRepository,
) : ViewModel() {

    private val groupId: String = checkNotNull(savedStateHandle[Routes.GROUP_ID_ARG])

    private val formState = MutableStateFlow(ManageMembersFormState())

    val uiState: StateFlow<ManageMembersUiState> = combine(
        groupRepository.observeParticipants(groupId),
        formState,
    ) { participants, form ->
        ManageMembersUiState(
            members = participants.map { MemberRow(it.person.id, it.person.name, it.person.email) },
            newPersonName = form.name,
            newPersonEmail = form.email,
            newPersonPhone = form.phone,
            nameError = form.nameError,
            emailError = form.emailError,
            phoneError = form.phoneError,
            removeError = form.removeError,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ManageMembersUiState())

    fun onNewPersonNameChange(name: String) = formState.update { it.copy(name = name, nameError = null) }

    fun onNewPersonEmailChange(email: String) = formState.update { it.copy(email = email, emailError = null) }

    fun onNewPersonPhoneChange(phone: String) = formState.update { it.copy(phone = phone, phoneError = null) }

    fun addParticipant() {
        val form = formState.value
        val nameError = ProfileValidator.nameError(form.name)
        val emailError = ProfileValidator.emailError(form.email)
        val phoneError = ProfileValidator.phoneError(form.phone)
        if (nameError != null || emailError != null || phoneError != null) {
            formState.update { it.copy(nameError = nameError, emailError = emailError, phoneError = phoneError) }
            return
        }
        viewModelScope.launch {
            val person = personRepository.upsertAndGet(form.name, form.email, form.phone)
            groupRepository.addParticipant(groupId, person)
            formState.value = ManageMembersFormState()
        }
    }

    /** Only removes directly when the member's net balance in this group is exactly zero. */
    fun removeParticipant(personId: String) {
        viewModelScope.launch {
            val expenses = expenseRepository.observeExpenses(groupId).first()
            val balance = BalanceCalculator.netBalance(personId, expenses)
            if (balance != 0L) {
                formState.update {
                    it.copy(removeError = "Cannot remove: outstanding balance of ${MoneyFormatter.format(balance)}")
                }
                return@launch
            }
            groupRepository.removeParticipant(groupId, personId)
            formState.update { it.copy(removeError = null) }
        }
    }
}
