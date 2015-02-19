package unimelb.cis.spatialanalytics.fuelpriceshare.maps.autoComplete;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import unimelb.cis.spatialanalytics.fuelpriceshare.http.ServerRequest;

/**
 * Created by Yu Sun on 5/02/2015.
 * For user input automatic completion.
 * TODO Optimize the auto complete by the user's historical search and current location
 */
public class AutoComplete {

    private static final String LOG_TAG = AutoComplete.class.getSimpleName();

    // API host address
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete"; // API request function
    private static final String OUT_JSON = "/json"; // API return type

    // My API key
    private static final String API_KEY = "AIzaSyBaLZ9LFvlLmVL16xGDQkKOF5Ml69_JcSI";

    public AutoComplete(){
        //API_KEY = getString(R.string.map_api_key);
    }

    public static ArrayList<String> autoComplete(String input) {
        ArrayList<String> resultList = null;

        String jsonResults = null;
        try {

            //TODO change to Uri methods
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            ///////// 05/02/2015 Yu Sun: Should be changed after debug and testing //////////////
            sb.append("&components=country:au");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());

            // Submit request to the server and read the response
            ServerRequest serverRequest = new ServerRequest();
            jsonResults = serverRequest.getResponse(url);

            if( jsonResults == null ){
                Log.e(LOG_TAG, "Error getting auto complete places for URL:" + url.toString());
                return resultList;
            }

        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (UnsupportedEncodingException e) {
            Log.e(LOG_TAG, "Error unsupported encoding", e);
        } finally {
            ////Yu Sun 05/02/2015 Currently nothing
        }

        // Parse the returned json string and form the result Json array
        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<String>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }
}
