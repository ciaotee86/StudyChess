package com.example.studychessapp.screens

//import RegisterDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.studychessapp.R
import com.example.yourapp.ui.LoginDialog
import com.example.studychessapp.network.ApiResponse
import com.example.studychessapp.network.RetrofitClient
import com.example.studychessapp.network.ApiServices
import com.example.studychessapp.network.AuthViewModel
import com.example.studychessapp.screens.RegisterDialog

@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val userSession by authViewModel.userSession.collectAsState()
    val isLoggedIn = userSession.isLoggedIn
    val currentUserName = userSession.userName

    var showLoginDialog by remember { mutableStateOf(false) }
    var showRegisterDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // --- 1. BANNER VÀ TEXT ---
            Image(
                painter = painterResource(id = R.drawable.logodhkt),
                contentDescription = "Chess Banner",
                modifier = Modifier
                    .height(350.dp)
                    .clip(RoundedCornerShape(30.dp))
            )

            Text("Study Chess", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("Ứng dụng học cờ vua", fontSize = 20.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. LOGIC HIỂN THỊ CÁC NÚT ---
            if (isLoggedIn) {
                // ĐÃ ĐĂNG NHẬP: HIỆN CHỨC NĂNG + ĐĂNG XUẤT

                // Nút 1: Bài học
                Button(onClick = { navController.navigate("lesson_list") }, // ✅ SỬA Ở ĐÂY
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("📖 Bài học", fontSize = 18.sp) }

                // Nút 2: Bàn cờ luyện tập
                Button(onClick = { navController.navigate("board") },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) { Text("♟️ Bàn cờ luyện tập ", fontSize = 18.sp, color = Color.White) }

                // Nút 3: Chơi với AI
                Button(onClick = { navController.navigate("play_ai") },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) { Text("🤖 Chơi với AI", fontSize = 18.sp, color = Color.White) }



            } else {
                // CHƯA ĐĂNG NHẬP: CHỈ HIỆN ĐĂNG NHẬP VÀ ĐĂNG KÝ

                // Nút 1: Đăng nhập
                Button(
                    onClick = { showLoginDialog = true },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("Đăng nhập", fontSize = 18.sp, color = Color.White) }

                // Nút 2: Đăng ký
                Button(

                    onClick = { showRegisterDialog = true },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) { Text("Đăng ký", fontSize = 18.sp, color = Color.White) }
            }
        }


        if (isLoggedIn) {
            IconButton(
                onClick = { navController.navigate("profile") },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Tài khoản người dùng",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // --- 4. DIALOGS ---
        if (showLoginDialog) LoginDialog(
            onDismiss = { showLoginDialog = false },

            onLoginSuccess = { user ->
                authViewModel.setLoggedIn(user)
                showLoginDialog = false
            }
        )


        if (showRegisterDialog) RegisterDialog(onDismiss = { showRegisterDialog = false })
    }
}
