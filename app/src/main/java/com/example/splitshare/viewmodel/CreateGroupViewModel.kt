package com.example.splitshare.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitshare.data.GroupRepository
import com.example.splitshare.domain.GroupValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateGroupUiState(
    val name: String = "",
    val description: String = "",
    val nameError: String? = null,
    val descriptionError: String? = null,
)

/** S5 Create group (US4): name and description, saved locally and queued for sync. */
class CreateGroupViewModel(private val groupRepository: GroupRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateGroupUiState())
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) = _uiState.update { it.copy(name = name, nameError = null) }

    fun onDescriptionChange(description: String) =
        _uiState.update { it.copy(description = description, descriptionError = null) }

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        val nameError = GroupValidator.nameError(state.name)
        val descriptionError = GroupValidator.descriptionError(state.description)
        if (nameError != null || descriptionError != null) {
            _uiState.update { it.copy(nameError = nameError, descriptionError = descriptionError) }
            return
        }
        viewModelScope.launch {
            groupRepository.createGroup(state.name.trim(), state.description.trim())
            onSaved()
        }
    }
}
