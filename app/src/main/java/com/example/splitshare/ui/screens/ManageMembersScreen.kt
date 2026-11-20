package com.example.splitshare.ui.screens

import android.widget.Spinner
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.example.splitshare.domain.model.SyncStatus
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
    var showAlertDialog by remember { mutableStateOf(false) }

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
                        supportingContent = {
                            when(member.syncStatus) {
                                SyncStatus.SYNCED -> Text(member.email)
                                SyncStatus.FAILED -> Text("SYNC FAILED")
                                else -> Text("Syncing")
                            } },
                        trailingContent = {
                            when(member.syncStatus) {
                                SyncStatus.SYNCED ->
                                    IconButton(onClick = { /*viewModel.removeParticipant(member.personId)*/
                                            showAlertDialog = true
                                        }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove ${member.name}")
                                    }
                                else -> null
                            }
                        },
                    )
                }
            }

            HorizontalDivider()
            Text(text = "Add participant", style = MaterialTheme.typography.titleMedium)

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
            Button(onClick = viewModel::addParticipant, modifier = Modifier.fillMaxWidth()) {
                Text("Add")
            }
        }
    }
    if (showAlertDialog) {
        AlertDialog(
            onDismissRequest = { showAlertDialog = false },
            title = { Text("Alert") },
            text = { Text("This functionality is not yet implemented") },
            confirmButton = {
                TextButton(onClick = { showAlertDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}
