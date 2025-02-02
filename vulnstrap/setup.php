<?php
include("config.php");

mysql_query("drop database vulnex");

mysql_query("create database vulnex");

mysql_select_db("vulnex", $con);

mysql_query("create table administrator (username varchar(30), password varchar(30))");

mysql_query("insert into administrator values('admin', 'password'), ('celso', 'celso!@#')");

mysql_query("create table articles (id int, title varchar(50), body varchar(250))");

mysql_query("insert into articles values (1, 'Why We Should Party', 'Coz according to Jolly Mongrel, the most important thing in life is to celebrate it while you are still an air-breathing creature roaming on Mother Earth with fervor, humor and without hesitation.!')");

mysql_query("create table inventory (item_number int, item varchar(50), owner varchar(50))");

mysql_query("insert into inventory values(1, 'chair', 'Harris Solemnio'), 
			(2, 'pen', 'Jayson Vallente'), (3, 'laptop', 'Jay Turla') ");

echo "<script>alert('Vulnstrap is armed and ready!');document.location.href='index.php'</script>";
?>