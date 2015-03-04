package unimelb.cis.spatialanalytics.fuelpriceshare.maps.query;

import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

import unimelb.cis.spatialanalytics.fuelpriceshare.config.URLConstant;
import unimelb.cis.spatialanalytics.fuelpriceshare.http.ServerRequest;

/**
 * <p>This class issues the Path Query for the app, which given an origin location (lat/lng), a destination location
 * (lat/lng) and a distance (double) returns the path from origin to destination and the points (fuel stations)
 * that have a distance to the path less than or equal to the given distance. </p>
 * The distance from a specific point to the path is the minimum distance between the point and any point in
 * the path, which hereafter is called 'path distance'. <p>
 *
 * The required parameters are:
 * 1. origin -- in the format of latitude,longitude
 * 2. destination -- in the format of latitude,longitude
 * 3. path_dist -- the maximum path distance in the format of a double number <p>
 *
 * A common request example is:
 * http://128.250.26.229:8080/FuelPriceSharingServer/PathQueryServlet?origin=-37.7963,144.9614&destination=-37.864,144.982&path_dist=1.0 <p>
 *
 * The server's response will be: <p>
 * 	i) A Json object with 1) the name of "fuel_station" and value of a Json array contains a
 * 	   list of the required stations, and 2) the name of "direction" and value of the response
 *     of google directions API call. <p>
 *  ii) If there are errors in any field, the corresponding value would be "error". <p>
 *  iii) If there are no query results, the corresponding value would be "empty".
 *
 *  One example result is {"fuel_station":"empty","direction":"error"}
 *
 *  Created by Yu Sun on 13/02/2015.
 */
public class PathQuery {

    private static final String LOG_TAG = PathQuery.class.getSimpleName();

    private static final String QUERY_BASE_URL = URLConstant.PATH_QUERY_BASE_URL;
    private final String TABLE_NAME = "fuel_station"; // Query table name TODO used later
    public static final String PARAM_ORIGIN = "origin";
    public static final String PARAM_DESTINATION = "destination";
    public static final String PARAM_PATH_DISTANCE = "path_dist";

    /**
     * Constructor of the class
     */
    public PathQuery(){

    }

    /**
     * This function takes the latitudes and longitudes of the origin and destination and the maximum path
     * distance (which is the minimum distance between the specific point and any point in the path) as
     * arguments, issues the Path Query to the server and returns the server's response.
     * If any error on this mobile (such as internet connection failure) occurs, it returns null.
     * @param lat_o -- the latitude of the origin
     * @param lng_o -- the longitude of the origin
     * @param lat_d -- the latitude of the destination
     * @param lng_d -- the longitude of the destination
     * @param p_dist -- the maximum path distance
     * @return
     *     <p> i) A Json object with 1) the key of "fuel_station" and value of a Json array contains a
     * 	   list of the required stations, and 2) the key of "direction" and value of the response
     *     of google directions API call. </p>
     *     <p> ii) null if any error occurs on this mobile (such as internect connection failure) </p>
     */
    public JSONObject executeQuery(
            double lat_o, double lng_o,
            double lat_d, double lng_d,
            double p_dist ){

        //throw(new UnsupportedOperationException("Sorry, Later..."));
        Uri buildUri = Uri.parse(QUERY_BASE_URL).buildUpon()
                .appendQueryParameter(PARAM_ORIGIN, lat_o + "," + lng_o)
                .appendQueryParameter(PARAM_DESTINATION, lat_d+","+lng_d)
                .appendQueryParameter(PARAM_PATH_DISTANCE, String.valueOf(p_dist))
                .build();

        URL url = null;
        try{
            url = new URL(buildUri.toString());
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error malformed URL:"+buildUri.toString(), e);
            return null;
        }

        Log.v(LOG_TAG, "The request URL is: " + url.toString());
        Log.v(LOG_TAG, "Getting data from server...");
        ServerRequest sr = new ServerRequest();
        String response = sr.getResponse(url);
        Log.v(LOG_TAG, "done!");
        if( response == null || response.isEmpty() ){
            Log.e(LOG_TAG, "Error getting results from server, check the internet or the server");
            return null;
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(response);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error json format from: " + response, e);
        }
        return jsonObject;
    }



}
