<?php

$conn = mysqli_connect('sql12.freemysqlhosting.net','sql12341324','e68zyBj4Xl','sql12341324');

if(!$conn)
{
	die('Connection failed!'.mysqli_error($conn));
}



$username = $_POST['username'];
$password = $_POST['password'];
if ($username==strip_tags($username) and $password==strip_tags($password))
{
$sql = "INSERT INTO student(username,password) VALUES('$username', '$password')";
 if(mysqli_query($conn,$sql))
{
	echo "alert('Registerd Successfully');";
	header("location:data_db.php");
}
else
{
	echo mysqli_error($conn);
}
}

else
{
	echo("not Registerd ");
	sleep(3);
	header("location:registration.html");
}


?>