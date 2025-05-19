package navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ui.screens.AddEditNoteScreen
import ui.screens.HomeScreen
import ui.screens.LoginScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("home") },
                onRegisterSuccess = { navController.navigate("home") }
            )
        }
        composable("home") {
            HomeScreen(
                onAddNote = { navController.navigate("add_edit_note/null") },
                onEditNote = { noteId -> navController.navigate("add_edit_note/$noteId") }
            )
        }
        composable(
            route = "add_edit_note/{noteId}?",
            arguments = listOf(navArgument("noteId") { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            AddEditNoteScreen(
                noteId = noteId,
                onNoteSaved = { navController.popBackStack() }
            )
        }
    }
}