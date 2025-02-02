<?php

define( 'OSCSP_WEB_PAGE_TO_ROOT', '' );
require_once OSCSP_WEB_PAGE_TO_ROOT . 'oscsp/includes/oscspPage.inc.php';

oscspPageStartup( array( 'phpids' ) );

$page = oscspPageNewGrab();
$page[ 'title' ]   = 'Setup' . $page[ 'title_separator' ].$page[ 'title' ];
$page[ 'page_id' ] = 'setup';

if( isset( $_POST[ 'create_db' ] ) ) {
	// Anti-CSRF
	checkToken( $_REQUEST[ 'user_token' ], $_SESSION[ 'session_token' ], 'setup.php' );

	if( $DBMS == 'MySQL' ) {
		include_once OSCSP_WEB_PAGE_TO_ROOT . 'oscsp/includes/DBMS/MySQL.php';
	}
	elseif($DBMS == 'PGSQL') {
		// include_once OSCSP_WEB_PAGE_TO_ROOT . 'oscsp/includes/DBMS/PGSQL.php';
		oscspMessagePush( 'PostgreSQL is not yet fully supported.' );
		oscspPageReload();
	}
	else {
		oscspMessagePush( 'ERROR: Invalid database selected. Please review the config file syntax.' );
		oscspPageReload();
	}
}

// Anti-CSRF
generateSessionToken();

$page[ 'body' ] .= "
<div class=\"body_padded\">
	<h1>Database Setup <img src=\"" . OSCSP_WEB_PAGE_TO_ROOT . "oscsp/images/spanner.png\" /></h1>

	<hr />

	<!-- Create db button -->
	<form action=\"#\" method=\"post\">
		<input name=\"create_db\" type=\"submit\" value=\"Create / Reset Database\">
		" . tokenField() . "
	</form>
	<br />
	<hr />
</div>";

oscspHtmlEcho( $page );

?>
