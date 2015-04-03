package unimelb.cis.spatialanalytics.fuelpriceshare.maps.locationHistory;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import unimelb.cis.spatialanalytics.fuelpriceshare.config.URLConstant;
import unimelb.cis.spatialanalytics.fuelpriceshare.data.Users;
import unimelb.cis.spatialanalytics.fuelpriceshare.http.ServerRequest;

/**
 * Created by yus1 on 3/03/2015.
 * Retrieve and store the location history for the user
 */
public class SearchLocationHistory {

    private static final String LOG_TAG = SearchLocationHistory.class.getSimpleName();

    // The host address of the API (servlet)
    private static final String QUERY_BASE_URL = URLConstant.LOC_HISTORY_BASE_URL;

    // These names are determined by the API documentation
    private static final String PARAM_USER_ID = "user_id"; // user id param
    private static final String PARAM_ADDRESS = "addr"; // address param

    private static final String LOCATION_HISTORY_COLUMN_LOCATION_HISTORY = "location_history";
    private static final String ERROR_VALUE = "error";

    private static final int CONNECTION_TIMEOUT = 1 * 500;

    /**
     * Default constructor
     */
    public SearchLocationHistory(){
    }

    /**
     * Get the user's address search history
     * @param user_id  -- the id of the user whose history it gets
     * @return
     * 1) A list of string representing the user's location search history
     * 2) an empty list, if any error (including server error) occurs
     */
    public static ArrayList<String> get( String user_id ){

        ArrayList<String> result = new ArrayList<>();

        Uri url_builder = Uri.parse(QUERY_BASE_URL).buildUpon()
                .appendQueryParameter(PARAM_USER_ID, user_id)
                .build();
        URL url = null;
        try {
            url = new URL(url_builder.toString());
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Malformed url: " + url_builder.toString(), e);
            return result; // an empty list
        }

        ServerRequest serverRequest = new ServerRequest( CONNECTION_TIMEOUT );
        String res = serverRequest.getResponse(url);
        if( res == null ){
            return result; // an empty list
        }

        try {
            JSONObject jsonObj = new JSONObject(res);
            if( jsonObj.optString(
                    LOCATION_HISTORY_COLUMN_LOCATION_HISTORY ).equals(ERROR_VALUE) ){
                return result;
            }
            //else we have gotten the address in a json array
            JSONArray jsonArray = jsonObj.getJSONArray(LOCATION_HISTORY_COLUMN_LOCATION_HISTORY);
            result = new ArrayList<>( jsonArray.length() );
            for(int i = 0; i < jsonArray.length(); i++){
                result.add( jsonArray.getString(i) );
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error creating json from: " + res, e);
            return result; // return the content that has been added
        }

        return result;
    }

    /**
     * Store the given address to the server.
     * If error occurs, we do noting.
     * @param address -- the address to be stored
     */
    public static void store( String address ){

        Uri url_builder = Uri.parse(QUERY_BASE_URL).buildUpon()
                .appendQueryParameter(PARAM_USER_ID, Users.id)
                .appendQueryParameter(PARAM_ADDRESS, address)
                .build();

        URL url = null;
        try {
            url = new URL(url_builder.toString());
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Malformed url: " + url_builder.toString(), e);
            return;
        }

        ServerRequest serverRequest = new ServerRequest( "PUT" );
        serverRequest.getResponse( url );
    }
}
