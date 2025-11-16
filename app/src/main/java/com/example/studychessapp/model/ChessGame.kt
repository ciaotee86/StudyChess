package com.example.studychessapp.model

data class Tile(val piece: String = "", val isSelected: Boolean = false)

class ChessGame {
    var board: Array<Array<Tile>> = Array(8) { Array(8) { Tile() } }
    var selected: Pair<Int, Int>? = null
    var gameOver: Boolean = false
    var winner: String = ""

    init {
        setupBoard()
    }

    fun setupBoard() {
        board[0][4] = Tile("♚") // vua đen
        board[7][4] = Tile("♔") // vua trắng
        for (i in 0..7) {
            board[1][i] = Tile("♟") // tốt đen
            board[6][i] = Tile("♙") // tốt trắng
        }
    }

    fun selectTile(row: Int, col: Int): Boolean {
        if (gameOver) return false

        val current = board[row][col]
        if (selected == null && current.piece.isNotEmpty() && current.piece == "♙") {
            selected = Pair(row, col)
            board[row][col] = current.copy(isSelected = true)
        } else if (selected != null) {
            val (fromRow, fromCol) = selected!!
            val fromTile = board[fromRow][fromCol]
            val targetTile = board[row][col]

            // Kiểm tra nước đi hợp lệ đơn giản: tốt trắng đi lên 1 ô
            if (row == fromRow - 1 && col == fromCol) {
                if (targetTile.piece == "♚") {
                    gameOver = true
                    winner = "Người chơi"
                }
                board[row][col] = fromTile.copy(isSelected = false)
                board[fromRow][fromCol] = Tile()
                selected = null
                return true
            } else {
                // Hủy chọn nếu nước đi không hợp lệ
                board[fromRow][fromCol] = fromTile.copy(isSelected = false)
                selected = null
            }
        }
        return false
    }

    fun aiMove() {
        if (gameOver) return

        for (i in 0..6) {
            for (j in 0..7) {
                if (board[i][j].piece == "♟" && board[i + 1][j].piece == "") {
                    if (board[i + 1][j].piece == "♔") {
                        gameOver = true
                        winner = "AI"
                    }
                    board[i + 1][j] = board[i][j]
                    board[i][j] = Tile()
                    return
                }
            }
        }
    }

    fun resetBoard() {
        board = Array(8) { Array(8) { Tile() } }
        selected = null
        gameOver = false
        winner = ""
        setupBoard()
    }
}