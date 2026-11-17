package com.example.splitshare.viewmodel

import com.example.splitshare.data.FakeGroupRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CreateGroupViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeGroupRepository()
    private val viewModel = CreateGroupViewModel(repository)

    @Test
    fun blankNameBlocksSaveAndSetsFieldError() {
        viewModel.onNameChange("   ")
        var saved = false

        viewModel.save { saved = true }

        assertFalse(saved)
        assertEquals("Name is required", viewModel.uiState.value.nameError)
        assertTrue(repository.createdGroups.isEmpty())
    }

    @Test
    fun descriptionOverLimitBlocksSave() {
        viewModel.onNameChange("Cabin trip")
        viewModel.onDescriptionChange("a".repeat(281))
        var saved = false

        viewModel.save { saved = true }

        assertFalse(saved)
        assertEquals("Description must be 280 characters or fewer", viewModel.uiState.value.descriptionError)
        assertTrue(repository.createdGroups.isEmpty())
    }

    @Test
    fun validInputSavesAndInvokesCallback() {
        viewModel.onNameChange("Cabin trip")
        viewModel.onDescriptionChange("Weekend getaway")
        var saved = false

        viewModel.save { saved = true }

        assertTrue(saved)
        assertEquals(1, repository.createdGroups.size)
        assertEquals("Cabin trip", repository.createdGroups.single().name)
        assertEquals("Weekend getaway", repository.createdGroups.single().description)
    }

    @Test
    fun changingAFieldClearsItsOwnError() {
        viewModel.onNameChange("")
        viewModel.save {}
        assertEquals("Name is required", viewModel.uiState.value.nameError)

        viewModel.onNameChange("Cabin trip")

        assertEquals(null, viewModel.uiState.value.nameError)
    }
}
