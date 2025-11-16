package com.example.studychessapp.model

import android.content.Context

object UserFileExporter {
    fun export(context: Context, username: String, email: String) {
        val fileName = "user_info.txt"
        val content = "Tên: $username\nEmail: $email\nThời gian: ${System.currentTimeMillis()}"
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
            it.write(content.toByteArray())
        }
    }
}