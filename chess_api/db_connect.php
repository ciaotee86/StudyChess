<?php

ini_set('display_errors', 0);
ini_set('log_errors', 1);
error_reporting(E_ALL);

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "chess_app";

$conn = new mysqli($servername, $username, $password, $dbname);


$conn->set_charset("utf8mb4");

if ($conn->connect_error) {
    
    header('Content-Type: application/json; charset=UTF-8');
    echo json_encode([
        "status" => "error",
        "message" => "Lỗi kết nối CSDL: " . $conn->connect_error
    ], JSON_UNESCAPED_UNICODE);
    exit;
}