<?php include('includes/menu.php'); ?>
<?php include('includes/sidebar.php'); ?>

      <div id="content">
        <h1>Search page</h1>
		<h3>You have searched the website for:</h3>
		<p><?php echo $_GET['search'];?></p>
      </div>
    </div>
<?php include('includes/footer.php');
