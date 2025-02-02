<?php

require_once('db.php');

session_start();

/**
 * Gets the currently logged-in user's details.
 * If a user is not logged in, or the id in the session is not valid, returns NULL.
 */
function get_logged_in_user()
{
  global $_PAGE;

  if (empty($_SESSION['user_id']))
    return NULL;

  $result = pg_query_params('select * from users where id = $1', array($_SESSION['user_id']));

  if (pg_num_rows($result) < 1)
    return NULL;

  $_PAGE['user'] = pg_fetch_assoc($result);
  return $_PAGE['user'];
}

function is_logged_in()
{
  return (get_logged_in_user() !== NULL);
}

function require_login()
{
  global $_PAGE;

  if (is_logged_in())
    return;

  $_PAGE['title'] = 'Error';
  $_PAGE['content'] = '<p>You must be logged in to view this page</p>';

  require('template.php');
  die('');
}
