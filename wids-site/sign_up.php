<?php

require_once('app.php');

$_PAGE['title'] = 'Sign Up';

$error_message = '';
if (!empty($_GET['error_message']))
  $error_message = '<p class="bg-danger">'.urldecode($_GET['error_message']).'</p>';

$_PAGE['content'] = <<<EOT
  {$error_message}
  <form method="post" action="sign_up_submit.php" role="form">
    <div class="form-group">
      <label for="username">Username</label>
      <input type="text" class="form-control" id="username" name="username" placeholder="Enter Username" />
    </div>
    <div class="form-group">
      <label for="password">Password</label>
      <input type="password" class="form-control" id="password" name="password" placeholder="Enter Password" />
    </div>
    <div class="form-group">
      <label for="password_confirmation">Confirm Password</label>
      <input type="password" class="form-control" id="password_confirmation" name="password_confirmation" placeholder="Re-Type Password" />
    </div>
    <div class="form-group">
      <label for="pic_url">Profile Picture URL</label>
      <input type="text" class="form-control" id="pic_url" name="pic_url" placeholder="http://mysite.com/profile_pic.png" />
    </div>
    <button type="submit" class="btn btn-default">Sign Up</button>
  </form>
EOT;

require('template.php');
