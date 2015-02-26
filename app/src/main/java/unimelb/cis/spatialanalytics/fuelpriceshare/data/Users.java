package unimelb.cis.spatialanalytics.fuelpriceshare.data;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;

import com.facebook.model.GraphUser;

import org.json.JSONException;
import org.json.JSONObject;

import unimelb.cis.spatialanalytics.fuelpriceshare.config.ConfigConstant;


/**
 * Created by hanl4 on 4/02/2015.
 * Mapping user information into this class to make it convenient to access user information anytime.
 */
public class Users {

    /*
    User information attribute/field
     */
    public static String id = "";
    public static String firstName = "";
    public static String lastName = "";
    public static String middleName = "";
    public static String userName = "";
    public static String email = "";
    public static String phone = "";
    public static String gender = "";
    public static String birth = "";
    public static String profileImage = "";//URLConfig.getDefaultProfileImageURL();
    public static Bitmap bitmap = null;
    public static String fbId = "";
    public static String whatup = "";
    public static String password = "";

    public static String tempProfielImageName="";

    private static final String TAG = "Users";

    /*
    Mainly for JSON usability
     */
    public static final String KEY_ID = "_id";
    public static final String KEY_FIRST_NAME = "firstName";
    public static final String KEY_LAST_NAME = "lastName";
    public static final String KEY_MIDDLE_NAME = "middleName";
    public static final String KEY_USERNAME = "userName";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_GENDER = "gender";
    public static final String KEY_BIRTH = "birth";
    public static final String KEY_PROFILEIMAGE = "profileImage";
    public static final String KEY_FBID = "fbId";
    public static final String KEY_BITMAP = "bitmap";
    public static final String KEY_WHATUP = "whatup";
    public static final String KEY_PASSWORD = "password";


    /**
     * Mapping user information from Facebook
     *
     * @param userFB Facebook User Object. For details, please refer to Facebook SDK
     */
    public static void mapFBUser(GraphUser userFB) {
        id = "FB_" + userFB.getId();
        fbId = userFB.getId();
        firstName = userFB.getFirstName() == null ? "" : userFB.getFirstName();
        lastName = userFB.getLastName() == null ? "" : userFB.getLastName();
        middleName = userFB.getMiddleName() == null ? "" : userFB.getMiddleName();
        userName = userFB.getName() == null ? "" : userFB.getName();
        email = userFB.getProperty("email").toString() == null ? "" : userFB.getProperty("email").toString();
        birth = userFB.getBirthday() == null ? "" : userFB.getBirthday();

    }


    /**
     * Map user information with JSON. Usually it is used to update local user information after receiving query from the server
     * CouchDB user document to make the local user information latest.
     *
     * @param json contains user information (generally defined by CouchDB user document)
     */

    public static void mapJson(JSONObject json) {
        try {
            id = json.getString(KEY_ID);
            fbId = json.getString(KEY_FBID);
            profileImage = json.getString(KEY_PROFILEIMAGE);
            gender = json.getString(KEY_GENDER);
            firstName = json.getString(KEY_FIRST_NAME);
            lastName = json.getString(KEY_LAST_NAME);
            middleName = json.getString(KEY_MIDDLE_NAME);
            userName = json.getString(KEY_USERNAME);
            email = json.getString(KEY_EMAIL);
            phone = json.getString(KEY_PHONE);
            birth = json.getString(KEY_BIRTH);
            whatup = json.getString(KEY_WHATUP);
            //please notice that, it the user was registered by facebook login, he or she probably doesn't have the field
            //of password
            if (json.has(KEY_PASSWORD))
                password = json.getString(KEY_PASSWORD);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }

    }


    /**
     * Get user JSONObject
     * @return
     */

    public static JSONObject getUserJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put(KEY_ID, id);
            json.put(KEY_FBID, fbId);
            json.put(KEY_FIRST_NAME, firstName);
            json.put(KEY_LAST_NAME, lastName);
            json.put(KEY_MIDDLE_NAME, middleName);
            json.put(KEY_USERNAME, userName);
            json.put(KEY_EMAIL, email);
            json.put(KEY_PHONE, phone);
            json.put(KEY_GENDER, gender);
            json.put(KEY_BIRTH, birth);
            json.put(KEY_PROFILEIMAGE, profileImage);
            json.put(KEY_WHATUP, whatup);
            json.put(KEY_PASSWORD, password);

            //     json.put(KEY_BITMAP,bitmap);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;

    }


    /**
     * Get user JSONObject for uploading image to the server
     * @param profileImageTemp profile image name
     * @return JSONObject
     */
    public static JSONObject getUserJSONForImageUpload(String profileImageTemp) {

        tempProfielImageName=profileImageTemp;

        JSONObject json = new JSONObject();
        try {
            json.put(KEY_ID, id);
            json.put(KEY_FBID, fbId);
            json.put(KEY_FIRST_NAME, firstName);
            json.put(KEY_LAST_NAME, lastName);
            json.put(KEY_MIDDLE_NAME, middleName);
            json.put(KEY_USERNAME, userName);
            json.put(KEY_EMAIL, email);
            json.put(KEY_PHONE, phone);
            json.put(KEY_GENDER, gender);
            json.put(KEY_BIRTH, birth);
            json.put(KEY_PROFILEIMAGE, profileImageTemp);
            json.put(KEY_WHATUP, whatup);
            json.put(KEY_PASSWORD, password);

            //     json.put(KEY_BITMAP,bitmap);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;

    }



    /**
     * Get the user in JSON format but without the information of password
     *
     * @return user in json
     */

    public static JSONObject getUserJSONWithoutPassword() {
        JSONObject json = new JSONObject();
        try {
            json.put(KEY_ID, id);
            json.put(KEY_FBID, fbId);
            json.put(KEY_FIRST_NAME, firstName);
            json.put(KEY_LAST_NAME, lastName);
            json.put(KEY_MIDDLE_NAME, middleName);
            json.put(KEY_USERNAME, userName);
            json.put(KEY_EMAIL, email);
            json.put(KEY_PHONE, phone);
            json.put(KEY_GENDER, gender);
            json.put(KEY_BIRTH, birth);
            json.put(KEY_PROFILEIMAGE, profileImage);
            json.put(KEY_WHATUP, whatup);


        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
        return json;

    }


    /**
     * Get user document ID of CouchDB
     *
     * @return
     */

    public static String getDocID() {
        return id;
    }


    /**
     * Mapping user information from preferences (session)
     *
     * @param pref SharedPreferences
     */
    public static void mapSharedPreference(SharedPreferences pref) {
        String user = pref.getString(ConfigConstant.LOCAL_USER_JSON, "");


        try {
            JSONObject json = new JSONObject(user);
            id = json.getString(KEY_ID);
            fbId = json.getString(KEY_FBID);
            profileImage = json.getString(KEY_PROFILEIMAGE);
            gender = json.getString(KEY_GENDER);
            firstName = json.getString(KEY_FIRST_NAME);
            lastName = json.getString(KEY_LAST_NAME);
            middleName = json.getString(KEY_MIDDLE_NAME);
            userName = json.getString(KEY_USERNAME);
            email = json.getString(KEY_EMAIL);
            phone = json.getString(KEY_PHONE);
            birth = json.getString(KEY_BIRTH);
            whatup = json.getString(KEY_WHATUP);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }


    }

    /**
     * Clear user information
     */
    public static void clearUserInfo() {
        id = "";
        firstName = "";
        lastName = "";
        middleName = "";
        userName = "";
        email = "";
        phone = "";
        gender = "";
        birth = "";
        profileImage ="";
        bitmap = null;
        fbId = "";
        whatup = "";
        password = "";
    }
}
