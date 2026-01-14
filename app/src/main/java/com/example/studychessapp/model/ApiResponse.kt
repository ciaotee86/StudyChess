package com.example.studychessapp.network

import androidx.lifecycle.ViewModel
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// --- 1. DATA CLASS APIRESPONSE ---
data class ApiResponse(
    val status: String,
    val message: String,
    val user: UserData? = null,
    // ✅ THÊM TRƯỜNG NÀY để hứng kết quả từ upload_file.php
    @SerializedName("duong_dan") val duongDan: String? = null
)

// --- 2. DATA CLASS USERDATA ---
data class UserData(
    @SerializedName("id") val id: Int,
    @SerializedName("ten_dang_nhap") val tenDangNhap: String?,
    val email: String?,
    @SerializedName("so_dien_thoai") val soDienThoai: String?,
    @SerializedName("ho_ten") val hoTen: String?,
    @SerializedName("duong_dan_anh") val avatarUrl: String? = null,
    @SerializedName("thoi_gian_tham_gia") val thoiGianThamGia: String? = null,
    @SerializedName("ngay_tao_goc") val ngayTaoGoc: String? = null,
    @SerializedName("ngay_tao") val ngayTao: String? = null
)

// --- 3. DATA CLASS USERSESSION ---
data class UserSession(
    val isLoggedIn: Boolean = false,
    val userName: String? = null,
    val userId: Int? = null,
    val avatarUrl: String? = null,
    val userData: UserData? = null
)

// --- 4. AUTH VIEW MODEL ---
class AuthViewModel : ViewModel() {
    private val _userSession = MutableStateFlow(UserSession())
    val userSession: StateFlow<UserSession> = _userSession.asStateFlow()

    fun setLoggedIn(user: UserData) {
        _userSession.update {
            it.copy(
                isLoggedIn = true,
                userName = user.tenDangNhap,
                userId = user.id,
                avatarUrl = user.avatarUrl,
                userData = user
            )
        }
    }

    // Hàm cập nhật lại thông tin user (ví dụ sau khi đổi avatar)
    fun updateUser(newUser: UserData) {
        _userSession.update {
            it.copy(
                avatarUrl = newUser.avatarUrl,
                userData = newUser
            )
        }
    }

    fun logout() {
        _userSession.update {
            UserSession() // Reset về trạng thái chưa đăng nhập
        }
    }
}