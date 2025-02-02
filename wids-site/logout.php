<?php

require_once('app.php');

unset($_SESSION['user_id']);

$_PAGE['title'] = 'Logged Out';
$_PAGE['content'] = '<p>You have successfully logged out</p>';

require('template.php');
