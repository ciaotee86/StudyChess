package com.example.studychessapp.screens

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.studychessapp.R
import com.example.studychessapp.network.ApiServices
import com.example.studychessapp.network.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

@Composable
fun RegisterDialog(onDismiss: () -> Unit) {
    // Các biến trạng thái lưu dữ liệu nhập
    var hoTen by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var sdt by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fileUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Khởi tạo Retrofit API
    val api = RetrofitClient.instance.create(ApiServices::class.java)

    // Bộ khởi chạy để chọn ảnh từ thư viện
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        fileUri = it
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(), // Tự động co giãn chiều cao theo nội dung
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()), // Cho phép cuộn nếu màn hình nhỏ
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Đăng ký", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))

                // Logo ứng dụng
                Image(
                    painter = painterResource(id = R.drawable.logodhkt),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .height(80.dp)
                        .padding(bottom = 8.dp)
                )

                // --- CÁC Ô NHẬP LIỆU ---

                // 1. Họ và Tên
                TextField(
                    value = hoTen,
                    onValueChange = { hoTen = it },
                    label = { Text("Họ và Tên") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                // 2. Email
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                // 3. Số điện thoại
                TextField(
                    value = sdt,
                    onValueChange = { sdt = it },
                    label = { Text("SĐT") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                // 4. Mật khẩu
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mật khẩu") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                // 5. Xác nhận mật khẩu
                TextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Nhập lại mật khẩu") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    isError = password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword,
                    supportingText = {
                        if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
                            Text("Mật khẩu không khớp", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                Spacer(Modifier.height(8.dp))

                // Nút chọn file avatar
                Button(onClick = { launcher.launch("image/*") }) {
                    Text(if (fileUri != null) "Đã chọn ảnh" else "Chọn ảnh đại diện")
                }

                Spacer(Modifier.height(16.dp))

                // --- NÚT XÁC NHẬN ---
                Button(
                    onClick = {
                        // 1. Kiểm tra dữ liệu đầu vào (Validate)
                        if (hoTen.isBlank() || password.isBlank() || email.isBlank() || sdt.isBlank()) {
                            Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (password != confirmPassword) {
                            Toast.makeText(context, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // 2. Gửi API
                        scope.launch {
                            try {
                                // Tạo RequestBody cho các trường text
                                // Lưu ý: Dùng email làm username
                                val usernamePart = RequestBody.create("text/plain".toMediaTypeOrNull(), email.trim())
                                val emailPart = RequestBody.create("text/plain".toMediaTypeOrNull(), email.trim())
                                val sdtPart = RequestBody.create("text/plain".toMediaTypeOrNull(), sdt.trim())
                                val passwordPart = RequestBody.create("text/plain".toMediaTypeOrNull(), password)
                                val hoTenPart = RequestBody.create("text/plain".toMediaTypeOrNull(), hoTen.trim())

                                // Xử lý file ảnh (nếu có)
                                var filePart: MultipartBody.Part? = null
                                fileUri?.let { uri ->
                                    val file = File(context.cacheDir, "upload.tmp")
                                    context.contentResolver.openInputStream(uri)?.use { input ->
                                        file.outputStream().use { output -> input.copyTo(output) }
                                    }
                                    val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                                    filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
                                }

                                // 3. GỌI API VỚI NAMED ARGUMENTS (QUAN TRỌNG)
                                // Cách này đảm bảo dữ liệu không bị lộn xộn dù thứ tự hàm có thay đổi
                                val response = api.registerUser(
                                    username = usernamePart,
                                    email = emailPart,
                                    sdt = sdtPart,
                                    password = passwordPart,
                                    hoTen = hoTenPart,
                                    file = filePart
                                )

                                // Xử lý kết quả trả về
                                if (response.isSuccessful && response.body()?.status == "success") {
                                    Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                                    onDismiss() // Đóng dialog
                                } else {
                                    val errorMsg = response.body()?.message ?: "Đăng ký thất bại"
                                    Toast.makeText(context, "Lỗi: $errorMsg", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Lỗi kết nối: ${e.message}", Toast.LENGTH_LONG).show()
                                Log.e("RegisterError", e.message.toString())
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Xác nhận đăng ký")
                }

                // Nút Hủy
                TextButton(onClick = onDismiss) { Text("Hủy") }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}