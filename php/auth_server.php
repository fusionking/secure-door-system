<?php

    
    //TO DO:12.4.2017 ****Decrypt the RSA encrypted password by parsing the POST body
    include 'RSA.php';

    $post = file_get_contents('php://input');
    echo($post);
    parse_str($post);
    $ciphertext = $password;

    $rsa = new Crypt_RSA();
    extract($rsa->createKey());

    $rsa->loadKey($publickey);
    echo($rsa->decrypt($ciphertext));
    
?>