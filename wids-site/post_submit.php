<?php

require_once('app.php');
require_login();

$_PAGE['title'] = 'Post';

/**
 * Redirects a user to login.php when there is an error.
 */
function error_redirect($message)
{
  global $_PAGE;

  header("Location: /post.php?error_message=". urlencode($message));

  $_PAGE['content'] = "<p>Post failed. You are being redirected.</p>";

  require('template.php');
  die();
}

// make sure the user posted both a username and password
if (empty($_POST['content']))
  error_redirect("Please enter your post content!");

// insert the data
// assume it worked, dont bother error checking
pg_query_params('insert into posts (user_id, content) values ($1, $2)',
  array($_SESSION['user_id'], $_POST['content']));

// redirect them to their profile
header("Location: /profile.php?id=".$_SESSION['user_id']);
$_PAGE['content'] = <<<EOT
  <p>Post successful. You are being redirected.</p>
EOT;

require('template.php');
