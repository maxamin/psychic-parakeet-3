<?php
$servername = "sql12.freemysqlhosting.net";
$username = "sql12341324";
$password = "e68zyBj4Xl";
$dbname = "sql12341324";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);
// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$sql = "SELECT * FROM student";
$result = $conn->query($sql);

if ($result) {
    // output data of each row
	echo " <body bgcolor=black><font color='white'><table border='1' align='center' cellpadding='1' cellspacing='1'>
    <tr>
        <td>Name</td>
        <td>Email</td>
		</tr></body>";

    while($row = $result->fetch_assoc()) {
        echo " <tr>     <td>".$row['username']."</td>     <td>".$row['password']."</td> </tr>";	
		
    }
} else {
    echo "0 results";
}
$conn->close();
?>