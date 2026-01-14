package com.example.studychessapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.studychessapp.network.AuthViewModel
import com.example.studychessapp.screens.HomeScreen
import com.example.studychessapp.screens.LessonListScreen
import com.example.studychessapp.screens.LessonScreen
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
            val userSession by authViewModel.userSession.collectAsState()

            ProfileScreen(
                navController = navController,
                userData = userSession.userData,
                onLogout = {
                    // Xử lý đăng xuất
                    authViewModel.logout()
                    // Quay về màn hình Home và xóa backstack để không back lại được Profile
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onUserUpdated = { newUser ->
                    // Cập nhật lại ViewModel khi có thay đổi (ví dụ đổi avatar)
                    authViewModel.updateUser(newUser)
                }
            )
        }

        composable("lesson_list") {
            LessonListScreen(navController = navController)
        }

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