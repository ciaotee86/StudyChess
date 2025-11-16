package com.example.studychessapp.network

import androidx.lifecycle.ViewModel
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// --- 1.  DATA CLASS USERDATA  ---

data class ApiResponse(
    val status: String,
    val message: String,
    val user: UserData? = null
)

data class UserData(
    @SerializedName("id")
    val id: Int,
    @SerializedName("ten_dang_nhap")
    val tenDangNhap: String?,

    val email: String?,
    @SerializedName("so_dien_thoai")
    val soDienThoai: String?,
    @SerializedName("ho_ten")
    val hoTen: String?,
    @SerializedName("duong_dan_anh")
    val avatarUrl: String? = null
)

// --- 2. DATA CLASS USERSESSION (Thêm userData: UserData?) ---
data class UserSession(
    val isLoggedIn: Boolean = false,
    val userName: String? = null,
    val userId: Int? = null,
    val avatarUrl: String? = null,
    val userData: UserData? = null
)

class AuthViewModel : ViewModel() {


    private val _userSession = MutableStateFlow(UserSession())
    val userSession: StateFlow<UserSession> = _userSession.asStateFlow()


    // ⚠️ CHỈ GIỮ LẠI MỘT HÀM setLoggedIn, nhận toàn bộ UserData
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

    // HÀM ĐƯỢC GỌI KHI NGƯỜI DÙNG BẤM ĐĂNG XUẤT (Giữ nguyên)
    fun logout() {
        _userSession.update {
            UserSession()
        }
    }

    // TODO: Bổ sung code đọc trạng thái từ DataStore khi ViewModel khởi tạo
}