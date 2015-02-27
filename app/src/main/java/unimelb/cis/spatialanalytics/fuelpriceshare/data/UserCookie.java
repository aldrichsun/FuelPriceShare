package unimelb.cis.spatialanalytics.fuelpriceshare.data;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.facebook.Session;

import org.json.JSONObject;

import unimelb.cis.spatialanalytics.fuelpriceshare.login.LoginActivity;
import unimelb.cis.spatialanalytics.fuelpriceshare.config.ConfigConstant;


/**
 * Created by hanl4 on 7/02/2015.
 * Store user information locally. Used as session
 */
public class UserCookie {


    private static final String LOCAL_USER_JSON= ConfigConstant.LOCAL_USER_JSON;
    private static final String LOCAL_USER_LOGIN= ConfigConstant.LOCAL_USER_LOGIN;


    /**
     * Store user information from Json, and mapping the json to SharedPreference pref
     * @param pref SharedPreferences known as one of local storage methods
     * @param json JSONObject contains user information
     */
    public static void storeUserLocal(SharedPreferences pref,JSONObject json) {

            pref.edit().putString(LOCAL_USER_JSON, json.toString()).commit();
            pref.edit().apply();

    }

    /**
     * Store user into pref by calling Uses.getUserJSON method directly.
     * @param pref SharedPreferences known as one of local storage methods
     */

    public static void storeUserLocal(SharedPreferences  pref) {
        JSONObject json= Users.getUserJSON();
        pref.edit().putString(LOCAL_USER_JSON, json.toString()).commit();
        pref.edit().apply();

    }


    /**
     * To detect user has logged in the system before or not.
     * @param pref SharedPreferences
     * @return true means user has logged into the system before; otherwise return false.
     */
    public static boolean isUserLoggedInBefore(SharedPreferences pref)

    {
        if(pref.contains(LOCAL_USER_LOGIN))
            if(pref.getBoolean(LOCAL_USER_LOGIN,false))
                return true;

        return false;

    }

    /**
     * If user has successfully logged into our system, record it locally.
     * @param pref SharedPreferences
     * @param flag set the the status of logging the same value as flag
     */

    public  static  void setLoginStatus(SharedPreferences pref,boolean flag)
    {
        pref.edit().putBoolean(LOCAL_USER_LOGIN,flag).commit();
        pref.edit().apply();

    }


    /**
     * Clear local user information including Facebook login
     * @param pref SharedPreferences
     * @param context the host activity context
     */
    public static  void clearUserInfo(SharedPreferences pref,Context context)
    {
        callFacebookLogout(context);//log out facebook session
        pref.edit().putBoolean(LOCAL_USER_LOGIN,false).commit();
        pref.edit().remove(LOCAL_USER_JSON);//remove the records stored in local
        pref.edit().apply();
        Users.clearUserInfo();//clear Users as well


    }

    /**
     * Log out the user from session including facebook login
     * @param pref SharedPreferences
     * @param context the host activity context
     */

    public static  void logOut(SharedPreferences pref,Context context)
    {
        callFacebookLogout(context);//log out facebook login
        pref.edit().putBoolean(LOCAL_USER_LOGIN, false).commit();
        pref.edit().apply();
        Users.clearUserInfo();
        /*
        after logging out, return back to login screen
         */

        Intent intent=new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        //return true;


    }

    /**
     * Logout From Facebook
     * @param context the host activity context
     */
    public static void callFacebookLogout(Context context) {
        //get facebook session
        Session session = Session.getActiveSession();
        if (session != null) {

            if (!session.isClosed()) {
                session.closeAndClearTokenInformation();
                //clear your preferences if saved
            }
        } else {

            session = new Session(context);
            Session.setActiveSession(session);

            session.closeAndClearTokenInformation();
            //clear your preferences if saved

        }

    }





}
