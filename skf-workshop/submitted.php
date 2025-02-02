<?php 
session_start();
if($_SESSION['access'] != "active"){
header("location:login.php");
}

$db = new PDO('mysql:host=localhost;dbname=test;charset=utf8', 'root', 'root');
$db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
$db->setAttribute(PDO::ATTR_EMULATE_PREPARES, false);

if(isset($_POST['update'])){

$stmt = $db->prepare("UPDATE pages SET text=? WHERE userID=?");
$stmt->execute(array($_POST['your_enquiry'], $_GET['userID']));
$affected_rows = $stmt->rowCount();
}
 ?>
<?php include('includes/menu.php'); ?>
<?php include('includes/sidebar.php'); ?>

      <div id="content">
<h1>Content submission</h1>
<h3>Succesfull submit</h3>
<p>Your page information has been updated!<br/>
<a href="loggedin.php?userID=<?php echo $_GET['userID']; ?>">Return</a> to update page
</p>

    </div>
    </div>
<?php include('includes/footer.php');

	
	
