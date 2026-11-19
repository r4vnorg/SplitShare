package com.example.splitshare.viewmodel

import com.example.splitshare.data.AuthProvider


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class UserUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
)

class UserDetailViewModel(private val authProvider: AuthProvider) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authProvider.getUser()?.let {
                _uiState.value = UserUiState(
                    name = it.name,
                    email = it.email,
                    phone = it.phone
                )
            }
        }
    }

    fun logOut(afterLogout: () -> Unit) {
        viewModelScope.launch {
            authProvider.logOut()
            afterLogout()
        }
    }
}
