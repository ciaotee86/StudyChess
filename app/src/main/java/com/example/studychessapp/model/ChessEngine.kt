package com.example.studychessapp.model

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import java.lang.Exception

class ChessEngine {
    var board = Board()
        private set // Chỉ cho phép thay đổi bên trong class

    // Lấy biểu tượng Unicode cho quân cờ
    fun getPieceSymbol(square: Square): String {
        val piece = board.getPiece(square)
        return when (piece) {
            Piece.WHITE_KING -> "♔"
            Piece.WHITE_QUEEN -> "♕"
            Piece.WHITE_ROOK -> "♖"
            Piece.WHITE_BISHOP -> "♗"
            Piece.WHITE_KNIGHT -> "♘"
            Piece.WHITE_PAWN -> "♙"
            Piece.BLACK_KING -> "♚"
            Piece.BLACK_QUEEN -> "♛"
            Piece.BLACK_ROOK -> "♜"
            Piece.BLACK_BISHOP -> "♝"
            Piece.BLACK_KNIGHT -> "♞"
            Piece.BLACK_PAWN -> "♟"
            else -> ""
        }
    }

    // Lấy màu của quân cờ tại ô
    fun getPieceColor(square: Square): Side? {
        return board.getPiece(square).getPieceSide()
    }

    // Thực hiện nước đi
    fun makeMove(from: Square, to: Square): Boolean {
        // Tạo nước đi (cần xử lý cả trường hợp phong cấp)
        val move = Move(from, to, Piece.NONE)

        // Kiểm tra xem nước đi có hợp lệ không
        val legalMoves = board.legalMoves()
        return if (legalMoves.any { it.from == from && it.to == to }) {
            board.doMove(move)
            true
        } else {
            false
        }
    }

    // Đặt lại bàn cờ
    fun reset() {
        board = Board()
    }

    // Kiểm tra ván cờ kết thúc
    fun isGameOver(): Boolean {
        return board.isMated || board.isStaleMate || board.isInsufficientMaterial
    }

    // Lấy thông báo kết thúc ván cờ
    fun getGameResult(): String {
        return when {
            board.isMated -> if (board.sideToMove == Side.WHITE) "Cờ đen thắng!" else "Cờ trắng thắng!"
            board.isStaleMate -> "Hòa cờ!"
            board.isInsufficientMaterial -> "Hòa do không đủ quân!"
            else -> ""
        }
    }
}