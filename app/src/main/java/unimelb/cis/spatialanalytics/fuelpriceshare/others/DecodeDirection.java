package unimelb.cis.spatialanalytics.fuelpriceshare.others;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Yu Sun on 24/02/2015.
 */
public class DecodeDirection {

    private static final String LOG_TAG = DecodeDirection.class.getSimpleName();

    /**
     * This method gets a list of starting and ending points in the "steps" json array returned
     * by the Google Directions API.
     *
     * An example of the "steps" json array is:
     *"steps" : [{"distance" : { "text" : "0.7 km","value" : 708},"duration" : {"text" : "2 mins","value" : 115},
     *       "end_location" : {"lat" : -37.8164178,"lng" : 144.9558208}, ...
     *       "start_location" : {"lat" : -37.8141755,"lng" : 144.9631975},"travel_mode" : "DRIVING"},
     *       {"distance" : {"text" : "1.4 km","value" : 1385}, ...]
     *
     * @param apiResponse -- the Google Directions API response
     * @return An array list of "start_locations" (points) and "end_locations" (points) in the format
     * of object LatLng.
     */
    public static ArrayList<LatLng> getAllDirectionPoints(String apiResponse) {

        ArrayList<LatLng> result = new ArrayList<>();
        try {

            JSONObject resJson = new JSONObject(apiResponse);
            JSONArray legs = resJson.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");
            for (int i = 0; i < legs.length(); i++) { // for each leg

                JSONArray steps = legs.getJSONObject(i).getJSONArray("steps");
                for (int j = 0; j < steps.length(); j++) { // for each step, i.e., route segment

                    JSONObject step = steps.getJSONObject(j);

                    result.add(new LatLng(
                            step.getJSONObject("start_location").getDouble("lat"),
                            step.getJSONObject("start_location").getDouble("lng")));

                    String polylineStr = step.getJSONObject("polyline").getString("points");
                    result.addAll(getPolylinePoints(polylineStr));

                    result.add(new LatLng(
                            step.getJSONObject("end_location").getDouble("lat"),
                            step.getJSONObject("end_location").getDouble("lng")));
                }
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error wrong json formats when parsing the api response", e);
        }
        return result;
    }

    /**
     * This function decodes the points of the "polyline" json object returned by the Google
     * directions API. An example of such a json object is:
     * "polyline" : { "points" : "be`fFqg{sZTFNBJFNFVTVPNFRF`@BXBRBHBbBBX@T?\\Ab@ATCZCRCRC^GPIb@Oh@U`@SVODEXSTY^]RS@A|A{ANM"}
     *
     * Using these points, the drawn polyline representing the routed path will be smoother and
     * follow the road networks better.
     *
     * Refer to http://blog-emildesign.rhcloud.com/?p=822
     * https://developers.google.com/maps/documentation/utilities/polylinealgorithm
     * @param encoded -- the encoded string
     * @return An array list of points in the form of object LatLng
     */
    private static ArrayList<LatLng> getPolylinePoints(String encoded) {

        ArrayList<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {

            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng position = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(position);

        }
        return poly;
    }
}
