package unimelb.cis.spatialanalytics.fuelpriceshare.login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import com.android.volley.toolbox.ImageLoader;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.LoginButton.UserInfoChangedCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import unimelb.cis.spatialanalytics.fuelpriceshare.MainActivity;
import unimelb.cis.spatialanalytics.fuelpriceshare.R;
import unimelb.cis.spatialanalytics.fuelpriceshare.config.ConfigConstant;
import unimelb.cis.spatialanalytics.fuelpriceshare.config.ConfigURL;
import unimelb.cis.spatialanalytics.fuelpriceshare.data.UserCookie;
import unimelb.cis.spatialanalytics.fuelpriceshare.data.Users;
import unimelb.cis.spatialanalytics.fuelpriceshare.help.RandomGenerateUniqueIDs;
import unimelb.cis.spatialanalytics.fuelpriceshare.http.AppController;
import unimelb.cis.spatialanalytics.fuelpriceshare.http.CustomRequest;
import unimelb.cis.spatialanalytics.fuelpriceshare.http.ImageUploader;

/**
 * Created by hanl4 on 15/02/2015.
 * This function is used to do login. There are THREE available ways for logging into our system:
 * 1) Register in our system and then log in;
 * 2) use Facebook account. Please notice that, all the public information of FB account are stored into our own database
 * 3) session login. If the user has already logged into our system before, the system will store the informatino locally.
 * Therefore, the user doesn't have to input the username and password to login again. However, if the user press the "LOG OUT"
 * button, the session will be erased from the local, and it will require input password and username again when the user is
 * opening the system.
 */

public class LoginActivity extends Activity implements ImageUploader.ImageUploaderReply {

    /**
     * UI component definition
     */
    private Button btnLogin;
    private Button btnLinkToRegister;
    private EditText inputUsername;
    private EditText inputPassword;
    private TextView loginErrorMsg;
    private SharedPreferences pref;


    /**
     * User name and password
     */
    private String username;
    private String password;

    /**
     * Facebook login
     */
    private LoginButton loginBtn;
    private TextView usernameTextView;
    private UiLifecycleHelper uiHelper;


    /**
     * For HTTP Return Request Code (switch case)
     */

    private final int REQUEST_CODE_COUCHDB_FB_LOGIN = 1;
    private final int REQUEST_CODE_COUCHDB_UPDATE_USER_INFO = 2;

    private final int REQUEST_CODE_IMAGE_LOADER = 1;
    private final int REQUEST_CODE_IMAGE_UPLOADER = 1;
    private final int REQUEST_CODE_HTTP_URL = 1;

    /**
     * HTTP Request Interface to receive the returned results
     */
    private ImageUploader.ImageUploaderReply imageUploaderReply = this;

    /**
     * For Log
     */
    private final String TAG = "LoginActivity";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        /**
         * First check user session. if user already logged in before, the system will automatically
         * log him in without password and username, and mapping user information to Users.
         * */
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (UserCookie.isUserLoggedInBefore(pref)) {
            Users.mapSharedPreference(pref);

            //Start the main activity
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);

            /**
             * User Volley API developed by Google to handle the request!
             * Update the local session information to make it updated
             * This operation might not be necessary though!
             * Can be removed!
             */

            // Tag used to cancel the request
            String tag_json_obj = TAG + " : Fetch User Doc";

            Map<String, String> params = new HashMap<String, String>();
            params.put(ConfigConstant.KEY_COUCHDB_DOC_ACTION, "GET");
            params.put(ConfigConstant.KEY_COUCHDB_DOC_ID, Users.id);
            CustomRequest customRequest = new CustomRequest(ConfigURL.getCouchDBURL(), params, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, response.toString());

                    //Update User information from the server to make sure that, the local stored user info is latest.
                    if (!response.has(ConfigConstant.KEY_ERROR)) {
                        Users.mapJson(response);
                        UserCookie.storeUserLocal(pref, response);
                        UserCookie.setLoginStatus(pref, true);
                    } else
                        Log.e(TAG, "failed to update the local session from the server");

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Error: " + error.getMessage());
                }
            });
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(customRequest, tag_json_obj);


        }

        // Importing all assets like buttons, text fields
        inputUsername = (EditText) findViewById(R.id.loginUsername);
        inputPassword = (EditText) findViewById(R.id.loginPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);
        loginErrorMsg = (TextView) findViewById(R.id.login_error);
        loginErrorMsg.setText("");


        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                //userLogin();

                username = inputUsername.getText().toString();
                password = inputPassword.getText().toString();

                if (username.equals("")) {
                    loginErrorMsg.setText("Username can't be empty");
                    return;
                }
                if (password.equals("")) {
                    loginErrorMsg.setText("Password can't be empty");
                    return;
                }
                Log.d(TAG, "Login");


                /**
                 * User Volley API developed by Google to handle the request!
                 * make a http request to the server to query user's information
                 */

                // Tag used to cancel the request
                String tag_json_obj = TAG + " : Fetch User Doc";

                Map<String, String> params = new HashMap<String, String>();
                params.put("Action", "Login");
                params.put("username", username);
                CustomRequest customRequest = new CustomRequest(Request.Method.POST, ConfigURL.getLoginURL(), params, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject json) {
                        Log.d(TAG, json.toString());

                        try {
                            /**
                             * Self defined login function
                             */
                            if (!json.has(ConfigConstant.KEY_ERROR)) {
                                if (json.has(ConfigConstant.KEY_Password)) {

                                    String res = null;

                                    res = json.getString(ConfigConstant.KEY_Password);

                                    if (res != null && res.equals(password)) {
                                        //user successfully logged in
                                        //record user information into system
                                        json.put(ConfigConstant.KEY_COUCHDB_DOC_ID,username);//put the username into json since it was removed by the server for security
                                        UserCookie.storeUserLocal(pref, json);
                                        UserCookie.setLoginStatus(pref, true);
                                        Users.mapJson(json);


                                        //Launch MainActivity Screen
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        // Close all views before launching Dashboard
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);

                                        // Close Login Screen
                                        finish();
                                    } else {
                                        // Error in login
                                        loginErrorMsg.setText("Incorrect password");
                                    }
                                } else {

                                    Log.d(TAG, "Do not have password field!");
                                    loginErrorMsg.setText("Password doesn't exist");

                                }
                            } else {
                                // Error in login
                                loginErrorMsg.setText("Username doesn't exist");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error: " + error.getMessage());
                    }
                });
                // Adding request to request queue
                AppController.getInstance().addToRequestQueue(customRequest, tag_json_obj);


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


        /**
         * Facebook login
         */

        uiHelper = new UiLifecycleHelper(this, statusCallback);
        uiHelper.onCreate(savedInstanceState);


        usernameTextView = (TextView) findViewById(R.id.username);
        loginBtn = (LoginButton) findViewById(R.id.fb_login_button);
        loginBtn.setReadPermissions(Arrays.asList("email"));

        loginBtn.setUserInfoChangedCallback(new UserInfoChangedCallback() {
            @Override
            public void onUserInfoFetched(GraphUser userFB) {
                if (userFB != null) {


                    if (!UserCookie.isUserLoggedInBefore(pref)) {

                        Users.mapFBUser(userFB);
                        usernameTextView.setText("You are currently logged in as " + userFB.getName());

                        //Check logged FB user registered in our system or not; if not, the system will automatically
                        //register him into the system for the first time logging in

                        // Tag used to cancel the request
                        String tag_json_obj = TAG + " : Fetch User Doc";

                        Map<String, String> params = new HashMap<String, String>();
                        params.put(ConfigConstant.KEY_COUCHDB_DOC_ACTION, "GET");
                        params.put(ConfigConstant.KEY_COUCHDB_DOC_ID, Users.id);
                        CustomRequest customRequest = new CustomRequest(ConfigURL.getCouchDBURL(), params, new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject json) {
                                Log.d(TAG, json.toString());

                                /**
                                 * Check whether our own system has registered FB logged in user into our own system or not;
                                 * if yes, then do not need to register the information to the server; otherwise register it.
                                 */

                                if (json.has(ConfigConstant.KEY_ERROR)) {
                                    //FB user hasn't registered in our system, and need to get all the information and registered into our system.

                                    /**
                                     * Google Volley API to download profile photo from server.
                                     * For more detailed information, please refer to the official document by
                                     * http://developer.android.com/training/volley/index.html
                                     */
                                    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

                                    // If you are using normal ImageView
                                    imageLoader.get("https://graph.facebook.com/" + Users.fbId + "/picture?type=large", new ImageLoader.ImageListener() {

                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Log.e(TAG, "Image Load FB Error: " + error.getMessage());
                                        }

                                        @Override
                                        public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {
                                            Bitmap bitmap = response.getBitmap();
                                            if (bitmap != null) {
                                                // load image into imageview
                                                Users.bitmap = bitmap;

                                                /**
                                                 * Get the profile image from Facebook API and write the information includes profile image, user basic background
                                                 * information of the user to the server for the first time login using Facebook ONLY.
                                                 * This operation can be regarded as Registration
                                                 */

                                                String fileName = RandomGenerateUniqueIDs.getFileName("png");

                                                Users.profileImage = ConfigURL.getImagePathBase() + fileName;
                                                String data = Users.getUserJSON().toString();

                                                new ImageUploader(imageUploaderReply, bitmap, data, REQUEST_CODE_IMAGE_UPLOADER, fileName, null);


                                            } else {

                                                Log.e("Facebook Login", "Image doest not exist or Network Error");
                                                //Toast.makeText(MainActivity.this, "Image Does Not exist or Network Error", Toast.LENGTH_SHORT).show();
                                            }


                                        }
                                    });


                                } else {
                                    //FB user has already registered in our system, and mapping the information to the local (can be
                                    //considered as session)
                                    //Mapping user information from FB API to Users
                                    Users.mapJson(json);
                                    UserCookie.storeUserLocal(pref, json);
                                    UserCookie.setLoginStatus(pref, true);
                                    //Start the main activity
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);

                                }
                                Log.d(TAG, json.toString());


                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG, "Error: " + error.getMessage());
                            }
                        });
                        // Adding request to request queue
                        AppController.getInstance().addToRequestQueue(customRequest, tag_json_obj);


                    }
                } else {

                    usernameTextView.setText("You are not logged in.");
                }
            }
        });


    }


    /**
     * Facebook Login Session
     */
    private Session.StatusCallback statusCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state,
                         Exception exception) {
            if (state.isOpened()) {
                Log.d(TAG, "Facebook session opened.");
            } else if (state.isClosed()) {
                Log.d(TAG, "Facebook session closed.");
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
        uiHelper.onSaveInstanceState(savedState);
    }


    /**
     * Interface implementation. Receive the response from the server of which the request was usually made by new ImageUploader
     *
     * @param reply       reply message from the server, known as response
     * @param requestCode for the switch case when multiple requests are made
     */

    @Override
    public void imageUploaderReply(Object reply, int requestCode) {
        try {
            JSONObject json = new JSONObject(reply.toString());

            switch (requestCode) {
                case REQUEST_CODE_IMAGE_UPLOADER:
                    /**
                     * Response from writing the data to the server. Mapping the latest user information to Users, and update or
                     * write local session if the user information was not stored locally before.                     */

                    UserCookie.storeUserLocal(pref, Users.getUserJSON());
                    UserCookie.setLoginStatus(pref, true);
                    //Start the main activity
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);


                    break;

            }


        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }

    }


}