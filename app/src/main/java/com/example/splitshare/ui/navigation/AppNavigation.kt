package com.example.splitshare.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.splitshare.ui.screens.CreateProfileScreen
import com.example.splitshare.ui.screens.GroupListScreen
import com.example.splitshare.ui.screens.LoginScreen

@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier = Modifier) {
    // After login or profile creation, back must not return to S1/S2 (crossbar in the navigation model).
    val goToGroupList = {
        navController.navigate(Routes.GROUP_LIST) {
            popUpTo(Routes.LOGIN) { inclusive = true }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        modifier = modifier,
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = goToGroupList,
                onCreateProfile = { navController.navigate(Routes.CREATE_PROFILE) },
            )
        }
        composable(Routes.CREATE_PROFILE) {
            CreateProfileScreen(onSaved = goToGroupList)
        }
        composable(Routes.GROUP_LIST) {
            GroupListScreen()
        }
    }
}
