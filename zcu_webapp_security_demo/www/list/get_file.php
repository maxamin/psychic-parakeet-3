<?php

$filePath = $_GET['file'];

echo 'Content of file ' . $filePath . ':<br/>';
echo '<pre>';
echo shell_exec('cat ' . $filePath);
echo '</pre>';
