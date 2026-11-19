package com.example.splitshare.ui.screens

import com.example.splitshare.viewmodel.UserDetailViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.splitshare.viewmodel.AppViewModelProvider

@Composable
fun UserDetailScreen(
    afterLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UserDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Profile", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            enabled = false,
            value = uiState.name,
            onValueChange = { },
            label = @Composable { Text("Name") },
            isError = false,
            supportingText = { },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            enabled = false,
            value = uiState.email,
            onValueChange = { },
            label = @Composable { Text("Email") },
            isError = false,
            supportingText = { },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            enabled = false,
            value = uiState.phone,
            onValueChange = { },
            label = @Composable { Text("Phone (optional)") },
            isError = false,
            supportingText = { },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )

        Button(onClick = { viewModel.logOut(afterLogout) }, modifier = Modifier.fillMaxWidth()) {
            Text("Log out")
        }
    }
}
