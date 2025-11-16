package com.example.studychessapp.model

data class Lesson(
    val id: String,
    val title: String,
    val description: String,
    val content: String // Nội dung chi tiết của bài học
)

// Dữ liệu giả lập
object LessonRepository {
    val lessons = listOf(
        Lesson("KNIGHT_MOVE", "Cách di chuyển quân mã", "Học cách quân mã di chuyển hình chữ L.", "Quân mã đi hình chữ L: 2 ô theo một hướng và 1 ô vuông góc..."),
        Lesson("PAWN_MOVE", "Cách di chuyển quân tốt", "Tìm hiểu về nước đi đầu tiên, bắt quân, và phong cấp.", "Quân tốt di chuyển thẳng về phía trước 1 ô..."),
        Lesson("CASTLING", "Luật Nhập thành", "Cách di chuyển Vua và Xe cùng lúc.", "Nhập thành là một nước đi đặc biệt...")
    )
}