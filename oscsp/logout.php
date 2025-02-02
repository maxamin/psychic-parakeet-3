<?php

define( 'OSCSP_WEB_PAGE_TO_ROOT', '' );
require_once OSCSP_WEB_PAGE_TO_ROOT . 'oscsp/includes/oscspPage.inc.php';

oscspPageStartup( array( 'phpids' ) );

if( !oscspIsLoggedIn() ) {	// The user shouldn't even be on this page
	// oscspMessagePush( "You were not logged in" );
	oscspRedirect( 'login.php' );
}

oscspLogout();
oscspMessagePush( "You have logged out" );
oscspRedirect( 'login.php' );

?>
