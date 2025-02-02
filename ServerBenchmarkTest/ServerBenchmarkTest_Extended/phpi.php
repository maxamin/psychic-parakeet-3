 <?php

parse_str(implode('&', array_slice($argv, 1)), $_GET);
$input = htmlspecialchars($_GET['phpi']);
include($input);

?>
