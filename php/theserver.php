<?php 
/*
 *---------------------------------------------------------------
 * Pure php webserver
 *---------------------------------------------------------------
 *
 * This is the source code for the tutorial:
 */

include 'C:\wamp\www\MyAndroidProjects\secure_door\Auth Server\Server.php';
include 'C:\wamp\www\MyAndroidProjects\secure_door\Auth Server\Response.php';
include 'C:\wamp\www\MyAndroidProjects\secure_door\Auth Server\Request.php';

// we never need the first argument
array_shift( $argv );
// the next argument should be the port if not use 80
if ( empty( $argv ) )
{
	$port = 8080;
} else {
	$port = array_shift( $argv );
}
// create a new server instance
$server = new Server( '127.0.0.1', $port );
// start listening
$server->listen( function( Request $request ) 
{
	// print information that we recived the request
	echo $request->method() . ' ' . $request->uri() . "\n";
	
	// return a response containing the request information
	return new Response( '<pre>'.print_r( $request, true ).'</pre>' );
});