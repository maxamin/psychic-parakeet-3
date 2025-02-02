<?php

require_once('app.php');

$_PAGE['title'] = 'Log In';

$error_message = '';
if (!empty($_GET['error_message']))
  $error_message = '<p class="bg-danger">'.urldecode($_GET['error_message']).'</p>';

$_PAGE['content'] = <<<EOT
  {$error_message}
  <form method="post" action="login_submit.php" role="form">
    <div class="form-group">
      <label for="username">Username</label>
      <input type="text" class="form-control" id="username" name="username" placeholder="Enter Username" />
    </div>
    <div class="form-group">
      <label for="password">Password</label>
      <input type="password" class="form-control" id="password" name="password" placeholder="Enter Password" />
    </div>
    <button type="submit" class="btn btn-default">Log In</button>
  </form>
EOT;

require('template.php');
