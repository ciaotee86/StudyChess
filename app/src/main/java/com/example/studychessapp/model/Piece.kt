package com.example.studychessapp.model

enum class PieceType { KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN }
enum class PieceColor { WHITE, BLACK }

data class Piece(val type: PieceType, val color: PieceColor)
