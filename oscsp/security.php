<?php

define( 'OSCSP_WEB_PAGE_TO_ROOT', '' );
require_once OSCSP_WEB_PAGE_TO_ROOT . 'oscsp/includes/oscspPage.inc.php';

oscspPageStartup( array( 'authenticated', 'phpids' ) );

$page = oscspPageNewGrab();
$page[ 'title' ]   = 'OSCSP Security' . $page[ 'title_separator' ].$page[ 'title' ];
$page[ 'page_id' ] = 'security';

$securityHtml = '';
if( isset( $_POST['seclev_submit'] ) ) {
	// Anti-CSRF
	checkToken( $_REQUEST[ 'user_token' ], $_SESSION[ 'session_token' ], 'security.php' );

	$securityLevel = '';
	switch( $_POST[ 'security' ] ) {
		case 'low':
			$securityLevel = 'low';
			break;

		default:
			$securityLevel = 'impossible';
			break;
	}

	oscspSecurityLevelSet( $securityLevel );
	oscspMessagePush( "Security level set to {$securityLevel}" );
	oscspPageReload();
}

if( isset( $_GET['phpids'] ) ) {
	switch( $_GET[ 'phpids' ] ) {
		case 'on':
			oscspPhpIdsEnabledSet( true );
			oscspMessagePush( "PHPIDS is now enabled" );
			break;
		case 'off':
			oscspPhpIdsEnabledSet( false );
			oscspMessagePush( "PHPIDS is now disabled" );
			break;
	}

	oscspPageReload();
}

$securityOptionsHtml = '';
$securityLevelHtml   = '';
foreach( array( 'low', 'impossible' ) as $securityLevel ) {
	$selected = '';
	if( $securityLevel == oscspSecurityLevelGet() ) {
		$selected = ' selected="selected"';
		$securityLevelHtml = "<p>Security level is currently: <em>$securityLevel</em>.<p>";
	}
	$securityOptionsHtml .= "<option value=\"{$securityLevel}\"{$selected}>" . ucfirst($securityLevel) . "</option>";
}

$phpIdsHtml = 'PHPIDS is currently: ';

// Able to write to the PHPIDS log file?
$WarningHtml = '';

if( oscspPhpIdsIsEnabled() ) {
	$phpIdsHtml .= '<em>enabled</em>. [<a href="?phpids=off">Disable PHPIDS</a>]';

	# Only check if PHPIDS is enabled
	if( !is_writable( $PHPIDSPath ) ) {
		$WarningHtml .= "<div class=\"warning\"><em>Cannot write to the PHPIDS log file</em>: ${PHPIDSPath}</div>";
	}
}
else {
	$phpIdsHtml .= '<em>disabled</em>. [<a href="?phpids=on">Enable PHPIDS</a>]';
}

// Anti-CSRF
generateSessionToken();

$page[ 'body' ] .= "
<div class=\"body_padded\">
	<h1>OSCSP Security <img src=\"" . OSCSP_WEB_PAGE_TO_ROOT . "oscsp/images/lock.png\" /></h1>
	<br />

	<h2>Security Level</h2>

	{$securityHtml}

	<form action=\"#\" method=\"POST\">
		{$securityLevelHtml}
		<p>You can set the security level to low  or impossible. The security level changes the vulnerability level of OSCSP:</p>
		<ol>
			<li> Low - This security level is completely vulnerable and <em>has no security measures at all</em>. It's use is to be as an example of how web application vulnerabilities manifest through bad 
			<li> Impossible - This level should be <em>secure against all vulnerabilities</em>. It is used to compare the vulnerable source code to the secure source code.<br />
				</li>
		</ol>
		<select name=\"security\">
			{$securityOptionsHtml}
		</select>
		<input type=\"submit\" value=\"Submit\" name=\"seclev_submit\">
		" . tokenField() . "
	</form>

	<br />
	<hr />
	<br />

	<h2>PHPIDS</h2>
	{$WarningHtml}
	<p>" . oscspExternalLinkUrlGet( 'https://github.com/PHPIDS/PHPIDS', 'PHPIDS' ) . " v" . oscspPhpIdsVersionGet() . " (PHP-Intrusion Detection System) is a security layer for PHP based web applications.</p>
	<p>PHPIDS works by filtering any user supplied input against a blacklist of potentially malicious code. It is used in OSCSP to serve as a live example of how Web Application Firewalls (WAFs) can help improve security and in some cases how WAFs can be circumvented.</p>
	<p>You can enable PHPIDS across this site for the duration of your session.</p>

	<p>{$phpIdsHtml}</p>
	[<a href=\"?test=%22><script>eval(window.name)</script>\">Simulate attack</a>] -
	[<a href=\"ids_log.php\">View IDS log</a>]
</div>";

oscspHtmlEcho( $page );

?>
