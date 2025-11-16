package com.example.studychessapp.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.github.bhlangonijr.chesslib.Piece // ✅ THÊM IMPORT NÀY
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayWithAIScreen(navController: NavHostController) {
    val engine = remember { ChessEngine() }
    var selectedSquare by remember { mutableStateOf<Square?>(null) }
    var forceRecompose by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var isAIThinking by remember { mutableStateOf(false) }
    val moveHistory = remember { mutableStateListOf<String>() }

    fun makeAIMove() {
        if (engine.isGameOver() || engine.board.sideToMove != Side.BLACK) return

        isAIThinking = true
        CoroutineScope(Dispatchers.Default).launch {
            val legalMoves = engine.board.legalMoves()
            val aiMove = if (legalMoves.isNotEmpty()) legalMoves.random() else null
            kotlinx.coroutines.delay(500)

            withContext(Dispatchers.Main) {
                if (aiMove != null) {
                    engine.board.doMove(aiMove)
                    moveHistory.add("AI: ${aiMove.toString()}")
                }
                if (engine.isGameOver()) {
                    Toast.makeText(context, engine.getGameResult(), Toast.LENGTH_LONG).show()
                }
                isAIThinking = false
                forceRecompose = !forceRecompose
            }
        }
    }

    fun onSquareClick(row: Int, col: Int) {
        if (isAIThinking || engine.board.sideToMove != Side.WHITE) return

        // ✅ SỬA CÁCH LẤY Ô CỜ
        val colName = ('a' + col).toString()
        val rowName = ('1' + (7 - row)).toString()
        val clickedSquare = Square.valueOf(colName.uppercase() + rowName)

        if (selectedSquare == null) {
            if (engine.getPieceColor(clickedSquare) == Side.WHITE) {
                selectedSquare = clickedSquare
            }
        } else {
            val move = Move(selectedSquare!!, clickedSquare, Piece.NONE) // Import 'Piece' đã sửa lỗi này
            val legalMoves = engine.board.legalMoves()

            if (legalMoves.any { it.from == move.from && it.to == move.to }) {
                engine.board.doMove(move)
                moveHistory.add("Bạn: ${move.toString()}")
                selectedSquare = null
                forceRecompose = !forceRecompose

                if (engine.isGameOver()) {
                    Toast.makeText(context, engine.getGameResult(), Toast.LENGTH_LONG).show()
                } else {
                    makeAIMove()
                }
            } else {
                Toast.makeText(context, "Nước đi không hợp lệ!", Toast.LENGTH_SHORT).show()
                selectedSquare = null
            }
        }
        forceRecompose = !forceRecompose
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🤖 Chơi với AI") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trở về")
                    }
                }
            )
        }
    ) { paddingValues ->
        val boardState = forceRecompose

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Bàn cờ (Bên trái)
            Column(
                modifier = Modifier.weight(2f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isAIThinking) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(8.dp))
                    } else if (engine.isGameOver()) {
                        Text(engine.getGameResult(), style = MaterialTheme.typography.titleMedium, color = Color.Red)
                    } else {
                        Text(
                            if(engine.board.sideToMove == Side.WHITE) "Đến lượt bạn (Trắng)" else "AI đang nghĩ...",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    for (i in 0..7) { // i là hàng
                        Row {
                            for (j in 0..7) { // j là cột
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
                                            color = if (isSelected) Color.Blue else Color.Transparent
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
                        moveHistory.clear()
                        isAIThinking = false
                        forceRecompose = !forceRecompose
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🔄 Đặt lại bàn cờ")
                }
            }

            // Lịch sử nước đi (Bên phải)
//            Column(modifier = Modifier
//                .weight(1f)
//                .padding(start = 16.dp)) {
//                Text("Lịch sử", style = MaterialTheme.typography.titleLarge)
//                Spacer(Modifier.height(8.dp))
//                LazyColumn(modifier = Modifier.fillMaxHeight()) {
//                    items(moveHistory) { move ->
//                        Text(move, modifier = Modifier.padding(bottom = 4.dp))
//                    }
//                }
//            }
        }
    }
}