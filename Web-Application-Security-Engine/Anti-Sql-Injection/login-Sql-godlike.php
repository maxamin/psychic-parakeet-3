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
	
		
		//------------------------------------------------------------------------------------------------------
		//Preventing Sql Injection using stored procedures
		
		$query =  mysqli_query($connection,"call login('$username','$password')");
		//$result= 'SELECT @@ROWCOUNT;';
		
		$num=mysqli_fetch_array($query);
		if($num>0)
		{
			sleep(3);
			echo ('<script>alert("Login Successfull!! Welcome")</script>');
			echo "Logged in Successfully!!!";
			echo "Now Logging Out...";
			header( "refresh:3;url=Sql-Injection.html" );
		}
		else
		{
			echo ('<script>alert("Invalid Username or Password")</script>');
			header( "refresh:0.5;url=Sql-Injection.html" );
		}
	}
	    
	
	
?>