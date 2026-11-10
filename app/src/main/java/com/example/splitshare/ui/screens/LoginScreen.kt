package com.example.splitshare.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.splitshare.viewmodel.LoginViewModel

/** S1 Login (US3). */
@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onCreateProfile: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "SplitShare", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Log in with the email on your profile.")

        OutlinedTextField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text("Email") },
            isError = uiState.error != null,
            supportingText = uiState.error?.let { error -> @Composable { Text(error) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )

        if (uiState.noProfileFound) {
            Text(
                text = "No profile found for this email. Create one to continue.",
                color = MaterialTheme.colorScheme.error,
            )
        }

        Button(onClick = { viewModel.logIn(onLoggedIn) }, modifier = Modifier.fillMaxWidth()) {
            Text("Log in")
        }
        OutlinedButton(onClick = onCreateProfile, modifier = Modifier.fillMaxWidth()) {
            Text("Create profile")
        }
    }
}
