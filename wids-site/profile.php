<?php

require_once('app.php');

function profile_not_found()
{
  global $_PAGE;

  $_PAGE['title'] = 'Profile not found';
  $_PAGE['content'] = '<p>Sorry, the profile you specified was not found.</p>';

  require('template.php');
  die('');
}

function escape_content($content)
{
  return preg_replace('/(<(?!img).*?>)|(<.*?onload=.*?>)/i', '', $content);
}

// protect against empty param
if (empty($_GET['id']))
  profile_not_found();

// find the user who this profile belongs to
$profile_result = pg_query_params('select username from users where id = $1', array($_GET['id']));

// make sure there is a profile
if (pg_num_rows($profile_result) < 1)
  profile_not_found();

// retrieve the user's data
$profile_user = pg_fetch_assoc($profile_result);
$profile_user_name = htmlentities($profile_user['username']);

// set the page title
$_PAGE['title'] = $profile_user_name.'\'s Profile';

// find the user's posts
$posts_result = pg_query_params('select * from posts where user_id = $1', array($_GET['id']));

$posts_rows = '';
while($post = pg_fetch_assoc($posts_result))
{
  $posts_rows .= '<tr>';
  $posts_rows .= '<td>'.$post['id'].'</td>';
  $posts_rows .= '<td>'.escape_content($post['content']).'</td>';
  $posts_rows .= '</tr>';
}

$_PAGE['content'] = <<<EOT
  <h1>{$profile_user_name}'s Profile</h1>
  <h2>Posts</h2>
  <table class="table">
    <tr>
      <td>#</td>
      <td>Content</td>
    </tr>
    {$posts_rows}
  </table>
EOT;

require('template.php');
