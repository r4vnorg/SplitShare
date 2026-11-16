package com.example.splitshare.viewmodel

import com.example.splitshare.data.FakeGroupRepository
import com.example.splitshare.domain.model.GroupSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class GroupListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun emptyRepositoryStartsWithNoGroups() {
        val viewModel = GroupListViewModel(FakeGroupRepository())

        assertTrue(viewModel.uiState.value.groups.isEmpty())
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun uiStateReflectsRepositoryGroups() {
        val repository = FakeGroupRepository()
        repository.emitGroups(listOf(GroupSummary("g1", "Cabin trip", 3)))

        val viewModel = GroupListViewModel(repository)

        assertEquals(listOf(GroupSummary("g1", "Cabin trip", 3)), viewModel.uiState.value.groups)
    }

    @Test
    fun uiStateUpdatesWhenRepositoryEmitsAgain() {
        val repository = FakeGroupRepository()
        val viewModel = GroupListViewModel(repository)

        repository.emitGroups(listOf(GroupSummary("g1", "Cabin trip", 1)))

        assertEquals(1, viewModel.uiState.value.groups.size)
    }
}
