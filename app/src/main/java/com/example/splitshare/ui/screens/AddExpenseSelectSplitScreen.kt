package com.example.splitshare.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.splitshare.viewmodel.AddExpenseViewModel
import com.example.splitshare.viewmodel.SplitMode

/** S9 Add expense, step 2 of 2 (US7): who's included and how the amount is split. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseSelectSplitScreen(
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddExpenseViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Split expense") },
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.splitMode == SplitMode.EQUAL,
                    onClick = { viewModel.setSplitMode(SplitMode.EQUAL) },
                    label = { Text("Equal") },
                )
                FilterChip(
                    selected = uiState.splitMode == SplitMode.CUSTOM,
                    onClick = { viewModel.setSplitMode(SplitMode.CUSTOM) },
                    label = { Text("Custom") },
                )
            }

            if (uiState.splitError != null) {
                Text(text = uiState.splitError.orEmpty(), color = MaterialTheme.colorScheme.error)
            }

            Column {
                uiState.splitMembers.forEach { member ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Checkbox(
                            checked = member.isSelected,
                            onCheckedChange = { viewModel.toggleMemberSelected(member.personId) },
                        )
                        Text(text = member.name, modifier = Modifier.weight(1f))
                        if (member.isSelected && uiState.splitMode == SplitMode.CUSTOM) {
                            OutlinedTextField(
                                value = member.shareText,
                                onValueChange = { viewModel.onCustomShareChange(member.personId, it) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.width(100.dp),
                            )
                        } else {
                            Text(text = member.shareText, modifier = Modifier.width(100.dp))
                        }
                    }
                }
            }

            Button(onClick = { viewModel.saveSplit(onSaved) }, modifier = Modifier.fillMaxWidth()) {
                Text("Save")
            }
        }
    }
}
