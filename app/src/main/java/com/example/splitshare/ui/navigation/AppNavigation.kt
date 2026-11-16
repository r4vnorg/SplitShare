package com.example.splitshare.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.example.splitshare.ui.screens.AddExpensePayerAmountScreen
import com.example.splitshare.ui.screens.CreateGroupScreen
import com.example.splitshare.ui.screens.CreateProfileScreen
import com.example.splitshare.ui.screens.GroupDetailScreen
import com.example.splitshare.ui.screens.GroupListScreen
import com.example.splitshare.ui.screens.LoginScreen
import com.example.splitshare.ui.screens.ManageMembersScreen
import com.example.splitshare.viewmodel.AppViewModelProvider

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
            GroupListScreen(
                onCreateGroup = { navController.navigate(Routes.CREATE_GROUP) },
                onGroupClick = { groupId -> navController.navigate(Routes.groupDetail(groupId)) },
                // TODO US2: navigate to Profile & notification settings once it exists.
                onProfileClick = {},
            )
        }
        composable(Routes.CREATE_GROUP) {
            CreateGroupScreen(onSaved = { navController.popBackStack() })
        }
        composable(
            route = Routes.GROUP_DETAIL,
            arguments = listOf(navArgument(Routes.GROUP_ID_ARG) { type = NavType.StringType }),
        ) { backStackEntry ->
            val groupId = checkNotNull(backStackEntry.arguments?.getString(Routes.GROUP_ID_ARG))
            GroupDetailScreen(
                onBack = { navController.popBackStack() },
                onMembersClick = { navController.navigate(Routes.manageMembers(groupId)) },
                onAddExpenseClick = { navController.navigate(Routes.addExpenseGraph(groupId)) },
                // TODO US7: navigate to Expense Detail once it exists.
                onExpenseClick = {},
            )
        }
        composable(
            route = Routes.MANAGE_MEMBERS,
            arguments = listOf(navArgument(Routes.GROUP_ID_ARG) { type = NavType.StringType }),
        ) {
            ManageMembersScreen(onBack = { navController.popBackStack() })
        }
        navigation(
            route = Routes.ADD_EXPENSE_GRAPH,
            startDestination = Routes.ADD_EXPENSE_PAYER_AMOUNT,
            arguments = listOf(navArgument(Routes.GROUP_ID_ARG) { type = NavType.StringType }),
        ) {
            composable(Routes.ADD_EXPENSE_PAYER_AMOUNT) { backStackEntry ->
                val graphEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.ADD_EXPENSE_GRAPH)
                }
                AddExpensePayerAmountScreen(
                    onBack = { navController.popBackStack() },
                    // TODO US7: navigate to the select-split step once it exists.
                    onNext = {},
                    viewModel = viewModel(factory = AppViewModelProvider.Factory, viewModelStoreOwner = graphEntry),
                )
            }
        }
    }
}
