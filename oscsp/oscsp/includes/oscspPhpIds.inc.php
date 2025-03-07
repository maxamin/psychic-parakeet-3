<?php

if( !defined( 'OSCSP_WEB_PAGE_TO_ROOT' ) ) {
	define( 'OSCSP System error- WEB_PAGE_TO_ROOT undefined' );
	exit;
}

define( 'OSCSP_WEB_ROOT_TO_PHPIDS', 'external/phpids/' . oscspPhpIdsVersionGet() . '/' );
define( 'OSCSP_WEB_PAGE_TO_PHPIDS', OSCSP_WEB_PAGE_TO_ROOT . OSCSP_WEB_ROOT_TO_PHPIDS );

// Add PHPIDS to include path
set_include_path( get_include_path() . PATH_SEPARATOR . OSCSP_WEB_PAGE_TO_PHPIDS . 'lib/' );

require_once 'IDS/Init.php';

function oscspPhpIdsVersionGet() {
	return '0.6';
}

// PHPIDS Log parsing function
function oscspReadIdsLog() {
	$file_array = file( OSCSP_WEB_PAGE_TO_PHPIDS_LOG );

	$data = '';

	foreach( $file_array as $line_number => $line ) {
		$line = explode( ",", $line );
		$line = str_replace( "\"", " ", $line );

		$datetime      = $line[1];
		$vulnerability = $line[3];
		$variable      = urldecode($line[4]);
		$request       = urldecode($line[5]);
		$ip            = $line[6];
		$data .= "<div id=\"idslog\">\n<em>Date/Time:</em> {$datetime}<br />\n<em>Vulnerability:</em> {$vulnerability}<br />\n<em>Request:</em> " . htmlspecialchars($request) . "<br />\n<em>Variable:</em> " . htmlspecialchars($variable) . "<br />\n<em>IP:</em> {$ip}</div>";
	}

return $data;
}

// Clear PHPIDS log
function oscspClearIdsLog()	{
	if( isset( $_GET[ 'clear_log' ] ) ) {
		$fp = fopen( OSCSP_WEB_PAGE_TO_PHPIDS_LOG, w );
		fclose( $fp );
		oscspMessagePush( "PHPIDS log cleared" );
		oscspPageReload();
	}
}

// Main PHPIDS function
function oscspPhpIdsTrap() {
	global $_OSCSP;
	try {

		/*
		* 1. Define what to scan
		* Please keep in mind what array_merge does and how this might interfer
		* with your variables_order settings
		*/
		$request = array(
			'REQUEST' => $_REQUEST,
			'GET'     => $_GET,
			'POST'    => $_POST,
			'COOKIE'  => $_COOKIE
		);

		$init = IDS_Init::init( OSCSP_WEB_PAGE_TO_PHPIDS . 'lib/IDS/Config/Config.ini' );

		$init->config[ 'General' ][ 'base_path' ] = OSCSP_WEB_PAGE_TO_PHPIDS . 'lib/IDS/';
		$init->config[ 'General' ][ 'use_base_path' ] = true;
		$init->config[ 'Caching' ][ 'caching' ] = 'none';

		// 2. Initiate the PHPIDS and fetch the results
		$ids = new IDS_Monitor( $request, $init );
		$result = $ids->run();

		if( !$result->isEmpty() ) {
			require_once 'IDS/Log/File.php';
			require_once 'IDS/Log/Composite.php';

			$compositeLog = new IDS_Log_Composite();
			$compositeLog->addLogger(IDS_Log_File::getInstance($init));

			$compositeLog->execute($result);

			echo 'Hacking attempt detected and logged.<br />Have a nice day.';

			if( $_OSCSP[ 'default_phpids_verbose' ] == 'true' )
				echo $result;

			exit;
		}
	}
	catch (Exception $e) {
		// Something went terribly wrong - maybe the filter rules weren't found?
		printf( 'An error occured: %s', $e->getMessage() );
	}
}

?>
