<?php

# If you are having problems connecting to the MySQL database and all of the variables below are correct
# try changing the 'db_server' variable from localhost to 127.0.0.1. Fixes a problem due to sockets.
#   Thanks to @digininja for the fix.

# Database management system to use
$DBMS = 'MySQL';
#$DBMS = 'PGSQL'; // Currently disabled

# Database variables
#   WARNING: The database specified under db_database WILL BE ENTIRELY DELETED during setup.
#   Please use a database dedicated to OSCSP.
#
# If you are using MariaDB then you cannot use root, you must use create a dedicated OSCSP user.
#   See README.md for more information on this.
$_OSCSP = array();
$_OSCSP[ 'db_server' ]   = '127.0.0.1';
$_OSCSP[ 'db_database' ] = 'oscsp';
$_OSCSP[ 'db_user' ]     = 'root';
$_OSCSP[ 'db_password' ] = '';

# Only used with PostgreSQL/PGSQL database selection.
$_OSCSP[ 'db_port '] = '5432';

# ReCAPTCHA settings
#   Used for the 'Insecure CAPTCHA' module
#   You'll need to generate your own keys at: https://www.google.com/recaptcha/admin
$_OSCSP[ 'recaptcha_public_key' ]  = '';
$_OSCSP[ 'recaptcha_private_key' ] = '';

# Default security level
#   Default value for the secuirty level with each session.
#   The default is 'impossible'. You may wish to set this to either 'low', 'medium', 'high' or impossible'.
$_OSCSP[ 'default_security_level' ] = 'impossible';

# Default PHPIDS status
#   PHPIDS status with each session.
#   The default is 'disabled'. You can set this to be either 'enabled' or 'disabled'.
$_OSCSP[ 'default_phpids_level' ] = 'disabled';

# Verbose PHPIDS messages
#   Enabling this will show why the WAF blocked the request on the blocked request.
#   The default is 'disabled'. You can set this to be either 'true' or 'false'.
$_OSCSP[ 'default_phpids_verbose' ] = 'false';

?>
