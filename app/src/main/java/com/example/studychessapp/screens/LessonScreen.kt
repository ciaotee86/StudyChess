package com.example.studychessapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun LessonScreen(navController: NavHostController) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text("📖 Bài học: Cách di chuyển quân mã",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)

            Spacer(Modifier.height(12.dp))
            Text("Quân mã đi hình chữ L: 2 ô theo một hướng và 1 ô vuông góc.", fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))
            Text("Ví dụ: từ ô E4, quân mã có thể đi đến các ô sau:", fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))

            val knightMoves = listOf("C5", "C3", "D2", "F2", "G3", "G5", "F6", "D6")
            knightMoves.forEach { move -> Text("• $move", fontSize = 16.sp) }

            Spacer(Modifier.height(24.dp))
            Text("Hãy tưởng tượng quân mã đang đứng ở ô E4 và nhảy đến các vị trí này!",
                style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
    }
}
