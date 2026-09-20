package com.miir.remote.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.miir.remote.AppContainer
import com.miir.remote.ui.add.AddRemoteScreen
import com.miir.remote.ui.control.RemoteControlScreen
import com.miir.remote.ui.group.GroupManageScreen
import com.miir.remote.ui.home.HomeScreen

object Routes {
    const val HOME = "home"
    const val ADD_REMOTE = "add_remote"
    const val GROUPS = "groups"
    const val CONTROL = "control/{remoteId}"
    fun control(id: Long) = "control/$id"
}

@Composable
fun MiNavHost(container: AppContainer) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                onAddRemote = { nav.navigate(Routes.ADD_REMOTE) },
                onManageGroups = { nav.navigate(Routes.GROUPS) },
                onOpenControl = { nav.navigate(Routes.control(it)) },
                container = container
            )
        }

        composable(Routes.ADD_REMOTE) {
            AddRemoteScreen(
                onSaved = { nav.popBackStack() },
                onBack = { nav.popBackStack() },
                container = container
            )
        }

        composable(Routes.GROUPS) {
            GroupManageScreen(
                onBack = { nav.popBackStack() },
                container = container
            )
        }

        composable(
            route = Routes.CONTROL,
            arguments = listOf(navArgument("remoteId") { type = NavType.LongType })
        ) { backStack ->
            val remoteId = backStack.arguments?.getLong("remoteId") ?: return@composable
            RemoteControlScreen(
                remoteId = remoteId,
                onBack = { nav.popBackStack() },
                onDeleted = { nav.popBackStack() },
                container = container
            )
        }
    }
}
