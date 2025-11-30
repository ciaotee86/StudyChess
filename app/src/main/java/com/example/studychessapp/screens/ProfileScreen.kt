package com.example.studychessapp.screens

import android.content.Context // ✅ THÊM DÒNG NÀY (cho lỗi 'Context')
import com.example.studychessapp.R
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.studychessapp.network.ApiServices
import com.example.studychessapp.network.AuthViewModel
import com.example.studychessapp.network.RetrofitClient
import com.example.studychessapp.network.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val userSession by authViewModel.userSession.collectAsState()
    val isLoggedIn = userSession.isLoggedIn
    val userData = userSession.userData

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val imageLoader = ImageLoader(context)
    val api = RetrofitClient.instance.create(ApiServices::class.java) // ✅ Khởi tạo API

    // ✅ State để giữ file Uri đã chọn
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            navController.navigate("home") {
                popUpTo("home") { inclusive = true }
            }
        }
    }

    // ✅ Launcher để chọn ảnh
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFileUri = uri
    }

    // ✅ Launcher để xuất PDF (từ mã trước)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                scope.launch {
                    try {
                        val outputStream = context.contentResolver.openOutputStream(uri) as FileOutputStream
                        generateAndSavePdf(context, imageLoader, userData!!, outputStream)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Xuất PDF thành công!", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Lỗi khi ghi PDF: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    // ✅ Hàm xử lý upload
    fun handleUpload() {
        if (selectedFileUri == null || userData?.id == null) {
            Toast.makeText(context, "Chưa chọn tệp hoặc lỗi user ID", Toast.LENGTH_SHORT).show()
            return
        }

        isUploading = true
        scope.launch {
            try {
                // Chuyển Uri thành File
                val file = File(context.cacheDir, "upload.tmp")
                context.contentResolver.openInputStream(selectedFileUri!!)?.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

                // Tạo RequestBody cho userId
                val userIdPart = userData.id.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                // Gọi API
                val response = api.updateAvatar(userIdPart, filePart)

                if (response.isSuccessful && response.body()?.status == "success" && response.body()?.user != null) {
                    // Cập nhật AuthViewModel với dữ liệu mới
                    authViewModel.setLoggedIn(response.body()!!.user!!)
                    Toast.makeText(context, "Cập nhật avatar thành công!", Toast.LENGTH_SHORT).show()
                    selectedFileUri = null // Reset Uri
                } else {
                    Toast.makeText(context, "Lỗi: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi upload: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("ProfileUpload", "Lỗi: ${e.message}")
            } finally {
                isUploading = false
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
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "Chess_User_${userData.id}_Profile.pdf")
        }
        exportLauncher.launch(intent)
    }

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
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(32.dp))

            // Hiển thị Avatar (Coil)
            AsyncImage(
                // ✅ Ưu tiên hiển thị ảnh mới chọn (nếu có)
                model = selectedFileUri ?: userData?.avatarUrl,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                // Hiển thị icon mặc định nếu không có avatarUrl và không có ảnh mới chọn
                placeholder = painterResource(id = R.drawable.ic_user_account),
                error = painterResource(id = R.drawable.ic_user_account)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = userData?.tenDangNhap ?: "Người dùng",
                style = MaterialTheme.typography.headlineMedium
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            ProfileDetail(label = "ID", value = userData?.id?.toString() ?: "N/A")
            ProfileDetail(label = "Họ tên", value = userData?.hoTen ?: "N/A")
            ProfileDetail(label = "Email", value = userData?.email ?: "N/A")
            ProfileDetail(label = "SĐT", value = userData?.soDienThoai ?: "N/A")
            ProfileDetail(label = "Ngày đăng ký", value = userData?.ngayTaoGoc ?: "N/A")
            ProfileDetail(
                label = "Đã tham gia",
                value = userData?.thoiGianThamGia ?: "N/A" // Lấy thẳng từ UserData
            )
            Spacer(modifier = Modifier.height(32.dp))

            // --- ✅ NÚT UPLOAD MỚI ---
            if (selectedFileUri == null) {
                // Nút 1: Chọn ảnh
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Đổi Ảnh Đại Diện")
                }
            } else {
                // Nút 2: Xác nhận upload
                Button(
                    onClick = { handleUpload() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isUploading
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Lưu Ảnh Mới")
                    }
                }
                // Nút 3: Hủy
                TextButton(onClick = { selectedFileUri = null }) {
                    Text("Hủy")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- CÁC NÚT CŨ ---
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
                Text("📁 Xuất Dữ Liệu Tài Khoản (.pdf)")
            }
        }
    }
}

// (Hàm generateAndSavePdf và ProfileDetail giữ nguyên như câu trả lời trước)
// ...
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

// Trong ProfileScreen.kt

private suspend fun generateAndSavePdf(
    context: Context,
    imageLoader: ImageLoader,
    data: UserData,
    outputStream: FileOutputStream
) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    // --- SETUP PAINTS ---
    val titlePaint = Paint().apply {
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textSize = 24f
        color = android.graphics.Color.BLUE
        textAlign = Paint.Align.CENTER
    }

    val labelPaint = Paint().apply {
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textSize = 14f
        color = android.graphics.Color.DKGRAY
    }

    val valuePaint = Paint().apply {
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textSize = 14f
        color = android.graphics.Color.BLACK
    }

    val borderPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = android.graphics.Color.BLACK
    }

    // --- VẼ KHUNG VIỀN ---
    canvas.drawRect(20f, 20f, 575f, 822f, borderPaint)

    // --- VẼ TIÊU ĐỀ ---
    canvas.drawText("HỒ SƠ CÁ NHÂN STUDY CHESS", 595f / 2, 80f, titlePaint)

    var yPos = 140f
    val startX = 60f

    // --- VẼ AVATAR (Căn giữa) ---
    val avatarBitmap: Bitmap? = try {
        if (data.avatarUrl != null) {
            val request = ImageRequest.Builder(context)
                .data(data.avatarUrl)
                .allowHardware(false)
                .build()
            val result = (imageLoader.execute(request) as SuccessResult).drawable
            (result as BitmapDrawable).bitmap
        } else null
    } catch (e: Exception) { null }

    if (avatarBitmap != null) {
        val scaledBitmap = Bitmap.createScaledBitmap(avatarBitmap, 120, 120, true)
        // Vẽ ảnh ở giữa trang
        canvas.drawBitmap(scaledBitmap, (595f - 120f) / 2, yPos, null)
        yPos += 150f
    } else {
        yPos += 20f
    }

    // --- VẼ THÔNG TIN CHI TIẾT ---
    fun drawLineInfo(label: String, value: String?) {
        canvas.drawText(label, startX, yPos, labelPaint)
        // Vẽ giá trị cách lề trái 200 đơn vị
        canvas.drawText(value ?: "Chưa cập nhật", startX + 150f, yPos, valuePaint)
        // Vẽ đường gạch dưới mờ
        val linePaint = Paint().apply { color = android.graphics.Color.LTGRAY; strokeWidth = 1f }
        canvas.drawLine(startX, yPos + 10f, 535f, yPos + 10f, linePaint)
        yPos += 40f
    }

    drawLineInfo("ID Tài khoản:", "#${data.id}")
    drawLineInfo("Họ và Tên:", data.hoTen)
    drawLineInfo("Tên đăng nhập:", data.tenDangNhap)
    drawLineInfo("Email:", data.email)
    drawLineInfo("Số điện thoại:", data.soDienThoai)

    // Xử lý ngày tham gia (Cần đảm bảo API trả về trường này hoặc UserData có trường này)
    // Giả sử bạn đã thêm `ngay_tao` vào UserData trong bước 2
    drawLineInfo("Ngày đăng ký:", data.thoiGianThamGia) // Hoặc data.ngayTaoGoc nếu bạn map thêm
    drawLineInfo("Đã tham gia:", data.thoiGianThamGia)

    // Footer
    val footerPaint = Paint().apply { textSize = 12f; color = android.graphics.Color.GRAY; textAlign = Paint.Align.CENTER }
    canvas.drawText("Được xuất từ ứng dụng StudyChessApp", 595f / 2, 800f, footerPaint)

    pdfDocument.finishPage(page)
    pdfDocument.writeTo(outputStream)
    pdfDocument.close()
    outputStream.close()
}