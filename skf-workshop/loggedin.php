<?php 
ob_start();
session_start();
if($_SESSION['access'] != "active"){
if($_SESSION['access'] == ""){
header("location:login.php");
}
}

$db = new PDO('mysql:host=localhost;dbname=test;charset=utf8', 'root', 'root');
$db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
$db->setAttribute(PDO::ATTR_EMULATE_PREPARES, false);

$stmt = $db->prepare("SELECT * FROM pages WHERE userID=?");
$stmt->execute(array($_GET['userID']));
$rows = $stmt->fetchAll(PDO::FETCH_ASSOC);

foreach($rows as $info){

$text = $info['text'];
}


 ?>
<?php include('includes/menu.php'); ?>
<?php include('includes/sidebar.php'); ?>

<div id="content">
	<h1>You have been authenticated</h1>
    <h4>Update your page information!</h4>
    Upload an image for your avatar!<br>
    <a href="upload.php">Upload Avatar</a>
    <br/>	
    <?php echo $text; ?>	
    <br/><br/><br/><br/>
	<form action="submitted.php?userID=<?php echo $_GET['userID']; ?>" method="post">
    	<div class="form_settings">
            <p><span>My information:</span><textarea class="contact textarea" rows="8" cols="50" name="your_enquiry"><?php echo $text; ?></textarea></p>
            <p style="padding-top: 15px"><span>&nbsp;</span><input class="submit" type="submit" name="update" value="submit" /></p>
        </div>
    </form>
</div>
    </div>
<?php include('includes/footer.php');

	
	
