package navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ui.screens.HomeScreen
import ui.screens.LoginScreen
import ui.screens.AddEditNoteScreen
@Composable
fun NavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    Log.d("NavGraph", "Navigating to home screen after login")
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    Log.d("NavGraph", "Navigating to home screen after register")
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(
                onAddNote = {
                    Log.d("NavGraph", "Navigating to add_edit_note for adding new note")
                    navController.navigate("add_edit_note/null")
                },
                onEditNote = { noteId ->
                    Log.d("NavGraph", "Navigating to add_edit_note for editing note with ID: $noteId")
                    navController.navigate("add_edit_note/$noteId")
                }
            )
        }
        composable(
            route = "add_edit_note/{noteId}?",
            arguments = listOf(navArgument("noteId") { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            Log.d("NavGraph", "Opened AddEditNoteScreen with noteId: $noteId")
            AddEditNoteScreen(
                noteId = if (noteId == "null") null else noteId,
                onNoteSaved = {
                    Log.d("NavGraph", "Note saved, navigating back to home screen")
                    navController.popBackStack()
                }
            )
        }
    }
}