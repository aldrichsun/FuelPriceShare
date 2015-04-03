package unimelb.cis.spatialanalytics.fuelpriceshare.maps.query;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

import unimelb.cis.spatialanalytics.fuelpriceshare.config.URLConstant;
import unimelb.cis.spatialanalytics.fuelpriceshare.http.ServerRequest;

/**
 * Created by Yu Sun on 30/01/2015.
 * Sample query:
 * http://128.250.26.229:8080/FuelPriceSharingServer/RangeQueryServlet?lng=144.9614&lat=-37.7963&r_dist=2
 *
 * Function: Given a query point and a range radius, return all the points (fuel stations) in the circle that
 * is centered at the query point and has a radius equals the range radius.
 *
 * Input: Query point -- LatLng, Range radius -- Double
 * Output:
 *         1. A json object containing the fuel stations in the range whose key is the
 *         table name and value is the fuel stations in a json array, i.e.,
 *              {"table_name (fuel_station)":["list of points (fuel stations)"]};
 *         2. if there are no fuel stations in the range, the json array is empty;
 *         3. json object {"error":3001} if the user doesn't have sufficient credit.
 *         4. json object {"error":3002} if internal DB error occurs in the process.
 *         5. null, if any error occurs on the mobile end mostly due to internet failure
 *
 *         Note: the json format of each station follows the data model design.
 *
 */
public class RangeQuery {

    private static final String LOG_TAG = RangeQuery.class.getSimpleName();
    // The host address of the API (servlet)
    private final String QUERY_BASE_URL = URLConstant.RANGE_QUERY_BASE_URL;
    // These names are determined by the API documentation
    private final String TABLE_NAME = "fuel_station"; // Query table name
    private final String LAT_PARAM = "lat"; // Parameter latitude name
    private final String LNG_PARAM = "lng"; // Parameter longitude name
    private final String DIST_PARAM = "r_dist"; // Parameter range distance name
    private final String USER_PARAM = "user_id"; // Parameter user id

    /**
     * Default constructor
     */
    public RangeQuery(){
    }

    /**
     * This function returns all the points (fuel stations) in the circle that is centered at queryPoint
     * and has a radius of rangeDist.
     * @param queryPoint -- the query point
     * @param rangeDist -- the range distance
     * @param userId -- the user id
     * @return
     *         1. A json object containing the fuel stations in the range whose key is the
     *         table name and value is the fuel stations in a json array, i.e.,
     *         {"table_name (fuel_station)":["list of points (fuel stations)"]};
     *         2. If there are no fuel stations in the range, the json array is empty;
     *         3. json object {"error":3001} if the user doesn't have sufficient credit.
     *         4. json object {"error":3002} if internal DB error occurs in the process;
     *         5. null, if any error occurs on the mobile end mostly due to internet failure
     *
     *         Note: the json format of each station follows the data model design.
     */
    public JSONObject executeQuery(LatLng queryPoint, Double rangeDist, String userId){

        // Build the query URL
        Uri builtUri = Uri.parse(QUERY_BASE_URL).buildUpon()
                .appendQueryParameter(LAT_PARAM, String.valueOf(queryPoint.latitude))
                .appendQueryParameter(LNG_PARAM, String.valueOf(queryPoint.longitude))
                .appendQueryParameter(DIST_PARAM, String.valueOf(rangeDist))
                .appendQueryParameter(USER_PARAM, userId)
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
            Log.e(LOG_TAG, "Error getting results from server, which may caused by internet failure");
            return null;
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(res);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error when parsing json array string: " + res +
                    ", which may caused by internet failure", e);
            return null;
        }

        return jsonObject;
    }

}