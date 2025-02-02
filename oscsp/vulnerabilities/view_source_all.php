<?php

define( 'OSCSP_WEB_PAGE_TO_ROOT', '../' );
require_once OSCSP_WEB_PAGE_TO_ROOT . 'oscsp/includes/oscspPage.inc.php';

oscspPageStartup( array( 'authenticated', 'phpids' ) );

$page = oscspPageNewGrab();
$page[ 'title' ] = 'Source' . $page[ 'title_separator' ].$page[ 'title' ];

$id = $_GET[ 'id' ];

$lowsrc = @file_get_contents("./{$id}/source/low.php");
$lowsrc = str_replace( array( '$html .=' ), array( 'echo' ), $lowsrc);
$lowsrc = highlight_string( $lowsrc, true );


$impsrc = @file_get_contents("./{$id}/source/impossible.php");
$impsrc = str_replace( array( '$html .=' ), array( 'echo' ), $impsrc);
$impsrc = highlight_string( $impsrc, true );

switch ($id) {
	case "javascript" :
		$vuln = 'JavaScript';
		break;

	case "fi" :
		$vuln = 'File Inclusion';
		break;
	case "brute" :
		$vuln = 'Brute Force';
		break;
	case "csrf" :
		$vuln = 'CSRF';
		break;
	case "exec" :
		$vuln = 'Command Injection';
		break;
	case "sqli" :
		$vuln = 'SQL Injection';
		break;
	case "sqli_blind" :
		$vuln = 'SQL Injection (Blind)';
		break;
	case "upload" :
		$vuln = 'File Upload';
		break;
	case "xss_r" :
		$vuln = 'Reflected XSS';
		break;
	case "xss_s" :
		$vuln = 'Stored XSS';
		break;
	case "weak_id" :
		$vuln = 'Weak Session IDs';
		break;
	default:
		$vuln = "Unknown Vulnerability";
}

$page[ 'body' ] .= "
<div class=\"body_padded\">
	<h1>{$vuln}</h1>
	<br />

	<h3>Impossible {$vuln} Source</h3>
	<table width='100%' bgcolor='white' style=\"border:2px #C0C0C0 solid\">
		<tr>
			<td><div id=\"code\">{$impsrc}</div></td>
		</tr>
	</table>
	<br />

	

	<h3>Low {$vuln} Source</h3>
	<table width='100%' bgcolor='white' style=\"border:2px #C0C0C0 solid\">
		<tr>
			<td><div id=\"code\">{$lowsrc}</div></td>
		</tr>
	</table>
	<br /> <br />

	<form>
		<input type=\"button\" value=\"<-- Back\" onclick=\"history.go(-1);return true;\">
	</form>

</div>\n";

oscspSourceHtmlEcho( $page );

?>
