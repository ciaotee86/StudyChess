package com.example.studychessapp.screens

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var sdt by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fileUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = RetrofitClient.instance.create(ApiServices::class.java)

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        fileUri = it
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.size(500.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Đăng ký", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))

                TextField(value = username, onValueChange = { username = it }, label = { Text("Tên") })
                TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                TextField(
                        value = sdt,
                        onValueChange = { sdt = it },
                        label = { Text("SĐT")},
//                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
//                        visualTransformation = PasswordVisualTransformation()
                )
                TextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Mật khẩu") }, visualTransformation = PasswordVisualTransformation()
                )

                Spacer(Modifier.height(8.dp))

                Button(onClick = { launcher.launch("*/*") }) {
                    Text(if (fileUri != null) "Đã chọn file" else "Chọn file upload")
                }

                Spacer(Modifier.height(16.dp))

                Button(onClick = {
                    scope.launch {
                        try {
                            val usernamePart = RequestBody.create("text/plain".toMediaTypeOrNull(), username)
                            val emailPart = RequestBody.create("text/plain".toMediaTypeOrNull(), email)
                            val sdtPart = RequestBody.create("text/plain".toMediaTypeOrNull(), sdt)
                            val passwordPart = RequestBody.create("text/plain".toMediaTypeOrNull(), password)

                            var filePart: MultipartBody.Part? = null
                            fileUri?.let { uri ->
                                val file = File(context.cacheDir, "upload.tmp")
                                context.contentResolver.openInputStream(uri)?.use { input ->
                                    file.outputStream().use { output -> input.copyTo(output) }
                                }
                                val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                                filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
                            }

                            val response = api.registerUser(usernamePart, emailPart, sdtPart, passwordPart, filePart)
                            Log.d("API_BODY", response.body()?.toString() ?: "null")
                            Log.d("API_ERROR", response.errorBody()?.string() ?: "no error")

                            if (response.isSuccessful && response.body()?.status == "success") {
                                Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            } else {
                                Toast.makeText(context, "Lỗi: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }) {
                    Text("Xác nhận đăng ký")
                }

                TextButton(onClick = onDismiss) { Text("Hủy") }
            }
        }
    }
}
