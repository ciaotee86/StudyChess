<?php
// Đặt header ở trên cùng
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
date_default_timezone_set('Asia/Ho_Chi_Minh');

// Sử dụng file kết nối chung
include_once 'db_connect.php'; 


if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    echo json_encode(["status" => "error", "message" => "Sai phương thức, phải là POST"], JSON_UNESCAPED_UNICODE);
    exit;
}

// Nhận dữ liệu POST 
$ten_dang_nhap = $_POST['username'] ?? '';
$email = $_POST['email'] ?? '';
$mat_khau = $_POST['password'] ?? '';
$so_dien_thoai = $_POST['sdt'] ?? '';
$ho_ten = $_POST['ho_ten'] ?? '';

// Kiểm tra thiếu dữ liệu
if (empty($ten_dang_nhap) || empty($email) || empty($mat_khau)) {
    echo json_encode(["status" => "error", "message" => "Thiếu thông tin đăng ký"], JSON_UNESCAPED_UNICODE);
    exit;
}

// Mã hóa mật khẩu
$mat_khau_ma_hoa = password_hash($mat_khau, PASSWORD_DEFAULT);
$da_xac_thuc = 0;
$ngay_tao = date("Y-m-d H:i:s");

// Thêm người dùng 
$stmt = $conn->prepare("INSERT INTO nguoi_dung (ten_dang_nhap, email, mat_khau, da_xac_thuc, so_dien_thoai, ho_ten, ngay_tao)
                         VALUES (?, ?, ?, ?, ?, ?, ?)");
$stmt->bind_param("sssisss", $ten_dang_nhap, $email, $mat_khau_ma_hoa, $da_xac_thuc, $so_dien_thoai, $ho_ten, $ngay_tao);

if (!$stmt->execute()) {
    echo json_encode(["status" => "error", "message" => "Không thể thêm người dùng: " . $stmt->error], JSON_UNESCAPED_UNICODE);
    $stmt->close();
    $conn->close();
    exit;
}

$id_nguoi_dung = $stmt->insert_id;
$stmt->close();

// Nếu có file upload (đã xóa các ký tự thụt lề lạ)
if (isset($_FILES['file']) && $_FILES['file']['error'] === UPLOAD_ERR_OK) {
    $upload_dir = "uploads/";
    if (!file_exists($upload_dir)) {
        mkdir($upload_dir, 0777, true);
    }

    $ten_file_goc = basename($_FILES["file"]["name"]);
    $loai_file = pathinfo($ten_file_goc, PATHINFO_EXTENSION);
    $kich_thuoc = $_FILES["file"]["size"];
    $duong_dan = $upload_dir . time() . "_" . $ten_file_goc;

    if (move_uploaded_file($_FILES["file"]["tmp_name"], $duong_dan)) {
        $trang_thai = 'hoạt động';
        $ngay_tao = date("Y-m-d H:i:s");

        $stmt2 = $conn->prepare("INSERT INTO tep_tin (nguoi_dung_id, ten_file, loai_file, kich_thuoc, duong_dan, trang_thai, ngay_tao)
                                VALUES (?, ?, ?, ?, ?, ?, ?)");
        $stmt2->bind_param("ississs", $id_nguoi_dung, $ten_file_goc, $loai_file, $kich_thuoc, $duong_dan, $trang_thai, $ngay_tao);
        $stmt2->execute();
        $stmt2->close();
    } else {
        echo json_encode(["status" => "warning", "message" => "Đăng ký thành công nhưng upload file thất bại"], JSON_UNESCAPED_UNICODE);
        $conn->close();
        exit;
    }
}

echo json_encode(["status" => "success", "message" => "Đăng ký thành công"], JSON_UNESCAPED_UNICODE);
$conn->close();
?>