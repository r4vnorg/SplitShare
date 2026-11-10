package com.example.splitshare.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitshare.data.ProfileRepository
import com.example.splitshare.domain.ProfileValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val error: String? = null,
    val noProfileFound: Boolean = false,
)

/** S1 Login (US3): mocked login that matches the email of the profile stored on the device. */
class LoginViewModel(private val profileRepository: ProfileRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, error = null, noProfileFound = false) }
    }

    fun logIn(onLoggedIn: () -> Unit) {
        val email = _uiState.value.email
        val error = ProfileValidator.emailError(email)
        if (error != null) {
            _uiState.update { it.copy(error = error) }
            return
        }
        viewModelScope.launch {
            if (profileRepository.findByEmail(email) != null) {
                onLoggedIn()
            } else {
                _uiState.update { it.copy(noProfileFound = true) }
            }
        }
    }
}
