<?php

define( 'OSCSP_WEB_PAGE_TO_ROOT', '../' );
require_once OSCSP_WEB_PAGE_TO_ROOT . 'oscsp/includes/oscspPage.inc.php';

oscspPageStartup( array( 'authenticated', 'phpids' ) );

$page = oscspPageNewGrab();
$page[ 'title' ] = 'Help' . $page[ 'title_separator' ].$page[ 'title' ];

$id       = $_GET[ 'id' ];
$security = $_GET[ 'security' ];

ob_start();
eval( '?>' . file_get_contents( OSCSP_WEB_PAGE_TO_ROOT . "vulnerabilities/{$id}/help/help.php" ) . '<?php ' );
$help = ob_get_contents();
ob_end_clean();

$page[ 'body' ] .= "
<div class=\"body_padded\">
	{$help}
</div>\n";

oscspHelpHtmlEcho( $page );

?>
