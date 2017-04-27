login.php
<?php
require_once 'include/DB_Functions.php';
$db = new DB_Functions();
 
// json response array
$response = array("error" => FALSE);
 
if (isset($_POST['email']) && isset($_POST['password'])) {
 
    // receiving the post params
    $email = $_POST['email'];
    $password = $_POST['password'];
 
    //26.4.2017 get the concatanated Message+MAC
    $messageAndMac = $_POST['messageAndMac'];
    echo "Message received by server: " . $messageAndMac;

    // get the user by email and password
    $user = $db->getUserByEmailAndPassword($email, $password);
 
    if ($user != false) {
        // use is found
        echo "User found GEEVA";
        //$response["error"] = FALSE;
        $response["uid"] = $user["unique_id"];
        $response["user"]["name"] = $user["name"];
        $response["user"]["email"] = $user["email"];
        $response["user"]["created_at"] = $user["created_at"];
        $response["user"]["updated_at"] = $user["updated_at"];

        //26.4.2017 MAC Logic
        $macText = substr($messageAndMac,3);
        $messageText = substr($messageAndMac,0,3);
        echo "message text: " . $messageText;
        echo "user secret key: " . $user["secret_key"];
        $mac = hash_hmac('sha1', $messageText, $user["secret_key"]);

        echo "client side mac: " . $macText . " server side mac: " . $mac; 

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