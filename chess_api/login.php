<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");

// Đặt múi giờ để tính toán cho chính xác
date_default_timezone_set('Asia/Ho_Chi_Minh');

include_once 'db_connect.php';

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    echo json_encode(["status" => "error", "message" => "Sai phương thức, phải là POST"], JSON_UNESCAPED_UNICODE);
    exit;
}

$email = $_POST['email'] ?? '';
$password = $_POST['password'] ?? '';

if (empty($email) || empty($password)) {
    echo json_encode(["status" => "error", "message" => "Thiếu thông tin đăng nhập"], JSON_UNESCAPED_UNICODE);
    exit;
}

// **CẢI TIẾN 1:** Thêm "ngay_tao" vào câu SELECT
$stmt = $conn->prepare("SELECT id, email, mat_khau, ho_ten, so_dien_thoai, ngay_tao FROM nguoi_dung WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    $user = $result->fetch_assoc();
    
    // Kiểm tra mật khẩu
    if (password_verify($password, $user['mat_khau'])) {
        
        // --- TÍNH THỜI GIAN THAM GIA ---
        try {
            $ngay_dang_ky = new DateTime($user['ngay_tao']);
            $hom_nay = new DateTime(); // Lấy thời gian hiện tại
            $khoang_thoi_gian = $hom_nay->diff($ngay_dang_ky);

            // Format (định dạng) chuỗi thời gian cho dễ đọc
            $thoi_gian_str = "";
            if ($khoang_thoi_gian->y > 0) {
                $thoi_gian_str = $khoang_thoi_gian->y . " năm";
            } elseif ($khoang_thoi_gian->m > 0) {
                $thoi_gian_str = $khoang_thoi_gian->m . " tháng";
            } elseif ($khoang_thoi_gian->d > 0) {
                $thoi_gian_str = $khoang_thoi_gian->d . " ngày";
            } elseif ($khoang_thoi_gian->h > 0) {
                 $thoi_gian_str = $khoang_thoi_gian->h . " giờ";
            } elseif ($khoang_thoi_gian->i > 0) {
                 $thoi_gian_str = $khoang_thoi_gian->i . " phút";
            } else {
                $thoi_gian_str = "Vừa mới tham gia";
            }

            // **CẢI TIẾN 2:** Thêm trường mới vào mảng $user
            $user['thoi_gian_tham_gia'] = $thoi_gian_str;

        } catch (Exception $e) {
            // Nếu có lỗi khi xử lý ngày (ví dụ: $user['ngay_tao'] bị null)
            $user['thoi_gian_tham_gia'] = "Không rõ";
        }
        // --- KẾT THÚC TÍNH TOÁN ---

        // Cải tiến bảo mật: Xóa mật khẩu khỏi mảng trước khi trả về
        unset($user['mat_khau']); 
        
        // **CẢI TIẾN 3:** Giờ đây $user đã chứa thêm "thoi_gian_tham_gia"
        echo json_encode([
            "status" => "success",
            "message" => "Đăng nhập thành công",
            "user" => $user 
        ], JSON_UNESCAPED_UNICODE); 

    } else {
        echo json_encode(["status" => "error", "message" => "Sai mật khẩu"], JSON_UNESCAPED_UNICODE);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Email không tồn tại"], JSON_UNESCAPED_UNICODE);
}

$stmt->close();
$conn->close();
?>