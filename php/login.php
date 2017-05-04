login.php
<?php
require_once 'include/DB_Functions.php';
$db = new DB_Functions();
 
// json response array
$response = array("error" => FALSE);


//authentication request response
/*if(isset($_POST['auth']))
{
    echo "Auth request received!";
    $auth_request = $_POST["auth_request"];
    echo "This is the auth request:".$auth_request;
    $auth_code = substr($auth_request,0,3);
    $uid = substr($auth_request,3);

    if($auth_code == "101")
    {
        $challenge = random_bytes(16);
        echo "Challenge sent by server: " . $challenge;
        $response["challenge"] = $challenge;
        echo json_encode($response);

    }

}*/
 
if (isset($_POST['email']) && isset($_POST['password']) && isset($_POST['messageAndMac'])) {
 
    // receiving the post params
    $email = $_POST['email'];
    $password = $_POST['password'];
 
    //26.4.2017 get the concatanated Message+MAC
    $messageAndMac = $_POST['messageAndMac'];
    echo "Message received by server: " . $messageAndMac ."\n";

    // get the user by email and password
    $user = $db->getUserByEmailAndPassword($email, $password);
 
    if ($user != false) {
        // use is found
        //$response["error"] = FALSE;
        $response["uid"] = $user["unique_id"];
        $response["user"]["name"] = $user["name"];
        $response["user"]["email"] = $user["email"];
        $response["user"]["created_at"] = $user["created_at"];
        $response["user"]["updated_at"] = $user["updated_at"];

        //26.4.2017 MAC Logic
        $macText = substr($messageAndMac,32);
        $messageText = substr($messageAndMac,0,32);
        echo "challenge text: " . $messageText ."\n";
        echo "user secret key: " . $user["secret_key"] ."\n";

        $concat = $_POST['password'] . $user["secret_key"];
        $keyForMac = md5($concat);
        $mac = hash_hmac('sha1', $messageText, $keyForMac);

        echo "client side mac: " . $macText . " server side mac: " . $mac ."\n"; 

        if($mac == $macText)
        {
        	$response["error"] = FALSE;
        	$response["error_msg"] = "User is authenticated and message is integrity-checked! Success!";
        	echo json_encode($response);
        }
        else
        {
        	$response["error"] = TRUE;
        	echo json_encode($response);
        }

        
    } else {
        // user is not found with the credentials
        $response["error"] = TRUE;
        $response["error_msg"] = "Login credentials are wrong. Please try again!";
        echo json_encode($response);
    }
} else {
    // required post params is missing
    $response["error"] = TRUE;
    $response["error_msg"] = "Required parameters email or password is missing!";
    echo json_encode($response);
}
?>