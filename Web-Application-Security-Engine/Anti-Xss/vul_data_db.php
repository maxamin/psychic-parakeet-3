<?php
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "student";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);
// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$sql = "SELECT * FROM vul_student";
$result = $conn->query($sql);

if ($result) {
    // output data of each row
    while($row = $result->fetch_assoc()) {
        echo "sno: " . $row["sno"]. " - Name: " . $row["name"]. " " . $row["uname"]."comment:".$row["comment"]. "<br>";
		
    }
} else {
    echo "0 results";
}
$conn->close();
?>