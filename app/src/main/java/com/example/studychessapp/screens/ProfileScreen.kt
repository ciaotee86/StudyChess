package com.example.studychessapp.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.studychessapp.R
import com.example.studychessapp.network.UserData
import com.example.studychessapp.network.ApiServices
import com.example.studychessapp.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// --- CÁC HÀM HELPER ---
fun formatDateTime(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return "Không xác định"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formatter = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
        val date = parser.parse(dateString)
        formatter.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

fun calculateDuration(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return "Vừa tham gia"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val startDate = parser.parse(dateString) ?: return "Lỗi ngày"
        val endDate = Date()
        var diff = endDate.time - startDate.time
        if (diff < 0) diff = 0
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60
        when {
            days > 365 -> "${days / 365} năm ${(days % 365) / 30} tháng"
            days > 30 -> "${days / 30} tháng ${days % 30} ngày"
            days > 0 -> "$days ngày $hours giờ"
            hours > 0 -> "$hours giờ $minutes phút"
            else -> "$minutes phút $seconds giây"
        }
    } catch (e: Exception) {
        "Không xác định"
    }
}

@Composable
fun ProfileScreen(
    navController: NavController,
    userData: UserData?,
    onLogout: () -> Unit, // Callback xử lý đăng xuất
    onUserUpdated: (UserData) -> Unit // Callback cập nhật lại ViewModel sau khi upload avatar
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentUser by remember { mutableStateOf(userData) }

    // Launcher chọn ẢNH (Avatar)
    val avatarLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            if (currentUser != null) {
                scope.launch {
                    uploadAvatar(context, it, currentUser!!.id) { newUser ->
                        currentUser = newUser // Cập nhật UI cục bộ
                        onUserUpdated(newUser) // Cập nhật vào ViewModel
                    }
                }
            }
        }
    }

    // Launcher chọn FILE (Tài liệu)
    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            if (currentUser != null) {
                scope.launch {
                    uploadDocument(context, it, currentUser!!.id)
                }
            }
        }
    }

    val formattedRegDate = remember(currentUser?.ngayTao) { formatDateTime(currentUser?.ngayTao) }
    val durationString = remember(currentUser?.ngayTao) { calculateDuration(currentUser?.ngayTao) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hồ sơ cá nhân",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // --- AVATAR ---
        Box(contentAlignment = Alignment.BottomEnd) {
            if (currentUser?.avatarUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(currentUser!!.avatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .clickable { avatarLauncher.launch("image/*") }
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_user_account),
                    contentDescription = "Avatar Default",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .clickable { avatarLauncher.launch("image/*") }
                )
            }
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_camera),
                contentDescription = "Edit",
                tint = Color.White,
                modifier = Modifier
                    .background(Color.Blue, CircleShape)
                    .padding(4.dp)
                    .size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = currentUser?.hoTen ?: "Chưa cập nhật tên",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- INFO CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileItem(label = "Email", value = currentUser?.email)
                Divider()
                ProfileItem(label = "Số điện thoại", value = currentUser?.soDienThoai)
                Divider()
                ProfileItem(label = "Ngày đăng ký", value = formattedRegDate)
                Divider()
                ProfileItem(label = "Đã tham gia", value = durationString)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- CÁC NÚT CHỨC NĂNG ---

        // 1. Xuất PDF
        Button(
            onClick = {
                scope.launch(Dispatchers.IO) {
                    if (currentUser != null) {
                        exportPdf(context, currentUser!!)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Xuất thông tin PDF")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Tải lên tài liệu (Mới)
        Button(
            onClick = {
                // Mở chọn tất cả các loại file
                fileLauncher.launch("*/*")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), // Màu xanh lá
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("📂 Tải lên tài liệu")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Đăng xuất (Mới)
        Button(
            onClick = { onLogout() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Đăng xuất")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Nút Quay lại
        TextButton(onClick = { navController.popBackStack() }) {
            Text("Quay lại", color = Color.Gray)
        }
    }
}

@Composable
fun ProfileItem(label: String, value: String?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray)
        Text(text = value ?: "---", fontWeight = FontWeight.SemiBold)
    }
}

// --- LOGIC UPLOAD AVATAR (Sửa lỗi) ---
suspend fun uploadAvatar(context: Context, uri: Uri, userId: Int, onSuccess: (UserData) -> Unit) {
    withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "temp_avatar_${System.currentTimeMillis()}.jpg")

            inputStream?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }

            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            // Quan trọng: userId phải gửi dưới dạng text/plain
            val userIdPart = RequestBody.create("text/plain".toMediaTypeOrNull(), userId.toString())

            val api = RetrofitClient.instance.create(ApiServices::class.java)
            val response = api.updateAvatar(userIdPart, body)

            withContext(Dispatchers.Main) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(context, "Cập nhật ảnh thành công!", Toast.LENGTH_SHORT).show()
                    val updatedUser = response.body()?.user
                    if (updatedUser != null) {
                        onSuccess(updatedUser)
                    }
                } else {
                    val msg = response.body()?.message ?: "Lỗi không xác định: ${response.code()}"
                    Toast.makeText(context, "Lỗi server: $msg", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Lỗi kết nối: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }
}

// --- LOGIC UPLOAD FILE TÀI LIỆU (Mới) ---
suspend fun uploadDocument(context: Context, uri: Uri, userId: Int) {
    withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)

            // Lấy tên file gốc nếu có thể, hoặc đặt tên mặc định
            val fileName = "doc_${System.currentTimeMillis()}_upload"
            val file = File(context.cacheDir, fileName)

            inputStream?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }

            // Dùng multipart/form-data chung cho file
            val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val userIdPart = RequestBody.create("text/plain".toMediaTypeOrNull(), userId.toString())

            val api = RetrofitClient.instance.create(ApiServices::class.java)
            val response = api.uploadFile(userIdPart, body)

            withContext(Dispatchers.Main) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    val path = response.body()?.duongDan ?: ""
                    Toast.makeText(context, "Tải file thành công!\nLưu tại: $path", Toast.LENGTH_LONG).show()
                } else {
                    val msg = response.body()?.message ?: "Lỗi tải file: ${response.code()}"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Lỗi ngoại lệ: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// --- LOGIC XUẤT PDF (Giữ nguyên, chỉ cần copy lại nếu muốn chắc chắn) ---
suspend fun exportPdf(context: Context, user: UserData) {
    // ... (Giữ nguyên code exportPdf từ câu trả lời trước) ...
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 24f
        paint.color = android.graphics.Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("HỒ SƠ THÀNH VIÊN", 595f / 2, 80f, paint)

        var yPos = 120f
        val imageLoader = ImageLoader(context)
        if (!user.avatarUrl.isNullOrEmpty()) {
            val request = ImageRequest.Builder(context)
                .data(user.avatarUrl)
                .allowHardware(false)
                .build()
            val result = (imageLoader.execute(request) as? SuccessResult)?.drawable
            val bitmap = (result as? BitmapDrawable)?.bitmap

            if (bitmap != null) {
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true)
                canvas.drawBitmap(scaledBitmap, (595f - 100f) / 2, yPos, null)
                yPos += 120f
            }
        } else {
            yPos += 20f
        }

        paint.textAlign = Paint.Align.LEFT
        val startX = 50f
        val lineSpacing = 40f

        fun drawLine(label: String, content: String) {
            paint.textSize = 14f
            paint.typeface = Typeface.DEFAULT_BOLD
            paint.color = android.graphics.Color.DKGRAY
            canvas.drawText(label, startX, yPos, paint)

            paint.typeface = Typeface.DEFAULT
            paint.color = android.graphics.Color.BLACK
            canvas.drawText(content, startX + 150f, yPos, paint)

            paint.color = android.graphics.Color.LTGRAY
            paint.strokeWidth = 1f
            canvas.drawLine(startX, yPos + 10f, 595f - 50f, yPos + 10f, paint)

            yPos += lineSpacing
        }

        drawLine("Họ và Tên:", user.hoTen ?: "---")
        drawLine("Email:", user.email ?: "---")
        drawLine("Số điện thoại:", user.soDienThoai ?: "---")
        drawLine("Ngày đăng ký:", formatDateTime(user.ngayTao))
        drawLine("Đã tham gia:", calculateDuration(user.ngayTao))

        pdfDocument.finishPage(page)

        val fileName = "Profile_${System.currentTimeMillis()}.pdf"
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Đã xuất PDF: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Log.e("PDFError", e.message.toString())
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Lỗi xuất PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}