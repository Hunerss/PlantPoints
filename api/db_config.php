<?php 
    $db_host = "host_name";
    $db_name = "database_name";
    $db_user = "user_name";
    $db_pass = "password";

    $conn = new mysqli($db_host, $db_user, $db_pass, $db_name);

    if($conn->connect_error){
        die("Connection failed: " . $conn->connect_error);
    } else {
        echo "Connection successful";
    }
?>
