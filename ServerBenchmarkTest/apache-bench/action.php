Output der Datenbank:  
<?php

$username = "httpUser";
$password = "simpleP4ssPhrase4Testing";
$database = "htmlDB";
$server = "localhost";

$connection = new mysqli($server, $username, $password, $database);
if($connection->connect_error) {
    die("Connection to DB failed: " . $connection->connect_error . "<br>");
} else {
    echo "Connection established <br>";
}

$input = htmlspecialchars($_POST['sql']);
echo "Input: " . $input. "<br>";

$sql = "select * from htmlInput where input = '" . $input . "'";
echo "Query: " . $sql . "<br>";

if($connection->multi_query($sql)) {
    do {
        if($result = $connection->store_result()) {
            while($row = $result->fetch_assoc()) {
                echo "Result: " . $row["id"]. ", " . $row["input"]. "<br>";
            }
        }
    } while ($connection->next_result());
} else {
    echo "No result or error <br>" . $connection->error;
}

$connection->close();

?>
