<?php 
error_reporting(0);
ob_start();
session_start();
if(isset($_GET['download'])){
header("Content-disposition: attachment; filename=".$_GET['download']."");
header("Content-type: application/json");
readfile($_GET['download']);
exit;
    }?>
<!DOCTYPE HTML>
<html>

<head>
  <title>SKF Test Enviroment</title>
  <meta name="description" content="website description" />
  <meta name="keywords" content="website keywords, website keywords" />
  <meta http-equiv="content-type" content="text/html; charset=windows-1252" />
  <link rel="stylesheet" type="text/css" href="style/style.css" />
</head>

<body>
  <div id="main">
    <div id="header">
      <div id="logo">
        <div id="logo_text">
          <h1><a href="index.php"><img src="img/logo.svg" height="35" width="35" /> Security Knowledge Framework</a></h1>
        </div>
      </div>
      <div id="menubar">
        <ul id="menu">
          <!-- put class="selected" in the li tag for the selected page - to highlight which page you're on -->
          <li><a href="index.php">Home</a></li>
          <li><a href="about.php?file=includes/about.txt">About</a></li>
          <li><a href="login.php">Login</a></li>    
        </ul>
      </div>
    </div>
