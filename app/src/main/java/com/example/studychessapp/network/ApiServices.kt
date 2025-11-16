package com.example.studychessapp.network

import com.example.studychessapp.network.ApiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiServices {

    @Multipart
    @POST("register.php")
    suspend fun registerUser(
        @Part("username") username: RequestBody,
        @Part("email") email: RequestBody,
        @Part("sdt") sdt: RequestBody,
        @Part("password") password: RequestBody,
        @Part file: MultipartBody.Part? = null
    ): Response<ApiResponse>

    // API đăng nhập
    @FormUrlEncoded
    @POST("login.php")
    suspend fun loginUser(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<ApiResponse>

    // ✅ HÀM MỚI: CẬP NHẬT AVATAR
    // (Giả sử API của bạn nhận 'userId' và 'file')
    @Multipart
    @POST("updateAvatar.php")
    suspend fun updateAvatar(
        @Part("userId") userId: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<ApiResponse> // API nên trả về ApiResponse với UserData đã cập nhật
}