package com.example.studychessapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.studychessapp.network.AuthViewModel
import com.example.studychessapp.screens.HomeScreen
import com.example.studychessapp.ui.ChessBoard
import com.example.studychessapp.screens.LessonScreen
import com.example.studychessapp.screens.LessonListScreen // ✅ THÊM IMPORT
import com.example.studychessapp.screens.PlayWithAIScreen
import com.example.studychessapp.screens.PracticeBoardScreen
import com.example.studychessapp.screens.ProfileScreen

@Composable
fun NavGraph(
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {

        composable("home") {
            HomeScreen(navController = navController, authViewModel = authViewModel)
        }

        composable("profile") {
            ProfileScreen(navController = navController, authViewModel = authViewModel)
        }

        // ✅ SỬA "lesson" thành "lesson_list"
        composable("lesson_list") {
            LessonListScreen(navController = navController)
        }

        // ✅ THÊM TUYẾN MỚI cho chi tiết bài học
        composable(
            "lesson_detail/{lessonId}",
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId")
            LessonScreen(navController = navController, lessonId = lessonId)
        }

        composable("board") {
            PracticeBoardScreen(navController = navController)
        }

        composable("play_ai") {
            PlayWithAIScreen(navController = navController)
        }
    }
}