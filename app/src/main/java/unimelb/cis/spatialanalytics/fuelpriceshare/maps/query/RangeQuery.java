package unimelb.cis.spatialanalytics.fuelpriceshare.maps.query;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

import unimelb.cis.spatialanalytics.fuelpriceshare.http.ServerRequest;

/**
 * TODO Improve the server and client API document, especially on handling exceptions and errors.
 * Created by Yu Sun on 30/01/2015.
 * Sample query:
 * http://128.250.26.229:8080/FuelPriceSharingServer/RangeQueryServlet?lng=144.9614&lat=-37.7963&r_dist=2
 *
 * Function: Given a query point and a range radius, return all the points (fuel stations) in the circle that
 * is centered at the query point and has a radius equals the range radius.
 *
 * Input: Query point -- LatLng, Range radius -- Double
 * Output: 1. A Json array contains all the stations -- Json array.
 *         2. When there are internal server errors, return null. TODO implement this
 *         3. When there are no stations within the range, return an empty Json array.
 *         Note: the json format of each station follows the data model design.
 *
 */
public class RangeQuery {

    private static final String LOG_TAG = RangeQuery.class.getSimpleName();
    // The host address of the API (servlet)
    private final String QUERY_BASE_URL = "http://128.250.26.229:8080/FuelPriceSharingServer/RangeQueryServlet?";
    // These names are determined by the API documentation
    private final String TABLE_NAME = "fuel_station"; // Query table name
    private final String LAT_PARAM = "lat"; // Parameter latitude name
    private final String LNG_PARAM = "lng"; // Parameter longitude name
    private final String DIST_PARAM = "r_dist"; // Parameter range distance name

    /**
     * Default constructor
     */
    public RangeQuery(){
    }

    /**
     * Return all the points (fuel stations) in the circle that is centered at queryPoint
     * and has a radius of rangeDist.
     * @param queryPoint -- the query point
     * @param rangeDist -- the range distance
     * @return 1. A Json array contains all the stations -- Json array.
     *         2. When there is any error on this mobile (such as internet connection errors),
     *         return null. TODO implement this
     *         3. When there are no stations within the range, return an empty Json array.
     *
     *         Note: the json format of each station follows the data model design.
     */
    public JSONArray executeQuery(LatLng queryPoint, Double rangeDist){

        // Build the query URL
        Uri builtUri = Uri.parse(QUERY_BASE_URL).buildUpon()
                .appendQueryParameter(LAT_PARAM, String.valueOf(queryPoint.latitude))
                .appendQueryParameter(LNG_PARAM, String.valueOf(queryPoint.longitude))
                .appendQueryParameter(DIST_PARAM, String.valueOf(rangeDist))
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error malformed URL from: " + builtUri.toString(), e);
        }
        Log.v(LOG_TAG, "Built URI " + builtUri.toString());

        Log.v(LOG_TAG, "Getting data from server...");
        ServerRequest serverRequest = new ServerRequest();
        String res = serverRequest.getResponse(url);
        Log.v(LOG_TAG, "done!");
        if( res == null || res.isEmpty() ){
            Log.e(LOG_TAG, "Error getting results from server");
            return null;
        }

        // res is not null, so we try to parse.
        // If the query result is empty, the server returns a JSON object with
        // an empty JSON array.
        /* TODO Currently, if there are some internal server errors, the server still
        returns a JSON object with an empty JSON array, which is unacceptable and
        needs improvement.*/

        JSONArray jsonArray = null;
        try {
            JSONObject jsonObject = new JSONObject(res);
            jsonArray = jsonObject.getJSONArray(this.TABLE_NAME);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error when parsing json array string: " + res, e);
            return null;
        }

        return jsonArray;
    }

}