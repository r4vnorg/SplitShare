package com.example.splitshare.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitshare.data.GroupRepository
import com.example.splitshare.domain.model.GroupSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class GroupListUiState(
    val groups: List<GroupSummary> = emptyList(),
    val isLoading: Boolean = true,
)

/** S4 Group list (home screen): every group the device owner belongs to, read from local data. */
class GroupListViewModel(groupRepository: GroupRepository) : ViewModel() {

    val uiState: StateFlow<GroupListUiState> = groupRepository.observeGroups()
        .map { groups -> GroupListUiState(groups = groups, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, GroupListUiState())
}
