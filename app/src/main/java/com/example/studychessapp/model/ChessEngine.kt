package com.example.studychessapp.model

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move





class ChessEngine {
    val board = Board()

    fun getLegalMoves(): List<Move> = board.legalMoves()

    fun makeMove(from: Square, to: Square): Boolean {
        val move = Move(from, to)
        return if (board.legalMoves().contains(move)) {
            board.doMove(move)
            true
        } else false
    }

    fun getPieceAt(square: Square): String {
        return board.getPiece(square).toString()
    }

//    fun reset() {
//        board.loadFromFen(Board.STARTING_POSITION)
//    }
}