package com.example.splitshare.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.splitshare.viewmodel.AppViewModelProvider
import com.example.splitshare.viewmodel.ManageMembersViewModel

/** S7 Manage members (US5): add/remove participants. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageMembersScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ManageMembersViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Members") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (uiState.removeError != null) {
                Text(text = uiState.removeError.orEmpty(), color = MaterialTheme.colorScheme.error)
            }

            LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                items(uiState.members, key = { it.personId }) { member ->
                    ListItem(
                        headlineContent = { Text(member.name) },
                        supportingContent = { Text(member.email) },
                        trailingContent = {
                            IconButton(onClick = { viewModel.removeParticipant(member.personId) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove ${member.name}")
                            }
                        },
                    )
                }
            }

            HorizontalDivider()
            Text(text = "Add participant", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = uiState.newPersonName,
                onValueChange = viewModel::onNewPersonNameChange,
                label = { Text("Name") },
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { error -> @Composable { Text(error) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiState.newPersonEmail,
                onValueChange = viewModel::onNewPersonEmailChange,
                label = { Text("Email") },
                isError = uiState.emailError != null,
                supportingText = uiState.emailError?.let { error -> @Composable { Text(error) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiState.newPersonPhone,
                onValueChange = viewModel::onNewPersonPhoneChange,
                label = { Text("Phone (optional)") },
                isError = uiState.phoneError != null,
                supportingText = uiState.phoneError?.let { error -> @Composable { Text(error) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
            )
            Button(onClick = viewModel::addParticipant, modifier = Modifier.fillMaxWidth()) {
                Text("Add")
            }
        }
    }
}
