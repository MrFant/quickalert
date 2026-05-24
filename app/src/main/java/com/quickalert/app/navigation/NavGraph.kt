package com.quickalert.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.quickalert.app.ui.screens.RuleEditScreen
import com.quickalert.app.ui.screens.RuleListScreen

object Routes {
    const val RULE_LIST = "rule_list"
    const val RULE_EDIT = "rule_edit?ruleId={ruleId}"

    fun ruleEdit(ruleId: Long? = null): String {
        return if (ruleId != null) "rule_edit?ruleId=$ruleId" else "rule_edit"
    }
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.RULE_LIST
    ) {
        composable(Routes.RULE_LIST) {
            RuleListScreen(
                onAddRule = { navController.navigate(Routes.ruleEdit()) },
                onEditRule = { ruleId -> navController.navigate(Routes.ruleEdit(ruleId)) }
            )
        }

        composable(
            route = Routes.RULE_EDIT,
            arguments = listOf(
                navArgument("ruleId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val ruleId = backStackEntry.arguments?.getLong("ruleId") ?: -1L
            RuleEditScreen(
                ruleId = if (ruleId == -1L) null else ruleId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
