<?php 
ob_start();
session_start();
login(); ?>
<?php require_once('includes/menu.php'); ?>
   
    <div id="site_content">
      <div class="sidebar">
      </div>

      <div id="content">
		  <div style="margin-left:50%;" class="form_settings">
				<br/><br/>
				<h1>Please login!</h1>
				<form method="post">
					<h4>username:</h4>
					<input type="text" value="<?php if(!empty($_POST['username'])){ echo $_POST['username'];} ?>" name="username"/>
					<h4>Password</h4>
					<input type="password" name="password"/>
					<br/><br/>
					<input class="submit" type="submit" value="login" name="contact_submitted">
					<br/><br/><br/>
				</form>	
		  </div>
      </div>
    </div>
<?php include('includes/footer.php');

function login(){
		$db = new PDO('mysql:host=localhost;dbname=test;charset=utf8', 'root', 'root');
		$db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
		$db->setAttribute(PDO::ATTR_EMULATE_PREPARES, false);
		
		if(!empty($_POST['contact_submitted'])){
 			$username = $_POST['username'];
 			$password = $_POST['password'];
        
			$stmt = $db->prepare("SELECT * FROM users WHERE username=?");
			$stmt->execute(array($username));
			$rows = $stmt->fetchAll(PDO::FETCH_ASSOC);
		
			if(!$rows){
			echo "<script>alert('wrong username/password');</script>";
			}
			/*
			Than we validate the password, if the validation is true than we set the sessions
			For more detailed information on password validation check please look into the
			Password storage(salting/stretching/hashing) in the knowledgebase for more information.
			*/
			foreach($rows as $loginUser){
				if($loginUser['password'] == $password)
				{
					session_start();
					$_SESSION['userID'] = $loginUser['userID'];
					$_SESSION['access']   = "active";

					//The CSRF token is set here by an aproved random number generator
					$_SESSION['csrf'] = base64_encode(openssl_random_pseudo_bytes(128));
				
					header("location:loggedin.php?userID=".$loginUser['userID']."");
									
					return $loginUser;
				}
				else{
				echo "<script>alert('wrong password!');</script>";
				}
			}
    	}
    }
?>
