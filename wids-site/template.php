<?php header('X-XSS-Protection: 0'); ?>
<!DOCTYPE html>
<html>
  <head>
    <title><?= $_PAGE['title'] ?></title>
    <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css" />
    <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap-theme.min.css" />
    <style type="text/css">
      p { padding: 15px; }
    </style>
  </head>
  <body>
    <nav class="navbar navbar-default" role="navigation">
      <ul class="nav navbar-nav">
        <li><a href="index.php">Home</a></li>

        <?php if (is_logged_in()) { ?>

          <li><a href="#">Logged in as: <?= $_PAGE['user']['username'] ?></a></li>
          <li><a href="profile.php?id=<?= $_SESSION['user_id'] ?>">My Profile</a></li>
          <li><a href="post.php">New post</a></li>
        </ul>
        <ul class="nav navbar-right">
          <li><a href="logout.php">Log Out</a></li>

        <?php } else { ?>
        
        </ul>
        <ul class="nav navbar-right">

          <li><a href="login.php">Log In</a></li>
          <li><a href="sign_up.php">Sign Up</a></li>

        <?php } ?>

      </ul>
    </nav>
    <div class="container">
      <?php echo $_PAGE['content']; ?>
    </div>
  </body>
</html>
