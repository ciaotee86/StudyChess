package com.example.studychessapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studychessapp.model.*

@Composable
fun ChessBoard() {
    val board = remember { generateBoard() }

    Column {
        for (row in board) {
            Row {
                for (square in row) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(if ((square.row + square.col) % 2 == 0) Color.LightGray else Color.DarkGray)
                            .clickable { /* handle click */ },
                        contentAlignment = Alignment.Center
                    ) {
                        square.piece?.let {
                            Text(text = getPieceSymbol(it), fontSize = 24.sp)
                        }
                    }
                }
            }
        }
    }
}

fun getPieceSymbol(piece: Piece): String {
    return when (piece.type) {
        PieceType.KING -> if (piece.color == PieceColor.WHITE) "♔" else "♚"
        PieceType.QUEEN -> if (piece.color == PieceColor.WHITE) "♕" else "♛"
        PieceType.ROOK -> if (piece.color == PieceColor.WHITE) "♖" else "♜"
        PieceType.BISHOP -> if (piece.color == PieceColor.WHITE) "♗" else "♝"
        PieceType.KNIGHT -> if (piece.color == PieceColor.WHITE) "♘" else "♞"
        PieceType.PAWN -> if (piece.color == PieceColor.WHITE) "♙" else "♟"
    }
}

data class Square(val row: Int, val col: Int, var piece: Piece? = null)

fun generateBoard(): List<List<Square>> {
    return List(8) { row ->
        List(8) { col ->
            Square(row, col)
        }
    }
}