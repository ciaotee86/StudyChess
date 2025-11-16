package com.example.studychessapp.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.studychessapp.model.ChessEngine
import com.github.bhlangonijr.chesslib.Square

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeBoardScreen(navController: NavHostController) {
    val engine = remember { ChessEngine() }
    var selectedSquare by remember { mutableStateOf<Square?>(null) }
    var forceRecompose by remember { mutableStateOf(false) }
    val context = LocalContext.current

    fun onSquareClick(row: Int, col: Int) {
        // ✅ SỬA CÁCH LẤY Ô CỜ
        val colName = ('a' + col).toString()
        val rowName = ('1' + (7 - row)).toString() // (7-row) vì hàng 0 là hàng 8
        val clickedSquare = Square.valueOf(colName.uppercase() + rowName)

        if (selectedSquare == null) {
            if (engine.board.getPiece(clickedSquare) != null) {
                selectedSquare = clickedSquare
            }
        } else {
            val moveSuccess = engine.makeMove(selectedSquare!!, clickedSquare)

            if (moveSuccess) {
                if (engine.isGameOver()) {
                    Toast.makeText(context, engine.getGameResult(), Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "Nước đi không hợp lệ!", Toast.LENGTH_SHORT).show()
            }
            selectedSquare = null
        }
        forceRecompose = !forceRecompose
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("♟️ Bàn cờ luyện tập") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trở về")
                    }
                }
            )
        }
    ) { paddingValues ->
        val boardState = forceRecompose

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(16.dp))

                for (i in 0..7) { // i là hàng (row)
                    Row {
                        for (j in 0..7) { // j là cột (col)
                            // ✅ SỬA CÁCH LẤY Ô CỜ
                            val colName = ('a' + j).toString()
                            val rowName = ('1' + (7 - i)).toString()
                            val square = Square.valueOf(colName.uppercase() + rowName)

                            val piece = engine.getPieceSymbol(square)
                            val isSelected = selectedSquare == square

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(if ((i + j) % 2 == 0) Color(0xFFEEEED2) else Color(0xFF769656))
                                    .clickable { onSquareClick(i, j) }
                                    .border(
                                        width = 2.dp,
                                        color = if (isSelected) Color.Red else Color.Transparent
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(piece, fontSize = 28.sp)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    engine.reset()
                    selectedSquare = null
                    forceRecompose = !forceRecompose
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🔄 Đặt lại bàn cờ")
            }
        }
    }
}