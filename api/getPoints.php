<?php
    header("Content-Type: application/json");
    include 'db_config.php';

    $query = "SELECT * FROM points";
    $result = $conn->query($query);

    $points = [];

    if ($result && $result->num_rows > 0) {
        while ($row = $result->fetch_assoc()) {
            $points[] = [
                "id" => $row['id'],
                "name" => $row['point_name'],
                "description" => $row['point_description'],
                "range" => $row['point_range'],
                "x_value" => $row['point_x_value'],
                "y_value" => $row['point_y_value']
            ];
        }
    } else {
        echo json_encode(["status" => "error", "message" => "No records found"]);
        exit;
    }

    echo json_encode($points);

    $conn->close();
?>
