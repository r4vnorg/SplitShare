package com.example.splitshare.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import com.example.splitshare.domain.model.SyncStatus
import com.example.splitshare.viewmodel.AppViewModelProvider
import com.example.splitshare.viewmodel.GroupListViewModel

/** S4 Group list (home screen, US4). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupListScreen(
    onCreateGroup: () -> Unit,
    onGroupClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GroupListViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Groups") },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateGroup) {
                Icon(Icons.Default.Add, contentDescription = "New group")
            }
        },
    ) { innerPadding ->
        if (uiState.groups.isEmpty() && !uiState.isLoading) {
            Text(
                text = "No groups yet. Tap + to create one.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                items(uiState.groups, key = { it.group.id }) { info ->
                    ListItem(
                        headlineContent = { Text(info.group.name) },
                        supportingContent = {
                            when(info.group.syncStatus) {
                                SyncStatus.SYNCED -> {
                                    val memberWord =
                                        if (info.participants.size == 1) "member" else "members"
                                    Text("${info.participants.size} $memberWord")
                                }

                                SyncStatus.FAILED -> Text("SYNC FAILED")
                                else -> Text("Syncing")
                            }
                        },
                        modifier =
                            when(info.group.syncStatus) {
                                SyncStatus.SYNCED -> Modifier.clickable { onGroupClick(info.group.id) }
                                SyncStatus.PENDING -> Modifier.clickable { onGroupClick(info.group.id) }
                                else -> Modifier.clickable {}
                            }
                    )
                }
            }
        }
    }
}
