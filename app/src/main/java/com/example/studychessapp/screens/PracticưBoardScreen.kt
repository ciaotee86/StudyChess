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

@Composable
fun PracticeBoardScreen(navController: NavHostController) {
    var selected by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var board by remember {
        mutableStateOf(
            Array(8) { row ->
                Array(8) { col ->
                    if (row == 1) "♟" else if (row == 6) "♙" else ""
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("♟️ Bàn cờ luyện tập", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))

        for (i in 0..7) {
            Row {
                for (j in 0..7) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(if ((i + j) % 2 == 0) Color(0xFFEEEED2) else Color(0xFF769656))
                            .clickable {
                                if (selected == null && board[i][j].isNotEmpty()) {
                                    selected = Pair(i, j)
                                } else if (selected != null) {
                                    val (fromRow, fromCol) = selected!!
                                    board[i][j] = board[fromRow][fromCol]
                                    board[fromRow][fromCol] = ""
                                    selected = null
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(board[i][j], fontSize = 20.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            board = Array(8) { row ->
                Array(8) { col ->
                    if (row == 1) "♟" else if (row == 6) "♙" else ""
                }
            }
            selected = null
        }) {
            Text("Reset bàn cờ")
        }
    }
}