<?php
// File: chess_api/updateAvatar.php
header("Content-Type: application/json; charset=UTF-8");
include_once 'db_connect.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST' && isset($_FILES['file']) && isset($_POST['userId'])) {
    $userId = $_POST['userId'];
    $upload_dir = "uploads/";
    
    // Tạo tên file an toàn
    $file_name = time() . "_" . basename($_FILES["file"]["name"]);
    $target_path = $upload_dir . $file_name;
    
    // Đường dẫn đầy đủ để lưu vào DB (dùng cho Emulator 10.0.2.2)
    // Lưu ý: Thay đổi IP nếu chạy trên máy thật
    $full_url = "http://10.0.2.2/chess_api/" . $target_path;

    if (move_uploaded_file($_FILES["file"]["tmp_name"], $target_path)) {
        // 1. Cập nhật đường dẫn ảnh vào bảng nguoi_dung
        $stmt = $conn->prepare("UPDATE nguoi_dung SET duong_dan_anh = ? WHERE id = ?");
        $stmt->bind_param("si", $full_url, $userId);
        
        if ($stmt->execute()) {
            // 2. Lấy lại thông tin User mới nhất để trả về cho App cập nhật UI
            $stmt_user = $conn->prepare("SELECT id, email, ho_ten, so_dien_thoai, duong_dan_anh, ngay_tao FROM nguoi_dung WHERE id = ?");
            $stmt_user->bind_param("i", $userId);
            $stmt_user->execute();
            $result = $stmt_user->get_result();
            $user = $result->fetch_assoc();

            // Tính toán lại thời gian tham gia (copy logic từ login.php)
            $ngay_dang_ky = new DateTime($user['ngay_tao']);
            $hom_nay = new DateTime();
            $khoang_thoi_gian = $hom_nay->diff($ngay_dang_ky);
            $user['thoi_gian_tham_gia'] = $khoang_thoi_gian->days . " ngày"; // Đơn giản hóa ví dụ
            
            echo json_encode(["status" => "success", "message" => "Cập nhật ảnh thành công", "user" => $user]);
        } else {
            echo json_encode(["status" => "error", "message" => "Lỗi Database"]);
        }
    } else {
        echo json_encode(["status" => "error", "message" => "Lỗi di chuyển file"]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Thiếu dữ liệu gửi lên"]);
}
?>