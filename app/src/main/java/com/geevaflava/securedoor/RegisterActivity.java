package com.geevaflava.securedoor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import helper.SQLLiteHandler;
import helper.SessionManager;

import static javax.crypto.Cipher.ENCRYPT_MODE;
import static javax.crypto.Cipher.getInstance;

public class RegisterActivity extends Activity {
    private static final String TAG = RegisterActivity.class.getSimpleName();

    /**25.4.2017 IV AND SECRET KEY VALUES**/
    private static String iv = "fedcba9876543210";
    private static final IvParameterSpec FIXED_IV = new IvParameterSpec(iv.getBytes());
    private static String SecretKey = "0123456789abcdef";
    private static final SecretKeySpec keyspec = new SecretKeySpec(SecretKey.getBytes(), "AES");

    private Button btnRegister;
    private Button btnLinkToLogin;
    private EditText inputFullName;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLLiteHandler db;

    private String secret_key;
    private String password;
    private String email;
    private String name;

    private static final String AndroidKeyStore = "AndroidKeyStore";
    private static final String AES_MODE = "AES/GCM/NoPadding";
    private static String KEY_ALIAS = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputFullName = (EditText) findViewById(R.id.name);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);


        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLLiteHandler(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(RegisterActivity.this,
                    HomeActivity.class);
            startActivity(intent);
            finish();
        }

        // Register Button Click event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                name = inputFullName.getText().toString().trim();
                email = inputEmail.getText().toString().trim();
                password = inputPassword.getText().toString().trim();

                //25.4.2017 Calculate client salt
                try {
                    secret_key = encryptedKey(password);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (CertificateException e) {
                    e.printStackTrace();
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                } catch (InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                } catch (NoSuchProviderException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                    registerUser(name, email, password,secret_key);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter your details!", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        // Link to Login Screen
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

    }

    /**
     * Function to store user in MySQL database will post params(tag, name,
     * email, password) to register url
     * */
    private void registerUser(final String name, final String email,
                              final String password, final String secret_key) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        pDialog.setMessage("Registering ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // User successfully stored in MySQL
                        // Now store the user in sqlite
                        String uid = jObj.getString("uid");

                        JSONObject user = jObj.getJSONObject("user");
                        String name = user.getString("name");
                        String email = user.getString("email");
                        String created_at = user
                                .getString("created_at");

                        //25.4.2017
                        String secret_key = user.getString("secret_key");

                        // Inserting row in users table
                        db.addUser(name, email, uid, created_at,secret_key);

                        Toast.makeText(getApplicationContext(), "User successfully registered. Try login now!", Toast.LENGTH_LONG).show();

                        // Launch login activity
                        Intent intent = new Intent(
                                RegisterActivity.this,
                                LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", name);
                params.put("email", email);
                params.put("password", password);
                params.put("secret_key",secret_key);

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


    private String calculateSalt(String password) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, IOException, CertificateException, KeyStoreException, InvalidAlgorithmParameterException {


        int pswdIterations = 65536  ;
        int keySize = 256;
        byte[] ivBytes;
        byte[] saltBytes = {0,1,2,3,4,5,6};

        SecretKeyFactory factory = SecretKeyFactory.getInstance("AES");

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                saltBytes,
                pswdIterations,
                keySize
        );

        SecretKey secretKey = factory.generateSecret(spec);

        Key key = new SecretKeySpec(secretKey.getEncoded(), "AES");

        return key.getEncoded().toString();


    }

    /**25.4.2017 SECURE KEY GENERATION*/
    private Key generateKey() throws Exception {
        Key key = new SecretKeySpec(SecretKey.getBytes(), "AES");
        return key;
    }

    public String encryptedKey(String user_pass) throws Exception {

        Key key = generateKey();
        Cipher c = getInstance("AES");
        c.init(ENCRYPT_MODE,key);
        byte[] encVal = c.doFinal(user_pass.getBytes());
        String encryptedValue = encVal.toString();
        return encryptedValue;
    }
}
