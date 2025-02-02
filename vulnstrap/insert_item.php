<?php
include("config.php");

mysql_select_db("vulnex");

$item_number = $_POST["item_number"];
$item = $_POST["item"];
$owner = $_POST["owner"];

mysql_query("INSERT INTO inventory VALUES('$item_number','$item', '$owner')");

echo "<script>alert('Data Inserted!WEEEEH!');document.location.href='items.php'</script>";

?>