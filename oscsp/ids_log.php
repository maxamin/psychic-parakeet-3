<?php

define( 'OSCSP_WEB_PAGE_TO_ROOT', '' );
require_once OSCSP_WEB_PAGE_TO_ROOT . 'oscsp/includes/oscspPage.inc.php';

define( 'OSCSP_WEB_ROOT_TO_PHPIDS_LOG', 'external/phpids/' . oscspPhpIdsVersionGet() . '/lib/IDS/tmp/phpids_log.txt' );
define( 'OSCSP_WEB_PAGE_TO_PHPIDS_LOG', OSCSP_WEB_PAGE_TO_ROOT.OSCSP_WEB_ROOT_TO_PHPIDS_LOG );

oscspPageStartup( array( 'authenticated', 'phpids' ) );

$page = oscspPageNewGrab();
$page[ 'title' ]   = 'PHPIDS Log' . $page[ 'title_separator' ].$page[ 'title' ];
$page[ 'page_id' ] = 'log';
// $page[ 'clear_log' ]; <- Was showing error.

$page[ 'body' ] .= "
<div class=\"body_padded\">
	<h1>PHPIDS Log</h1>

	<p>" . oscspReadIdsLog() . "</p>
	<br /><br />

	<form action=\"#\" method=\"GET\">
		<input type=\"submit\" value=\"Clear Log\" name=\"clear_log\">
	</form>

	" . oscspClearIdsLog() . "
</div>";

oscspHtmlEcho( $page );

?>
