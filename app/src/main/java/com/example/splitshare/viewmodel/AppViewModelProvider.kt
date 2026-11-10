package com.example.splitshare.viewmodel

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.splitshare.SplitShareApplication

/** Creates ViewModels with their dependencies from [com.example.splitshare.AppContainer]. */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { LoginViewModel(splitShareApplication().container.profileRepository) }
        initializer { CreateProfileViewModel(splitShareApplication().container.profileRepository) }
    }
}

private fun CreationExtras.splitShareApplication(): SplitShareApplication =
    this[APPLICATION_KEY] as SplitShareApplication
