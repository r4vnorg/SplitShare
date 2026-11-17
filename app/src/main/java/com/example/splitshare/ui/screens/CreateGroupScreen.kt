package com.example.splitshare.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.splitshare.viewmodel.AppViewModelProvider
import com.example.splitshare.viewmodel.CreateGroupViewModel

/** S5 Create group (US4): name and description for a new shared occasion. */
@Composable
fun CreateGroupScreen(                                                                                                                                                                                                                                            
    onSaved: (groupId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateGroupViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Create group", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = uiState.name,
            onValueChange = viewModel::onNameChange,
            label = { Text("Name") },
            isError = uiState.nameError != null,
            supportingText = uiState.nameError?.let { error -> @Composable { Text(error) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = uiState.description,
            onValueChange = viewModel::onDescriptionChange,
            label = { Text("Description (optional)") },
            isError = uiState.descriptionError != null,
            supportingText = uiState.descriptionError?.let { error -> @Composable { Text(error) } },
            modifier = Modifier.fillMaxWidth(),
        )

        Button(onClick = { viewModel.save(onSaved) }, modifier = Modifier.fillMaxWidth()) {
            Text("Save")
        }
    }
}
