<?php

require_once('app.php');

$_PAGE['title'] = 'Log In';

/**
 * Redirects a user to login.php when there is an error.
 */
function error_redirect($message)
{
  global $_PAGE;

  header("Location: /login.php?error_message=". urlencode($message));

  $_PAGE['content'] = "<p>Login failed. You are being redirected.</p>";

  require('template.php');
  die();
}

// make sure the user posted both a username and password
if (empty($_POST['username']))
  error_redirect("Please enter your username.");
elseif (empty($_POST['password']))
  error_redirect("Please enter your password.");

// check their credentials
$result = pg_query_params('select * from users where username = $1', array($_POST['username']));

if (pg_num_rows($result) < 1)
  error_redirect("Username or password incorrect.");

$user = pg_fetch_assoc($result);
if ($user['password'] != md5($_POST['password']))
  error_redirect("Username or password incorrect.");

// we've checked the username and password, we have a valid login
// save the user's id in the session so we know who they're logged in as
$_SESSION['user_id'] = $user['id'];

// redirect them to their profile
header("Location: /profile.php?id=".$user['id']);
$_PAGE['content'] = <<<EOT
  <p>Login successful. You are being redirected.</p>
EOT;

require('template.php');
