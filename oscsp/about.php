<?php

define( 'OSCSP_WEB_PAGE_TO_ROOT', '' );
require_once OSCSP_WEB_PAGE_TO_ROOT . 'oscsp/includes/oscspPage.inc.php';

oscspPageStartup( array( 'phpids' ) );

$page = oscspPageNewGrab();
$page[ 'title' ]   = 'About' . $page[ 'title_separator' ].$page[ 'title' ];
$page[ 'page_id' ] = 'about';

$page[ 'body' ] .= "
<div class=\"body_padded\">
	<h2>About</h2>
	<p>Open Source Code Security Project(OSCSP) is a PHP/MySQL web application that is damn vulnerable. Its main goals are to be an aid for security professionals to test their skills and tools in a legal environment, help web developers better understand the processes of securing web applications and aid teachers/students to teach/learn web application security in a class room environment</p>
	<p>The official documentation for OSCSP can be found <a href=\"docs/oscsp_draft_report.pdf\">here</a>.</p>

	

	<ul>
		<li>PHPIDS - Copyright (c) 2007 " . oscspExternalLinkUrlGet( 'http://github.com/PHPIDS/PHPIDS', 'PHPIDS group' ) . "</li>
	</ul>

	
	<p>The PHPIDS library is included, in good faith, with this OSCSP distribution. The operation of PHPIDS is provided without support from the OSCSP team.</p>

	<h2>Development</h2>
	<p>Everyone is welcome to contribute and help make OSCSP as successful as it can be. All contributors can have their name and link (if they wish) placed in the credits section. To contribute pick an Issue from the Project Home to work on or submit a patch to the Issues list.</p>
</div>\n";

oscspHtmlEcho( $page );

exit;

?>
