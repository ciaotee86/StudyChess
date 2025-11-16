package com.example.yourapp.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.studychessapp.network.ApiResponse
import com.example.studychessapp.network.RetrofitClient
import com.example.studychessapp.network.ApiServices
import com.example.studychessapp.network.UserData

import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Response




@Composable
fun LoginDialog(
    onDismiss: () -> Unit,
    // Hàm gọi lại sau khi đăng nhập thành công
    onLoginSuccess: (user: UserData) -> Unit
) {
    // 1. ĐỔI TÊN BIẾN TỪ 'username' SANG 'email'
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") } // ✅ Khai báo biến password ở đây

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = RetrofitClient.instance.create(ApiServices::class.java)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Đăng nhập") },
        text = {
            Column {
                OutlinedTextField(
                    // 2. DÙNG BIẾN 'email'
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Đăng Nhập") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mật khẩu") },
                    visualTransformation = PasswordVisualTransformation(), // ✅ Dùng VisualTransformation để ẩn mật khẩu
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                scope.launch {
                    try {
                        // 3. THAY ĐỔI TRUY VẤN: gửi 'email' thay vì 'username'
                        // (Bạn phải đảm bảo ApiServices.kt cũng đã được sửa để nhận tham số 'email')
                        val response: Response<ApiResponse> = api.loginUser(email, password)
                        val body = response.body()

                        if (body?.status == "success" && body.user != null) {
                            Toast.makeText(context, body.message, Toast.LENGTH_SHORT).show()
                            val user = body.user
                            // 4. GỌI CALLBACK VỚI avatarUrl (Giả sử bạn đã sửa UserData và ApiResponse)
                            onLoginSuccess(body.user)

                        } else {
                            Toast.makeText(context, body?.message ?: "Đăng nhập thất bại", Toast.LENGTH_SHORT).show()
                        }

                    } catch (e: HttpException) {
                        Toast.makeText(context, "Lỗi kết nối server", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }) {
                Text("Đăng nhập")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}
