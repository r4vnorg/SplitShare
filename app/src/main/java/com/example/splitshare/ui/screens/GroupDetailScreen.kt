package com.example.splitshare.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.splitshare.domain.MoneyFormatter
import com.example.splitshare.viewmodel.AppViewModelProvider
import com.example.splitshare.viewmodel.GroupDetailTab
import com.example.splitshare.viewmodel.GroupDetailViewModel

/** S6 Group detail (Transactions / Balances tabs). Balances is US9 - shown here as a placeholder. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    onBack: () -> Unit,
    onMembersClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onExpenseClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GroupDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.messageShown()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(uiState.groupName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onMembersClick) { Text("Members") }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddExpenseClick) {
                Icon(Icons.Default.Add, contentDescription = "Add expense")
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            TabRow(selectedTabIndex = uiState.selectedTab.ordinal) {
                Tab(
                    selected = uiState.selectedTab == GroupDetailTab.TRANSACTIONS,
                    onClick = { viewModel.selectTab(GroupDetailTab.TRANSACTIONS) },
                    text = { Text("Transactions") },
                )
                Tab(
                    selected = uiState.selectedTab == GroupDetailTab.BALANCES,
                    onClick = { viewModel.selectTab(GroupDetailTab.BALANCES) },
                    text = { Text("Balances") },
                )
            }

            when (uiState.selectedTab) {
                GroupDetailTab.TRANSACTIONS -> {
                    if (uiState.expenses.isEmpty() && !uiState.isLoading) {
                        Text(
                            text = "No expenses yet. Tap + to add one.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(uiState.expenses, key = { it.id }) { expense ->
                                ListItem(
                                    headlineContent = { Text(expense.description.ifBlank { "Expense" }) },
                                    supportingContent = {
                                        Text("${expense.payerName} paid ${MoneyFormatter.format(expense.amountMinor)}")
                                    },
                                    trailingContent = { Text(expense.syncStatus.name) },
                                    modifier = Modifier.clickable { onExpenseClick(expense.id) },
                                )
                            }
                        }
                    }
                }
                GroupDetailTab.BALANCES -> {
                    // Minimal balances list; the full Balances view/settlement is US9.
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.balances, key = { it.personId }) { balance ->
                            ListItem(
                                headlineContent = { Text(balance.name) },
                                supportingContent = {
                                    Text(
                                        when {
                                            balance.netMinor > 0 -> "is owed ${MoneyFormatter.format(balance.netMinor)}"
                                            balance.netMinor < 0 -> "owes ${MoneyFormatter.format(-balance.netMinor)}"
                                            else -> "settled up"
                                        }
                                    )
                                },
                                trailingContent = {
                                    // US12: Remind only for members who owe the current user (conditional connector S6 -> S10).
                                    if (balance.owedToMeMinor > 0) {
                                        TextButton(onClick = { viewModel.sendReminder(balance.personId) }) { Text("Remind") }
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
