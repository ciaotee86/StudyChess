package com.example.studychessapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.studychessapp.network.AuthViewModel
import com.example.studychessapp.screens.HomeScreen
import com.example.studychessapp.ui.ChessBoard
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

        // ✅ 2. PROFILE SCREEN (Tuyến bị thiếu, gây crash)
        composable("profile") {
            // Giả định ProfileScreen cần AuthViewModel
            ProfileScreen(navController = navController, authViewModel = authViewModel)
        }

        // 2. BÀI HỌC (Danh sách)
        composable("lesson") {

            LessonScreen(navController = navController)
        }

        // 3. BÀN CỜ LUYỆN TẬP
        composable("board") {

            PracticeBoardScreen(navController = navController)
        }

        // 4. CHƠI VỚI AI
        composable("play_ai") {
            PlayWithAIScreen(navController = navController)
        }

    }
}

