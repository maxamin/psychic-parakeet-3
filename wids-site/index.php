<?php

require_once('app.php');

$_PAGE['title'] = 'Home';
$_PAGE['content'] = <<<EOT
  <h1>Welcome to Meowwer!</h1>
EOT;

$user = get_logged_in_user();
if ($user === NULL)
{
  $_PAGE['content'].= '<p>You are not currently logged in.<p>'
    .'<p>Please <a href="login.php">Log In</a> or <a href="sign_up.php">Sign Up</a>.</p>';
}
else
{
  $_PAGE['content'].= '<p>Welcome, '.$user['username'].'</p>'
    .'<a href="/profile.php?id='.$user['id'].'">Click here to go to your profile</a>';
}

require('template.php');
