<?php

ob_start();
session_start();
if($_SESSION['access'] != "active"){
if($_SESSION['access'] == ""){
header("location:login.php");
}
}
?>

<?php include('includes/menu.php'); ?>
<?php include('includes/sidebar.php'); ?>

<h1>Upload your User avatar:</h1>
<form action="upload.php" method="post" enctype="multipart/form-data">
    Select image to upload:
    <input type="file" name="fileToUpload" id="fileToUpload">
    <input type="submit" value="Upload Image" name="submit">
</form>

<?php


// Check if image file is a actual image or fake image
if(isset($_POST["submit"])) {

    $target_dir = "upload/";
    $target_file = $target_dir . basename($_FILES["fileToUpload"]["name"]);
    $uploadOk = 1;
    $imageFileType = pathinfo($target_file,PATHINFO_EXTENSION);

    $check = getimagesize($_FILES["fileToUpload"]["tmp_name"]);
    if($check !== false) {
        echo "File is an image - " . $check["mime"] . ".";
        $uploadOk = 1;
    } else {
        echo "File is not an image.";
        $uploadOk = 0;
    }
}

// Check if $uploadOk is set to 0 by an error
if ($uploadOk == 0) {
    echo "Sorry, your file was not uploaded.";
// if everything is ok, try to upload file
} else {
    if (move_uploaded_file($_FILES["fileToUpload"]["tmp_name"], $target_file)) {
        echo "<br><br>The file ". basename( $_FILES["fileToUpload"]["name"]). " has been uploaded.<br>";
        echo "<img src='upload/". basename( $_FILES["fileToUpload"]["name"]). "' width='300px' height='300px'>";
        exec("chmod 777 -R  /vagrant/www/skf-workshop/upload");
    } else {
        echo "Sorry, there was an error uploading your file.";
    }
}

?>

<?php include('includes/footer.php'); ?>
