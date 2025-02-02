<?php

	$connection = mysqli_connect('localhost','root','','dswa');
	
	if(!$connection)
	{
		die("Database Connection Failed" .mysqli_error($connection));
	}
	/*$select_db = mysqli_select_db($connection,'DSWA');
	if(!$select_db)
	{
		die("Database Selection Failed" .mysqli_error($connection ));
	}*/
	
	    if(isset($_POST['name']) and isset($_POST['pass']))
	{
		$username=$_POST['name'] ;
		$password=$_POST['pass'];
		//----------------------------------------------------------------------------------------
		//Insecure Code
		$query = "Select * FROM test WHERE username='$username' and password='$password'";
		$result = mysqli_query($connection,$query) or die(mysqli_error($connection));
		
		$count = mysqli_num_rows($result);
			
			if($count==0)
			{
				echo ('<script>alert("Invalid Username or Password")</script>');
				header( "refresh:0.5;url=Sql-injection.html" );
				
			}
			else
			{
				sleep(3);
				echo ('<script>alert("Login Successfull!! Welcome")</script>');
				echo "Logged in Successfully!!!";
				echo "Now Logging Out...";
				header( "refresh:3;url=Sql-injection.html" );
			}
		

	}
	    
	
	
?>