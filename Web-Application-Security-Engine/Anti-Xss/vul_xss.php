<?php

$conn = mysqli_connect('localhost','root','','student');

if(!$conn)
{
	die('Connection failed!'.mysqli_error($conn));
}

$username = $_POST['username'];
$password = $_POST['password'];
$sql = "INSERT INTO student(username,password) VALUES('$username', '$password')";
 
 if(mysqli_query($conn,$sql))
{
	// echo "Registerd Successfully";
	//echo $sql;
	header("location:data_db.php");
}
else
{
	echo mysqli_error($conn);
}
?>