package com.example.studychessapp.screens

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.studychessapp.network.AuthViewModel
import com.example.studychessapp.network.UserData


fun generateExportContent(data: UserData): String {
    return """
    ==================================================
    BÁO CÁO DỮ LIỆU TÀI KHOẢN STUDY CHESS
    Thời gian xuất: ${java.util.Date()}
    ==================================================
    ID Tài khoản: ${data.id}
    Tên đăng nhập: ${data.tenDangNhap ?: "N/A"}
    Họ tên: ${data.hoTen ?: "N/A"}
    Email: ${data.email ?: "N/A"}
    Số điện thoại: ${data.soDienThoai ?: "N/A"}
    Đường dẫn Avatar: ${data.avatarUrl ?: "Không có"}
    ==================================================
    """
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val userSession by authViewModel.userSession.collectAsState()
    val isLoggedIn = userSession.isLoggedIn
    val userData = userSession.userData// ✅ Lấy toàn bộ UserData

    val context = LocalContext.current

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            navController.navigate("home") {
                popUpTo("home") { inclusive = true }
            }
        }
    }

    // 1. LAUNCHER VÀ START EXPORT PROCESS (Giữ nguyên)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        // ⚠️ SỬA: Phải đảm bảo userData không null trước khi gọi
                        val content = generateExportContent(userData!!)
                        outputStream.write(content.toByteArray())
                    }
                    Toast.makeText(context, "Xuất dữ liệu thành công!", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Lỗi ghi file: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun startExportProcess() {
        if (userData == null) {
            Toast.makeText(context, "Không tìm thấy dữ liệu để xuất.", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "Chess_User_${userData.id}_Profile.txt")
        }
        exportLauncher.launch(intent)
    }

    // ... (Scaffold và TopAppBar giữ nguyên)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hồ sơ Cá nhân") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trở về")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // ✅ SỬ DỤNG paddingValues
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(32.dp))

            // 2. AVATAR VÀ TÊN
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Avatar",
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = userData?.tenDangNhap ?: "Người dùng", // ✅ Dùng userData
                style = MaterialTheme.typography.headlineMedium
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // 3. CHI TIẾT THÔNG TIN
            ProfileDetail(label = "ID", value = userData?.id?.toString() ?: "N/A") // ✅ Dùng userData
            ProfileDetail(label = "Họ tên", value = userData?.hoTen ?: "N/A")
            ProfileDetail(label = "Email", value = userData?.email ?: "N/A")
            ProfileDetail(label = "SĐT", value = userData?.soDienThoai ?: "N/A")


            Spacer(modifier = Modifier.height(32.dp))

            // 4. NÚT CHỨC NĂNG
            Button(
                onClick = { authViewModel.logout() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Đăng Xuất")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { startExportProcess() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("📁 Xuất Dữ Liệu Tài Khoản (.txt)")
            }
        }
    }
}

@Composable
fun ProfileDetail(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}