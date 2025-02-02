<?php

require_once('app.php');
require_login();

$_PAGE['title'] = 'New Post';

$error_message = '';
if (!empty($_GET['error_message']))
  $error_message = '<p class="bg-danger">'.urldecode($_GET['error_message']).'</p>';

$_PAGE['content'] = <<<EOT
  {$error_message}
  <form method="post" action="post_submit.php" role="form">
    <div class="form-group">
      <label for="content">Post Content</label>
      <input type="text" class="form-control" id="content" name="content" placeholder="Enter your content.. maybe something nice about cats?" />
    </div>
    <button type="submit" class="btn btn-default">Meow!</button>
  </form>
EOT;

require('template.php');
