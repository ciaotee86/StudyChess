package com.example.studychessapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.studychessapp.model.ChessGame

@Composable
fun PlayWithAIScreen(navController: NavHostController) {
    val game = remember { mutableStateOf(ChessGame()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🤖 Chơi với AI", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (game.value.gameOver) {
            Text("🎉 ${game.value.winner} thắng!", fontSize = 20.sp, color = Color.Red)
            Spacer(modifier = Modifier.height(16.dp))
        }

        for (i in 0..7) {
            Row {
                for (j in 0..7) {
                    val tile = game.value.board[i][j]
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if ((i + j) % 2 == 0) Color(0xFFEEEED2) else Color(0xFF769656)
                            )
                            .clickable {
                                val moved = game.value.selectTile(i, j)
                                if (moved) {
                                    game.value.aiMove()
                                }
                            }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tile.piece,
                            fontSize = 24.sp,
                            color = if (tile.isSelected) Color.Red else Color.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { game.value.resetBoard() }) {
            Text("🔄 Reset bàn cờ")
        }
    }
}