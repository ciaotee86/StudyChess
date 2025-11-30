<?php
include_once 'db_connect.php';

if (isset($_GET['id'])) {
    $id = $_GET['id'];

    $sql = "SELECT * FROM tep_tin WHERE id=?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $id);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($row = $result->fetch_assoc()) {
        $file_path = $row['duong_dan'];

        if (file_exists($file_path)) {
            // Gửi file về client
            header('Content-Description: File Transfer');
            header('Content-Type: application/octet-stream');
            header('Content-Disposition: attachment; filename="' . basename($file_path) . '"');
            header('Content-Length: ' . filesize($file_path));
            readfile($file_path);
            exit;
        } else {
            echo json_encode(["status" => "error", "message" => "File không tồn tại"]);
        }
    } else {
        echo json_encode(["status" => "error", "message" => "Không tìm thấy file"]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Thiếu tham số ID"]);
}
$conn->close();
?>
