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
import com.example.studychessapp.model.ApiResponse
import com.example.studychessapp.model.UserData
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

// --- 1. CÁC HÀM XỬ LÝ THỜI GIAN (Helper Functions) ---

// Chuyển đổi String MySQL (yyyy-MM-dd HH:mm:ss) sang HH:mm:ss dd/MM/yyyy
fun formatDateTime(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return "Không xác định"
    return try {
        // Định dạng của MySQL trả về
        val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        // Định dạng mong muốn hiển thị: Giờ:Phút:Giây Ngày/Tháng/Năm
        val formatter = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
        val date = parser.parse(dateString)
        formatter.format(date ?: Date())
    } catch (e: Exception) {
        dateString // Trả về nguyên gốc nếu lỗi parse
    }
}

// Tính khoảng thời gian từ lúc đăng ký đến hiện tại
fun calculateDuration(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return "Vừa tham gia"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val startDate = parser.parse(dateString) ?: return "Lỗi ngày"
        val endDate = Date() // Thời gian hiện tại

        var diff = endDate.time - startDate.time
        if (diff < 0) diff = 0

        val days = TimeUnit.MILLISECONDS.toDays(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60

        // Logic hiển thị đẹp
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
fun ProfileScreen(navController: NavController, userData: UserData?) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Sử dụng currentUserData để UI tự cập nhật khi thay đổi ảnh
    var currentUser by remember { mutableStateOf(userData) }

    // Launcher chọn ảnh
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            scope.launch {
                uploadAvatar(context, it, currentUser?.id ?: 0) { newUrl ->
                    // Callback khi upload thành công -> Cập nhật UI
                    currentUser = currentUser?.copy(avatarUrl = newUrl)
                }
            }
        }
    }

    // Các biến format thời gian để hiển thị
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
                        .clickable { launcher.launch("image/*") } // Bấm vào ảnh để đổi
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_user_account), // Đảm bảo có ảnh placeholder này
                    contentDescription = "Avatar Default",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .clickable { launcher.launch("image/*") }
                )
            }
            // Icon máy ảnh nhỏ (tùy chọn)
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

        // --- THÔNG TIN CHI TIẾT ---
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
                // Sử dụng hàm formatDateTime cho giao diện
                ProfileItem(label = "Ngày đăng ký", value = formattedRegDate)
                Divider()
                // Sử dụng hàm calculateDuration cho giao diện
                ProfileItem(label = "Đã tham gia", value = durationString)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Nút Xuất PDF
        Button(
            onClick = {
                scope.launch(Dispatchers.IO) {
                    if (currentUser != null) {
                        exportPdf(context, currentUser!!)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Xuất thông tin PDF")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.popBackStack() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Quay lại")
        }
    }
}

@Composable
fun ProfileItem(label: String, value: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray)
        Text(text = value ?: "---", fontWeight = FontWeight.SemiBold)
    }
}

// --- LOGIC UPLOAD ẢNH ---
suspend fun uploadAvatar(context: Context, uri: Uri, userId: Int, onSuccess: (String) -> Unit) {
    try {
        val file = File(context.cacheDir, "temp_avatar.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }

        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val userIdPart = RequestBody.create("text/plain".toMediaTypeOrNull(), userId.toString())

        val api = RetrofitClient.instance.create(ApiServices::class.java)
        // Gọi đến API updateAvatar
        val response = api.updateAvatar(userIdPart, body)

        if (response.isSuccessful && response.body()?.status == "success") {
            val newUrl = response.body()?.avatarUrl
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Cập nhật ảnh thành công!", Toast.LENGTH_SHORT).show()
                if (newUrl != null) onSuccess(newUrl)
            }
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Lỗi server: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Lỗi upload: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

// --- LOGIC XUẤT PDF ---
suspend fun exportPdf(context: Context, user: UserData) {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // 1. Tiêu đề
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 24f
        paint.color = android.graphics.Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("HỒ SƠ THÀNH VIÊN", 595f / 2, 80f, paint)

        // 2. Vẽ Avatar (nếu có)
        var yPos = 120f
        val imageLoader = ImageLoader(context)
        if (!user.avatarUrl.isNullOrEmpty()) {
            val request = ImageRequest.Builder(context)
                .data(user.avatarUrl)
                .allowHardware(false) // Bắt buộc cho Canvas
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

        // 3. Vẽ thông tin
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
            // Canh lề nội dung cách nhãn 150 đơn vị
            canvas.drawText(content, startX + 150f, yPos, paint)

            // Kẻ đường gạch dưới mờ
            paint.color = android.graphics.Color.LTGRAY
            paint.strokeWidth = 1f
            canvas.drawLine(startX, yPos + 10f, 595f - 50f, yPos + 10f, paint)

            yPos += lineSpacing
        }

        drawLine("Họ và Tên:", user.hoTen ?: "---")
        drawLine("Email:", user.email ?: "---")
        drawLine("Số điện thoại:", user.soDienThoai ?: "---")

        // --- XỬ LÝ YÊU CẦU: FORMAT NGÀY GIỜ CHÍNH XÁC ---
        drawLine("Ngày đăng ký:", formatDateTime(user.ngayTao))

        // --- XỬ LÝ YÊU CẦU: THỜI GIAN THAM GIA ---
        drawLine("Đã tham gia:", calculateDuration(user.ngayTao))

        // --- ĐÃ BỎ DÒNG TÊN ĐĂNG NHẬP THEO YÊU CẦU ---

        pdfDocument.finishPage(page)

        // Lưu file
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