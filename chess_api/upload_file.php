<?php
include_once 'db_connect.php';

if (isset($_POST['nguoi_dung_id']) && isset($_FILES['file'])) {

    $nguoi_dung_id = $_POST['nguoi_dung_id'];
    $file = $_FILES['file'];
    $upload_dir = "uploads/";

    // Tạo thư mục nếu chưa có
    if (!is_dir($upload_dir)) {
        mkdir($upload_dir, 0777, true);
    }

    $file_name = basename($file["name"]);
    $target_path = $upload_dir . uniqid() . "_" . $file_name;

    if (move_uploaded_file($file["tmp_name"], $target_path)) {
        $loai_file = pathinfo($file_name, PATHINFO_EXTENSION);
        $kich_thuoc = filesize($target_path);

        $sql = "INSERT INTO tep_tin (nguoi_dung_id, ten_file, loai_file, kich_thuoc, duong_dan, trang_thai)
                VALUES (?, ?, ?, ?, ?, 'uploaded')";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("issis", $nguoi_dung_id, $file_name, $loai_file, $kich_thuoc, $target_path);

        if ($stmt->execute()) {
            echo json_encode([
                "status" => "success",
                "message" => "Tải file thành công",
                "duong_dan" => $target_path
            ]);
        } else {
            echo json_encode(["status" => "error", "message" => "Lưu vào database thất bại"]);
        }
    } else {
        echo json_encode(["status" => "error", "message" => "Không thể tải file lên"]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Thiếu tham số hoặc file"]);
}

$conn->close();
?>
