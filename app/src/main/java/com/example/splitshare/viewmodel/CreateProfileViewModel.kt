package com.example.splitshare.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitshare.data.ProfileRepository
import com.example.splitshare.domain.ProfileValidator
import com.example.splitshare.domain.model.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class CreateProfileUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
)

/** S2 Create profile (US1): name and contact details, stored on the device. */
class CreateProfileViewModel(private val profileRepository: ProfileRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateProfileUiState())
    val uiState: StateFlow<CreateProfileUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) = _uiState.update { it.copy(name = name, nameError = null) }

    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email, emailError = null) }

    fun onPhoneChange(phone: String) = _uiState.update { it.copy(phone = phone, phoneError = null) }

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        val nameError = ProfileValidator.nameError(state.name)
        val emailError = ProfileValidator.emailError(state.email)
        val phoneError = ProfileValidator.phoneError(state.phone)
        if (nameError != null || emailError != null || phoneError != null) {
            _uiState.update {
                it.copy(nameError = nameError, emailError = emailError, phoneError = phoneError)
            }
            return
        }
        viewModelScope.launch {
            profileRepository.save(
                Profile(
                    id = UUID.randomUUID().toString(),
                    name = state.name.trim(),
                    email = state.email.trim(),
                    phone = state.phone.trim(),
                )
            )
            onSaved()
        }
    }
}
