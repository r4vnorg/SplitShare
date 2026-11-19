package com.example.splitshare.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitshare.data.AuthProvider
import com.example.splitshare.domain.ProfileValidator
import com.example.splitshare.network.NetworkStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val error: String? = null,
)

/**
 * S1 Login (US3): mocked login that matches the email of the profile stored on the device.
 * Per the navigation model, logging in is the single decision point: a match goes to Group
 * List, no match goes straight to Create Profile (no separate "Create profile" action).
 */
class LoginViewModel(private val authProvider: AuthProvider) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    fun logIn(onLoggedIn: () -> Unit) {
        val email = _uiState.value.email
        val error = ProfileValidator.emailError(email)
        if (error != null) {
            _uiState.update { it.copy(error = error) }
            return
        }
        viewModelScope.launch {

            when (val result = authProvider.tryAuth(email)) {
                is NetworkStatus.Success<*> -> onLoggedIn()
                is NetworkStatus.Error -> _uiState.update { it.copy(error = result.message) }
            }
        }
    }
}
