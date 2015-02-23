/**
 * Author: Ravi Tamada
 * URL: www.androidhive.info
 * twitter: http://twitter.com/ravitamada
 * */
package unimelb.cis.spatialanalytics.fuelpriceshare.login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import unimelb.cis.spatialanalytics.fuelpriceshare.MainActivity;
import unimelb.cis.spatialanalytics.fuelpriceshare.R;
import unimelb.cis.spatialanalytics.fuelpriceshare.config.ConfigConstant;
import unimelb.cis.spatialanalytics.fuelpriceshare.config.ConfigURL;
import unimelb.cis.spatialanalytics.fuelpriceshare.data.UserCookie;
import unimelb.cis.spatialanalytics.fuelpriceshare.data.Users;
import unimelb.cis.spatialanalytics.fuelpriceshare.http.AppController;
import unimelb.cis.spatialanalytics.fuelpriceshare.http.CustomRequest;
import unimelb.cis.spatialanalytics.fuelpriceshare.http.MyExceptionHandler;

/**
 * Register user into our system. Currently only require phone number and password for input
 */

public class RegisterActivity extends Activity {

    /**
     * UI component definition
     */
    private Button btnRegister;
    private Button btnLinkToLogin;
    private EditText inputPhone;
    private EditText inputPassword;
    private TextView registerErrorMsg;


    private final String KEY_ACTION = "Register";//Define the action for http request (servlet)

    /**
     * User basic information
     */
    private String phone;
    private String password;


    /**
     * Local User Storage : Session
     */
    private SharedPreferences pref;

    /**
     * For Log
     */

    private final String TAG = "RegisterActivity";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Importing all assets like buttons, text fields
        inputPhone = (EditText) findViewById(R.id.registerPhone);
        inputPassword = (EditText) findViewById(R.id.registerPassword);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);
        registerErrorMsg = (TextView) findViewById(R.id.register_error);
        registerErrorMsg.setText("");

        /**
         * Initialize pref
         */
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        UserCookie.clearUserInfo(pref, this);


        // Register Button Click event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                phone = inputPhone.getText().toString();
                password = inputPassword.getText().toString();

                /**
                 * Handle input errors. May add more if more requirements are defined such as the length of password etc.
                 */
                if (phone.equals("")) {
                    registerErrorMsg.setText("Phone can't be empty");
                    return;
                }
                if (password.equals("")) {
                    registerErrorMsg.setText("Password can't be empty");
                    return;
                }

                Log.d(TAG, "Register");


                /**
                 * User Volley API developed by Google to handle the request!
                 */

                // Tag used to cancel the request
                String tag_json_obj = TAG;

                Map<String, String> params = new HashMap<String, String>();
                params.put("Action", KEY_ACTION);
                params.put("phone", phone);
                Users.id = phone;
                Users.password = password;
                JSONObject json = Users.getUserJSON();
                params.put("json", json.toString());


                CustomRequest customRequest = new CustomRequest(Request.Method.POST, ConfigURL.getRegisterURL(), params, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());

                        handleResponse(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        MyExceptionHandler.presentError(TAG, "Register failed", getApplicationContext(), error);
                    }
                });
                // Adding request to request queue
                AppController.getInstance().addToRequestQueue(customRequest, tag_json_obj);


            }
        });

        // Link to Login Screen
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(i);
                // Close Registration View
                finish();
            }
        });
    }


    /**
     * Handle the response from the server
     *
     * @param response response from the server
     */

    public void handleResponse(JSONObject response) {
        if (!response.has(ConfigConstant.KEY_ERROR)) {


            Intent dashboard = new Intent(getApplicationContext(), MainActivity.class);
            // Close all views before launching Dashboard
            startActivity(dashboard);
            /**
             * Mapping some basic information and store the user locally
             */
            Users.phone = phone;
            Users.id = phone;
            UserCookie.storeUserLocal(pref);
            finish();

        } else {

            registerErrorMsg.setText("User name has been taken, please try another one.");
            Log.e(TAG, "Fail in registration:" + response.toString());


        }
    }


}