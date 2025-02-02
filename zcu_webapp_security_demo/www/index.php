<?php
  session_start();

  // database management
  $conn = mysql_connect('localhost', 'root', '');
  if (!$conn) {
    die("Connection failed: " . mysql_error());
  }
  mysql_select_db('zcu_demo');

  // action management
  $_REQUEST = array_merge($_GET, $_POST);
  if (isset($_REQUEST['action'])) $action = $_REQUEST['action'];
//echo "ACTION: " . $action . ' | REQUEST: '; print_r($_REQUEST); echo " | SESSION: "; print_r($_SESSION);

  // current user
  $current_user_id = -1;
  $current_user = '[not logged in]';
  if (array_key_exists('id', $_SESSION)) {
    $current_user_id = $_SESSION['id'];
    $result = mysql_query('SELECT username FROM user WHERE id=' . $current_user_id);
    $row = mysql_fetch_array($result);
    $current_user = $row['username'];
  } else {
    if ('login' != $action) unset($action); // no action means: go to login
  }
?>

<html>
  <body>
    <h3>ZCU WebApp Security Demo</h3>

<?php
echo '<h5>Logged as: ' . $current_user . "</h5>[<a href='index.php?action=logout'>logout</a>]<hr>";

// -------------------------- DISPLAY LOGIN FORM
if (!$action) {
?>
  <form method='post'>
    Username:<br>
    <input name='username' type='text'>
    <br>
    Password:<br>
    <input name='password' type='password'>
    <br>
    <input type='submit' value='Login'>
    <input type='hidden' name='action' value='login'>
  </form>


<?php
// -------------------------- LOGIN
} elseif ('login' == $action) {
  $result = mysql_query("SELECT id,password FROM user WHERE username='" . $_REQUEST['username'] . "'" . " AND password='" . $_REQUEST['password'] . "'");
  $row = mysql_fetch_assoc($result);
  if ($row) {
    $_SESSION['id'] = $row['id'];
    header("Location: index.php?action=list"); // redirect to list of notices
    exit;
  } else {
    echo "wrong username/password";
  }


} elseif ('logout' == $action) {
  unset($_SESSION['id']);
  header("Location: index.php");
  exit;
?>


<?php
// -------------------------- DISPLAY LIST OF NOTICES
} elseif ('create' == $action || 'list' == $action) {
  if ('create' == $action) {
    // ---------------------- CREATE NEW NOTICE
    mysql_query("INSERT INTO notice_board(author_id,notice) VALUES(" . $current_user_id . ",'" . $_REQUEST['notice'] . "')");
  }
?>
  <form method='post'>
    Notice:<br>
    <input name='notice' type='text'>
    <input type='submit' value='Put'>
    <input type='hidden' name='action' value='create'>
  </form>
  <hr>
  <table border='1' width='70%' align='center'>
<?php
  $result = mysql_query('SELECT u.id,u.username,n.notice FROM notice_board AS n JOIN user AS u ON n.author_id=u.id');
  while($entry = mysql_fetch_row($result)) {
    echo '<tr><td>' . $entry[1] . ' [id=' . $entry[0] . ']</td><td>' . $entry[2] . '</td></tr>';
  }
?>
  </table>

<?php
}
?>

  </body>
</html>

<?php
  mysql_close($conn);
?>
