package com.geevaflava.securedoor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import helper.SQLLiteHandler;
import helper.SessionManager;


public class LoginActivity extends Activity {

    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnLogin;
    private Button btnLinkToRegister;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLLiteHandler db;

    private static char[] PASSWORD = "".toCharArray();
    private static String user_password = "";
    private static String email = "";

    /**26.4.2017 HMAC VARIABLES****/
    private String secret_key = "";
    private byte[] message;
    private String messageText;
    private String macText;
    private String MAC_AND_MESSAGE_TEXT = "";

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private String PREF_KEY_NAME = "";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite database handler
        db = new SQLLiteHandler(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }

        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                email = inputEmail.getText().toString().trim();
                user_password = inputPassword.getText().toString().trim();

                /**Access SharedPreferences to get the secret client key 26.4.2017*/
                PREF_KEY_NAME = email;
                SharedPreferences sp = getSharedPreferences("your_prefs", Activity.MODE_PRIVATE);
                secret_key = sp.getString(PREF_KEY_NAME,"");
                System.out.println("Client side secret key: " + secret_key);

                // Check for empty data in the form
                if (!email.isEmpty() && !user_password.isEmpty()) {
                    // login user

                    //***TO DO 12.4.2017: HTTPS connection with server logic + encryption process of the password****////
                     message = calculateMessage();
                     messageText = new String(message);
                     System.out.println("Message string on client" + messageText);
                    try {
                        macText = calculateRFC2104HMAC(messageText,secret_key);
                    } catch (SignatureException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    }

                    MAC_AND_MESSAGE_TEXT = messageText+macText;
                    System.out.println("Message concatanated " + MAC_AND_MESSAGE_TEXT);
                    //************************************//
                    checkLogin(email, user_password);
                } else {
                    // Prompt user to enter credentials
                    Toast.makeText(getApplicationContext(),
                            "Please enter the credentials!", Toast.LENGTH_LONG)
                            .show();
                }
            }

        });

        // Link to Register Screen
        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        RegisterActivity.class);
                startActivity(i);
                finish();
            }
        });

    }


    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();

        for (byte b : bytes) {
            formatter.format("%02x", b);
        }

        return formatter.toString();
    }

    public static String calculateRFC2104HMAC(String data, String key)
            throws SignatureException, NoSuchAlgorithmException, InvalidKeyException
    {
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);
        System.out.println("Hex string mac on client: " + toHexString(mac.doFinal(data.getBytes())));
        String mcT = new String(mac.doFinal(data.getBytes()));
        System.out.print("Pure String mac on client" + mcT);
        return toHexString(mac.doFinal(data.getBytes()));
    }

    private byte[] calculateMessage()
    {
        /*ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] encryptedData = new byte[100];
        byte[] b = baos.toByteArray();
        byte[] keyStart = user_password.getBytes();
        KeyGenerator kgen = null;
        try {
            kgen = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        SecureRandom sr = null;
        try {
            sr = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        sr.setSeed(keyStart);
        kgen.init(128, sr); // 192 and 256 bits may not be available
        SecretKey skey = kgen.generateKey();
        byte[] key = skey.getEncoded();

        try {
            encryptedData = encrypt(key,b);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedData;
        */
        String randomMessage = "101";
        byte[] rnd = randomMessage.getBytes();
        return rnd;
    }
    /**
     * function to verify login details in mysql db
     * */
    private void checkLogin(final String email, final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Logging in ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response);
                hideDialog();

                //try {
                    //JSONObject jObj = new JSONObject(response);
                    //boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    boolean error = false;
                    if (!error) {
                        // user successfully logged in
                        // Create login session
                        session.setLogin(true);

                        // Now store the user in SQLite
                       /* String uid = jObj.getString("uid");

                        JSONObject user = jObj.getJSONObject("user");
                        String name = user.getString("name");
                        String email = user.getString("email");
                        String created_at = user
                                .getString("created_at");

                        // Inserting row in users table
                        db.addUser(name, email, uid, created_at,secret_key);*/

                        // Launch main activity
                        Intent intent = new Intent(LoginActivity.this,
                                HomeActivity.class );
                        intent.putExtra("user email", email);
                        startActivity(intent);
                        finish();
                    } else {
                        // Error in login. Get the error message
                        //String errorMsg = jObj.getString("error_msg");
                        //Toast.makeText(getApplicationContext(),
                                //errorMsg, Toast.LENGTH_LONG).show();
                    }
                //} catch (JSONException e) {
                    // JSON error
                  //  e.printStackTrace();
                   // Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                //}

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("password", password);
                params.put("messageAndMac",MAC_AND_MESSAGE_TEXT);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    /***TO DO:12.4.2017****/
    /*private static String encrypt(String property) throws GeneralSecurityException, UnsupportedEncodingException {
        PASSWORD = user_password.toCharArray();
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PASSWORD));
        Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
        pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
        return base64Encode(pbeCipher.doFinal(property.getBytes("UTF-8")));
    }*/
    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }
}