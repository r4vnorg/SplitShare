package com.example.splitshare.viewmodel

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.splitshare.SplitShareApplication
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

/** Creates ViewModels with their dependencies from [com.example.splitshare.AppContainer]. */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { LoginViewModel(splitShareApplication().container.authProvider) }
        initializer { CreateProfileViewModel(splitShareApplication().container.authProvider) }
        initializer { GroupListViewModel(splitShareApplication().container.groupRepository) }
        initializer { CreateGroupViewModel(splitShareApplication().container.groupRepository) }
        initializer {
            GroupDetailViewModel(
                savedStateHandle = createSavedStateHandle(),
                groupRepository = splitShareApplication().container.groupRepository,
                reminderRepository = splitShareApplication().container.reminderRepository,
                currentUserEmail = flow { emit(splitShareApplication().container.authProvider.getUser()?.email) }
            )
        }
        initializer {
            ManageMembersViewModel(
                savedStateHandle = createSavedStateHandle(),
                groupRepository = splitShareApplication().container.groupRepository,
                expenseRepository = splitShareApplication().container.expenseRepository,
                personRepository = splitShareApplication().container.personRepository,
            )
        }
        initializer {
            AddExpenseViewModel(
                savedStateHandle = createSavedStateHandle(),
                groupRepository = splitShareApplication().container.groupRepository,
                expenseRepository = splitShareApplication().container.expenseRepository,
            )
        }
        initializer {
            ExpenseDetailViewModel(
                savedStateHandle = createSavedStateHandle(),
                expenseRepository = splitShareApplication().container.expenseRepository,
                groupRepository = splitShareApplication().container.groupRepository,
            )
        }

        initializer {
            UserDetailViewModel(
                authProvider = splitShareApplication().container.authProvider
            )
        }
    }
}

private fun CreationExtras.splitShareApplication(): SplitShareApplication =
    this[APPLICATION_KEY] as SplitShareApplication
