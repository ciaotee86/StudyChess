package com.example.studychessapp.network

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
        @Part("ho_ten") hoTen: RequestBody,
        @Part file: MultipartBody.Part? = null
    ): Response<ApiResponse>

    @FormUrlEncoded
    @POST("login.php")
    suspend fun loginUser(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<ApiResponse>

    @Multipart
    @POST("updateAvatar.php")
    suspend fun updateAvatar(
        @Part("userId") userId: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<ApiResponse>

    // ✅ THÊM HÀM NÀY: Upload file tài liệu
    @Multipart
    @POST("upload_file.php")
    suspend fun uploadFile(
        @Part("nguoi_dung_id") userId: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<ApiResponse>
}