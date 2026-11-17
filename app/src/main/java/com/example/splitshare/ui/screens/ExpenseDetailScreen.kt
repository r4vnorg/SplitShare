package com.example.splitshare.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.splitshare.domain.MoneyFormatter
import com.example.splitshare.viewmodel.AppViewModelProvider
import com.example.splitshare.viewmodel.ExpenseDetailViewModel

/** S10 Expense detail (US7): shows sync status, with Retry shown only when it's FAILED. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExpenseDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Expense") },
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
            Text(
                text = uiState.description.ifBlank { "Expense" },
                style = MaterialTheme.typography.headlineMedium,
            )
            Text("${uiState.payerName} paid ${MoneyFormatter.format(uiState.amountMinor)}")
            Text("Status: ${uiState.syncStatus.name}")

            if (uiState.canRetry) {
                Button(onClick = viewModel::retry) {
                    Text("Retry")
                }
            }

            HorizontalDivider()
            Text(text = "Split", style = MaterialTheme.typography.titleMedium)
            uiState.splits.forEach { split ->
                Text("${split.personName}: ${MoneyFormatter.format(split.shareMinor)}")
            }
        }
    }
}
