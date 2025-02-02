<?php

require('www/db.php');
$sql = file_get_contents('www/setup/init_db.sql');
pg_query($sql);
