package com.example.studychessapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.studychessapp.model.LessonRepository // ✅ Thêm import

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonScreen(navController: NavHostController, lessonId: String?) {
    // Tìm bài học dựa trên ID
    val lesson = LessonRepository.lessons.find { it.id == lessonId }
        ?: LessonRepository.lessons.first() // Lấy bài đầu tiên nếu không tìm thấy

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(lesson.title) }, // ✅ Dùng tiêu đề động
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // ✅ Hiển thị nội dung bài học động
                    Text(
                        lesson.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(lesson.content, fontSize = 18.sp)
                }
            }
        }
    }
}