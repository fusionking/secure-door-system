<?php 

//echo "Hello CanAr";

if(isset($_POST['auth_request']))
{
    //echo "Auth request received!";
    $auth_request = $_POST['auth_request'];
    //echo "This is the auth request:".$auth_request;
    $auth_code = substr($auth_request,0,3);
    //$uid = substr($auth_request,3);

    if($auth_code == "101")
    {
        $challenge = openssl_random_pseudo_bytes(16);
        $hexChallenge   = bin2hex($challenge);
        //echo "Challenge sent by server: " . $hexChallenge;
        $response["challenge"] = $hexChallenge;
        echo json_encode($response);

    }

}

?>