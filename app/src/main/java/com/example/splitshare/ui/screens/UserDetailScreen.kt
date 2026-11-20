package com.example.splitshare.ui.screens

import com.example.splitshare.viewmodel.UserDetailViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var showAlertDialog by remember { mutableStateOf(false) }

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

        Button(onClick = { showAlertDialog = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Log out")
        }

        if (showAlertDialog) {
            AlertDialog(
                onDismissRequest = { showAlertDialog = false },
                title = { Text("Alert") },
                text = { Text("You are about to log out. Logging out will result in all the unsynced jobs to be deleted. Continue?") },
                confirmButton = {
                    TextButton(onClick = { viewModel.logOut(afterLogout) }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAlertDialog = false }) {
                        Text("No")
                    }
                }
            )
        }
    }
}

