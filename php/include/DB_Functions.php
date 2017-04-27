+<?php
 
/**
 * @author Ravi Tamada
 * @link http://www.androidhive.info/2012/01/android-login-and-registration-with-php-mysql-and-sqlite/ Complete tutorial
 */
 
class DB_Functions {
 
    private $conn;
 
    // constructor
    function __construct() {
        require_once 'DB_Connect.php';
        // connecting to database
        $db = new Db_Connect();
        $this->conn = $db->connect();
    }
 
    // destructor
    function __destruct() {
         
    }
 
    /**
     * Storing new user
     * returns user details
     */
    public function storeUser($name, $email, $password,$secret_key) {
        $uuid = uniqid('', true);
        //$secretKey = $name + $password;  //Secret key shared between the client and the server
        //$uuid = hash_hmac('md5', $input, $secretKey);

        $hash = $this->bcrypt($password);
        $encrypted_password = $hash["encrypted"]; // encrypted password
        $salt = $hash["salt"]; // salt
 
        $stmt = $this->conn->prepare("INSERT INTO users(unique_id, name, email, encrypted_password, salt, created_at,secret_key) VALUES(?, ?, ?, ?, ?, NOW(),?)");
        $stmt->bind_param("ssssss", $uuid, $name, $email, $encrypted_password, $salt,$secret_key);
        $result = $stmt->execute();
        $stmt->close();
 
        // check for successful store
        if ($result) {
            $stmt = $this->conn->prepare("SELECT * FROM users WHERE email = ?");
            $stmt->bind_param("s", $email);
            $stmt->execute();
            $user = $stmt->get_result()->fetch_assoc();
            $stmt->close();
 
            return $user;
        } else {
            return false;
        }
    }
 
    /**
     * Get user by email and password
     */
    public function getUserByEmailAndPassword($email, $password) {
 
        $stmt = $this->conn->prepare("SELECT * FROM users WHERE email = ?");
 
        $stmt->bind_param("s", $email);
 
        if ($stmt->execute()) {
            $user = $stmt->get_result()->fetch_assoc();
            $stmt->close();
 
            // verifying user password
            $user_salt = $user["salt"];
            //$encrypted_password = verifyPassword($password,$user_salt);
             $i = 22;
        $crypto_strong = true;
        
        $options = [
           'cost' => 11,
           'salt' => $user_salt,
                   ];
        $encrypted = password_hash($password, PASSWORD_BCRYPT, $options);
        $hash = array("salt" => $user_salt, "encrypted" => $encrypted);
        
        $encrypted_password = $hash["encrypted"];
        

            $user_password = $user["encrypted_password"];
            // check for password equality
            if ($encrypted_password == $user_password) {
                // user authentication details are correct
                echo "Successful login! Geeva Flava!";
                return $user;
            }
        } else {
            return NULL;
        }
    }
 
    /**
     * Check user is existed or not
     */
    public function isUserExisted($email) {
        $stmt = $this->conn->prepare("SELECT email from users WHERE email = ?");
 
        $stmt->bind_param("s", $email);
 
        $stmt->execute();
 
        $stmt->store_result();
 
        if ($stmt->num_rows > 0) {
            // user existed 
            $stmt->close();
            return true;
        } else {
            // user not existed
            $stmt->close();
            return false;
        }
    } 
 
    //TO DO:12.4.2017
    public function bcrypt($password){

        $i = 22;
        $crypto_strong = true;
        $salt = openssl_random_pseudo_bytes($i, $crypto_strong);
        $options = [
           'cost' => 11,
           'salt' => $salt,
                   ];
        $encrypted = password_hash($password, PASSWORD_BCRYPT, $options);
        $hash = array("salt" => $salt, "encrypted" => $encrypted);
        echo "The computed salt value by bcrypt " + $salt;
        return $hash;
    }
    
    //TO DO:12.4.2017
    public function verifyPassword($password,$user_salt){
        
        $i = 22;
        $crypto_strong = true;
        $salt = $user["salt"];
        $options = [
           'cost' => 11,
           'salt' => $salt,
                   ];
        $encrypted = password_hash($password, PASSWORD_BCRYPT, $options);
        $hash = array("salt" => $salt, "encrypted" => $encrypted);
        
        $encrypted_password = $hash["encrypted"];
        
        return $encrypted_password;
    }



    /**
     * Encrypting password
     * @param password
     * returns salt and encrypted password
     */
    public function hashSSHA($password) {
 
        $salt = sha1(rand());
        $salt = substr($salt, 0, 10);
        $encrypted = base64_encode(sha1($password . $salt, true) . $salt);
        $hash = array("salt" => $salt, "encrypted" => $encrypted);
        return $hash;
    }
 
    /**
     * Decrypting password
     * @param salt, password
     * returns hash string
     */
    public function checkhashSSHA($salt, $password) {
 
        $hash = base64_encode(sha1($password . $salt, true) . $salt);
 
        return $hash;
    }
 
}
 
?>