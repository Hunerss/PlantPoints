<?php
header("Content-Type: application/json");
include 'db_config.php';
 
$data = json_decode(file_get_contents("php://input"), true);
 
$name = $data['name'] ?? null;
$description = $data['description'] ?? null;
$range = $data['range'] ?? null;
$x_value = $data['x_value'] ?? null;
$y_value = $data['y_value'] ?? null;
 
if (empty($name)) {
    echo json_encode(["status" => "error", "message" => "Point name is required"]);
    exit;
} elseif (empty($description)) {
    echo json_encode(["status" => "error", "message" => "Point description is required"]);
    exit;
} elseif (!is_numeric($range) || $range < 1 || $range > 10000) {
    echo json_encode(["status" => "error", "message" => "Point range is either too big or too small"]);
    exit;
} elseif (!is_numeric($x_value) || !is_numeric($y_value)) {
    echo json_encode(["status" => "error", "message" => "X and Y values must be numeric"]);
    exit;
}
 
$query = $conn->prepare("INSERT INTO points (name, description, range, x_value, y_value) VALUES (?, ?, ?, ?, ?)");
$query->bind_param("ssiii", $name, $description, $range, $x_value, $y_value);
 
if ($query->execute()) {
    echo json_encode(["status" => "success", "message" => "Point added successfully", "id" => $conn->insert_id]);
} else {
    echo json_encode(["status" => "error", "message" => "Failed to add point"]);
}
 
$query->close();
$conn->close();
?>
