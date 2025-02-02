<?php

	$connection = mysqli_connect('localhost','root','','dswa');
	
	if(!$connection)
	{
		die("Database Connection Failed" .mysqli_error($connection));
	}
	    if(isset($_POST['username']) and isset($_POST['password']))
	{
		$username=$_POST['username'];
		$password=$_POST['password'];
		
		
			$query = "Select * FROM test WHERE username='$username' and password='$password'";
			
			$result = mysqli_query($connection,$query) or die(mysqli_error($connection));
			
			$count = mysqli_num_rows($result);
			
			

					
			if($count==0)
			{
				echo ('<script>alert("Invalid Username or Password")</script>');
				$j = "select attempt from test where username='$username'";
				$r = mysqli_query($connection,$j);
				$value = mysqli_fetch_assoc($r);
           
				
				if($value["attempt"]==0)
				{
					$i="update test set attempt='1' where username='$username'";
			        $status = mysqli_query($connection,$i);
					echo "you have done 1 wrong attempts";
					header( "refresh:5.0;url=index.html" );
				}
				if($value["attempt"]==1)
				{
					$i="update test set attempt='2' where username='$username'";
			        $status = mysqli_query($connection,$i);
					echo "you have done 2 wrong attempts";
					header( "refresh:5.0;url=index.html" );
				}
				if($value["attempt"]==2)
				{
					$i="update test set attempt='3' where username='$username'";
			        $status = mysqli_query($connection,$i);
					echo "you have done 3 wrong attempts";
					header( "refresh:5.0;url=index.html" );
				}
				if($value["attempt"]==3)
				{
					echo "You are banned";
					header( "refresh:5.0;url=index.html" );
				}
				
				
			}
			else
			{
					echo ('<script>alert("Welcome")</script>');
				    echo("You've been logged in success");
					$i="update test set attempt='0' where username='$username'";
			        $status = mysqli_query($connection,$i);	
					header( "refresh:2.0;url=index.html" );
			}	
    
	
	}
	    
	
?>