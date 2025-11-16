    package com.example.studychessapp.network

    import com.google.gson.GsonBuilder
    import retrofit2.Retrofit
    import retrofit2.converter.gson.GsonConverterFactory

    object RetrofitClient {
        private const val BASE_URL = "http://10.0.2.2/chess_api/" // Emulator truy cập localhost

        val instance: Retrofit by lazy {
            val gson = GsonBuilder()

                .create()

            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
    }