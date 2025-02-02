Das Ergebnis ist: 
<?php
echo "Beginn";

$username = "httpUser";
$password = "simpleP4ssPhrase4Testing";
$database = "htmlDB";
$server = "localhost";

echo "Starting";

$connection = new mysqli($server, $username, $password, $database);
if($connection->connect_error) {
    die("Connection to DB failed: " . $connection->connect_error);
}
echo "Tried to connect";


$order = "select * from htmlInput where input = '$_POST['sql']; ?>'";

$result = $connection->query($order);
if($result === TRUE) {
    echo "Result of Query: " . $result;
} else {
    echo "No result or error" . $connection->error;
}

$connection->close();
?>

Weiterer Text