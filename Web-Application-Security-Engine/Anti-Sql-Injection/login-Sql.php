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
		/*$query = "Select * FROM test WHERE username='$username' and password='$password'";
		$result = mysqli_query($connection,$query) or die(mysqli_error($connection));
		
		$count = mysqli_num_rows($result);
			
			if($count==0)
			{
				echo ('<script>alert("Invalid Username or Password")</script>');
				header( "refresh:0.5;url=index.html" );
				
			}
			else
			{
				sleep(3);
				echo ('<script>alert("Login Successfull!! Welcome")</script>');
				echo "Logged in Successfully!!!";
				echo "Now Logging Out...";
				header( "refresh:3;url=index.html" );
			}*/
		
		//----------------------------------------------------------------------------------------
		//Preventing SQL_INJECTION using MySqli_real_escape_string function
		
		/*$username=mysqli_real_escape_string($connection,$username);
		$password=mysqli_real_escape_string($connection,$password);
		
		$query = "Select * FROM test WHERE username='$username' and password='$password'";
		$result = mysqli_query($connection,$query) or die(mysqli_error($connection));
		
		$count = mysqli_num_rows($result);
			
			if($count==0)
			{
				echo ('<script>alert("Invalid Username or Password")</script>');
				//header( "refresh:0.5;url=index.html" );
				
			}
			else
			{
				sleep(3);
				echo ('<script>alert("Login Successfull!! Welcome")</script>');
				echo "Logged in Successfully!!!";
				echo "Now Logging Out...";
				//header( "refresh:3;url=index.html" );
			}*/
		
		//-------------------------------------------------------------------------------------------------------
		//Preventing SQL_INJECTION using parameterized or prepared statements query
		
		$stmt = $connection->prepare("SELECT username, password FROM test WHERE username=? AND  password=?");
		$stmt->bind_param('ss', $username, $password);
		$stmt->execute();
		$stmt->bind_result($username, $password);
		$stmt->store_result();
		$result=$stmt->num_rows;
		
		if($result == 0)
		{
			echo ('<script>alert("Invalid Username or Password")</script>');
			header( "refresh:0.5;url=Sql-Injection.html" );
		}
		else
		{
			sleep(3);
			echo ('<script>alert("Login Successfull!! Welcome")</script>');
			echo "Logged in Successfully!!!";
			echo "Now Logging Out...";
			header( "refresh:3;url=Sql-Injection.html" );
		}
		
		//------------------------------------------------------------------------------------------------------
		//Preventing Sql Injection using stored procedures
		
		/*$query =  mysqli_query($connection,"CALL login('$username','$password')");
		//$result= 'SELECT @@ROWCOUNT;';
		
		if(!$query)
		{
			echo ('<script>alert("Invalid Username or Password")</script>');
			header( "refresh:0.5;url=index.html" );
		}
		else
		{
			sleep(3);
			echo ('<script>alert("Login Successfull!! Welcome")</script>');
			echo "Logged in Successfully!!!";
			echo "Now Logging Out...";
			header( "refresh:3;url=index.html" );
		}*/
	}
	    
	
	
?>