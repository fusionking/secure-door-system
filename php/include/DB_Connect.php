<?php
class DB_Connect {
    private $conn;
 
    // Connecting to database
    public function connect() {
        require_once 'include/Config.php';
         
        // Connecting to mysql database
         $this->conn = new mysqli('localhost', 'root', '', 'secure_door');
         
        // return database handler
        return $this->conn;
    }
}
 
?>