package com.example.studychessapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType // ✅ Thêm Import NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument // ✅ Thêm Import navArgument
import com.example.studychessapp.ui.theme.StudyChessAppTheme
import com.example.studychessapp.navigation.NavGraph
import com.example.studychessapp.network.AuthViewModel


import com.google.firebase.FirebaseApp




@Composable
fun MainApp() {

    val authViewModel: AuthViewModel = viewModel()


    NavGraph(authViewModel = authViewModel)
}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        setContent {
            StudyChessAppTheme {
                MainApp()
            }
        }
    }
}