package unimelb.cis.spatialanalytics.fuelpriceshare.maps.DrawOnMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import unimelb.cis.spatialanalytics.fuelpriceshare.R;

/**
 * Created by Yu Sun on 20/02/2015.
 * This 'functional class' draws the points (fuel stations) stored in a json array on the given map.
 */
public class DrawMarkersOnMap {

    private static final String LOG_TAG = DrawMarkersOnMap.class.getSimpleName();
    private static final int maxNumberOfDrawnStations = 10;
    private static final String[] mColors = //from material design: http://www.google.com/design/spec/style/color.html#color-color-palette
            {"#bf360c", "#d84315",   //orange from heavy to light
             "#e64a19", "#f4511e",
             "#ff5722", "#ff7043",
             "#ff8a65", "#ffab91",
             "#ffccbc", "#fbe9e7"};
//            {"#ff3d00", "#ff9100", //different color
//            "#ffc400", "#ffea00",
//            "#c6ff00", "#76ff03",
//            "#00e676", "#1de9b6",
//            "#00e5ff", "#00b0ff"};

    private static ArrayList<mStation> myStations; //this list is changed only when
                        // function drawOnMapMaxTenDifferentColor() is called
    private static String preferredFuelType; //this string is initialized only when
                        // function drawOnMapMaxTenDifferentColor() is called
    private static double DEFAULT_PRICE_VALUE = Double.MAX_VALUE;  // the value of the price if we there's no real fuel price


    /**
     * Created by Yu Sun on 21/02/2015:
     * This function draws the points (fuel stations) stored in a json array on the given map.
     * The marker's title is (and must be) the station's name, and it's snippet is (must be)
     * the list of the provided fuels and prices.
     * NOTE: If this is changed, other places using the marker's title or snippet for switch
     * (e.g., if-else) must be changed accordingly.
     * @param actionBarActivity -- the activity the map will be presented
     * @param mMap -- the map we draw markers on
     * @param jsonArray -- the json array storing the points (fuel stations)
     */
    @Deprecated
    public static void drawOnMap( ActionBarActivity actionBarActivity,
                                  GoogleMap mMap,
                                  JSONArray jsonArray){
        //all contents are removed by Yu Sun on 03/04/2015
    }

    /**
     * Yu Sun 21/02/2015: Given the returned stations and the station we need to move
     * the focus, this methods determines the zoom level of the map camera.
     * Currently, the implementation of this class is very straightforward. To customized
     * the camera view after the query results, modify this method at your will.
     * @param station_list -- the stations in an array list
     * @param focus -- the station we focus on
     * @return
     */
    private static int determineZoomLevel( ArrayList<mStation> station_list, LatLng focus ){

        if(station_list.size() >= 5)
            return 14;
        //else
        return 13;
    }

    /**
     * Created by Yu Sun on 25/02/2015:
     * This function draws at most 10 points (fuel stations) with the lowest prices stored in
     * a json array on the given map. Each marker has a difference color.
     * The marker's title is (and must be) the station's name, and it's snippet is (must be)
     * the list of the provided fuels and prices.
     * NOTE: If this is changed, other places using the marker's title or snippet for switch
     * (e.g., if-else) must be changed accordingly.
     *
     * Update history
     * 03/04/2015 Nurlan Kenzhebekov and Yu Sun: check overlap of station markers.
     * Logic: Priority is given to the markers with lower price. Starting from the
     *        station A with the lowest price, we show a brief marker for all stations
     *        that overlap with A. Then, we move to the next lowest price station that
     *        doesn't overlap with A and repeat the above steps until all stations are checked.
     *
     * @param actionBarActivity -- the activity the map will be presented
     * @param mMap -- the map we draw markers on
     * @param jsonArray -- the json array storing the points (fuel stations)
     */
    public static void drawOnMap( ActionBarActivity actionBarActivity,
                                                      GoogleMap mMap,
                                                      JSONArray jsonArray,
                                                      LatLng focusLoc
    ) {

        preferredFuelType = PreferenceManager.getDefaultSharedPreferences(actionBarActivity)
                .getString(
                        actionBarActivity.getString(R.string.pref_key_preferred_fuel),
                        "Unleaded"
                );

        //conver the jsonArray to a collection and sort the collection by price in ascending order
        ArrayList<mStation> stationList = getStationList(
                actionBarActivity, jsonArray, preferredFuelType);
        Collections.sort(stationList);

        //keep the sorted result in the private array list myStations
        if( myStations != null )
            myStations.clear();
        else
            myStations = new ArrayList<>();

        int used_color = 0;
        for(int i = 0; i < stationList.size() && i < maxNumberOfDrawnStations; i++) {

            mStation station = stationList.get(i);
            //If the fuel station has no contributed price, we only show its short name with black color.
            //Otherwise, we show its short name and PRICE with the color in the mColors array
            if (station.mPrice == DEFAULT_PRICE_VALUE) { // has no contributed price
                station.mFullMarkerIcon = CustomizeMapMarker.generateBitmapFromText(
                        actionBarActivity.getApplicationContext(),
                        station.mShortName,
                        Color.BLUE);
            }
            else{ //
                station.mFullMarkerIcon = CustomizeMapMarker.generateBitmapFromText(
                        actionBarActivity.getApplicationContext(),
                        station.mShortName + ": " + station.mPrice,
                        Color.parseColor(mColors[used_color++]));
            }
            //the dot marker
            station.mBriefMarkerIcon = CustomizeMapMarker.generateBitmapFromText(
                    actionBarActivity.getApplicationContext(),
                    null, // no string, which gives a single dot
                    Color.RED);

            myStations.add( stationList.get(i) );
        }

        ///////////////////////// Check overlap ////////////////////////////
        //02/04/2015 Determine whether to show the full marker of the station or not.
        //Priority is given to the ones with lower price. Starting from the
        //station A with the lowest price, we show a brief marker for all stations
        //that overlap with A. Then, we move to the next lowest price station that
        //doesn't overlap with A and repeat the above steps until all stations are checked.
        // First move the camera to the proper zoom level
        //////// 14/02/2015 Yu Sun: zoom out the map for one level and move the focus to a fuel station ////////////
        int zoomLevel = determineZoomLevel(myStations, focusLoc);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(focusLoc)
                .zoom(zoomLevel)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        //////////////////////////////////////////////////////////////////////

        // initially we show full markers for all stations
        for(int i = 0; i < myStations.size(); i++) {
            myStations.get(i).mShowFullMarker = true;
        }
        // check overlap
        for(int i = 0; i < myStations.size(); i++) {

            if( !myStations.get(i).mShowFullMarker ) continue; //it is already hidden (i.e., not show full marker)
            mStation highPriorityStation = myStations.get(i); //otherwise
            for(int j = i+1; j < myStations.size(); j++){ //check overlap with remaining stations

                if( !myStations.get(j).mShowFullMarker ) continue; //it is already hidden
                if( determineOverlap( highPriorityStation, myStations.get(j), mMap ) )
                    myStations.get(j).mShowFullMarker = false;
            }
        }
        ///////////////////////////////////////////////////////////////////////////

        // draw the station markers
        for (int i = 0; i < myStations.size(); i++) {

            mStation station = stationList.get(i);

            String snippet_string = preferredFuelType + ": ";
            if( station.mPrice == DEFAULT_PRICE_VALUE )
                snippet_string += "unknown";
            else
                snippet_string += station.mPrice;

            // Add a corresponding marker in the map
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(station.mLocation)
                    .title(station.mFullName)
                    .snippet( snippet_string )
//                            .icon(BitmapDescriptorFactory.fromBitmap(
//                                    CustomizeMapMarker.writeTextOnDrawable(
//                                            actionBarActivity,
//                                            actionBarActivity.getApplicationContext(),
//                                            //R.drawable.blue_rect,
//                                            R.drawable.rounded_rect,
//                                            sb.toString(),
//                                            Color.BLUE
//                                    )));
                    .icon(BitmapDescriptorFactory.fromBitmap(
                            station.mShowFullMarker ? station.mFullMarkerIcon : station.mBriefMarkerIcon
//                            CustomizeMapMarker.generateBitmapFromText(
//                                    actionBarActivity.getApplicationContext(),
//                                    station.mShortName + ": " + station.mPrice,
//                                    Color.parseColor(mColors[i])
//                            )
                    ));
            station.mMarker = mMap.addMarker(markerOptions);
        }
        // end for all stations
    }

    /**
     * Created by Nurlan Kenzhebekov and Yu Sun on 03/04/2015:
     * Redraw the marker when the user zoom in or out on the current map.
     *
     * This function is only useful when drawOnMapMaxTenDifferentColor is firstly called
     * which keeps the information of the stations we show on map including their markers
     * in the private variable 'myStations'.
     * If 'myStations' is null, we do nothing.
     *
     * @param actionBarActivity -- the activity the map will be presented
     * @param mMap -- the map we draw markers on
     */
    public static void reDrawOnMap( ActionBarActivity actionBarActivity, GoogleMap mMap ) {

        if( myStations == null || myStations.isEmpty() )
            return;

        ///////////////////////// Check overlap ////////////////////////////
        // initially we show full markers for all stations
        for(int i = 0; i < myStations.size(); i++) {
            myStations.get(i).mShowFullMarker = true;
        }
        // check overlap
        for(int i = 0; i < myStations.size(); i++) {

            if( !myStations.get(i).mShowFullMarker ) continue; //it is already hidden (i.e., not show full marker)
            mStation highPriorityStation = myStations.get(i); //otherwise
            for(int j = i+1; j < myStations.size(); j++){ //check overlap with remaining stations

                if( !myStations.get(j).mShowFullMarker ) continue; //it is already hidden
                if( determineOverlap( highPriorityStation, myStations.get(j), mMap ) )
                    myStations.get(j).mShowFullMarker = false;
            }
        }
        ///////////////////////////////////////////////////////////////////////////

        ////////////////////// remove the current station markers ////////////////////////
        for(int i = 0; i < myStations.size(); i++) {
            myStations.get(i).mMarker.remove();
        }

        //////////////////// re-draw the station markers ///////////////////////
        for(int i = 0; i < myStations.size(); i++) {

            mStation station = myStations.get(i);

            String snippet_string = preferredFuelType + ": ";
            if( station.mPrice == DEFAULT_PRICE_VALUE )
                snippet_string += "unknown";
            else
                snippet_string += station.mPrice;

            // Add a corresponding marker in the map
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(station.mLocation)
                    .title(station.mFullName)
                    .snippet( snippet_string )
                    .icon(BitmapDescriptorFactory.fromBitmap(
                            station.mShowFullMarker ? station.mFullMarkerIcon : station.mBriefMarkerIcon
                    ));
            station.mMarker = mMap.addMarker(markerOptions);
        }
        // end for all stations
    }

    /**
     * Clear the contents in myStations.
     * Used when a query cycle is ended.
     */
    public static void clearStations(){
        if( myStations != null ) myStations.clear();
    }

    /**
     * The mStation type is a self-defined private class for sorting the fuel stations
     * based on their prices and keeping the stations' markers.
     *
     * mStation has field:
     *  mLocation (LatLng): which will be used to draw the location of the station on the map,
     *  mShortName (String): which will be shown in the customized marker
     *  mFullName (String): which will be shown in the marker's info window
     *  mPrice (double): which is the price of the user's preferred fuel type, and will be
     *  shown in the customized marker
     *  mFullMarkerIcon (bitmap): the marker of the station on the map showing its name and fuel price
     *  mBriefMarkerIcon (bitmap): the marker of the station showing only a dot
     *  mShowFullMarker (boolean): true if the full marker of this station is shown in current map view
     */
    private static class mStation implements Comparable<mStation>{

        public LatLng mLocation;     //The location of the station
        public String mShortName;   //The short name of the station which
                                    // will be shown in the customized marker
        public String mFullName;    //The full name of the station which is the price of the user's preferred fuel type
                                    // and will be shown in the info window
        public double mPrice;    // the price of the user preferred fuel type

        public Bitmap mFullMarkerIcon; // the marker of the station on the map showing its name and fuel price
        public Bitmap mBriefMarkerIcon; // the marker of the station showing only a dot
        public boolean mShowFullMarker; // true if the full marker of this station is shown in current map view
        public Marker mMarker; // the marker of the station on the map

        public mStation(LatLng mLocation, String mFullName, String mShortName, double mPrice){
            this.mLocation = mLocation;
            this.mFullName = mFullName;
            this.mShortName = mShortName;
            this.mPrice = mPrice;
        }

        @Override
        public int compareTo(mStation another) {
            if( this.mPrice < another.mPrice )
                return -1;
            else if( this.mPrice > another.mPrice )
                return 1;
            return 0;
        }
    }

    /**
     * Given a json array representing a list of fuel stations as json objects, this function
     * converts the json object into a mStation type, which is a self-defined private class for
     * sorting the fuel stations based on their prices, and then stores them in an array list.
     *
     * mStation has field:
     *  --mLocation (LatLng) which will be used to draw the location of the station on the map,
     *  --mShortName (String) which will be shown in the customized marker
     *  --mFullName (String) which is the brand of the fuel station and will be shown in the marker's info window
     *  --mPrice (double) which is the price of the user's preferred fuel type, and will be
     *  shown in the customized marker
     *
     * @param actionBarActivity -- the activity the map will be presented
     * @param jsonArray -- -- the json array storing the points (fuel stations)
     * @return
     * i) a list of fuel stations in the format of mStation.
     * ii) there should be no error in normal cases.
     */
    private static ArrayList<mStation> getStationList(ActionBarActivity actionBarActivity,
                                   JSONArray jsonArray, String preferredFuelType) {

        ArrayList<mStation> result = new ArrayList<mStation>();
        for (int i = 0; i < jsonArray.length(); i++) {

            JSONObject station = null;
            try {
                station = jsonArray.getJSONObject(i);

                //Log.v(LOG_TAG, "Station " + i + " :" + station.toString());

                LatLng location = new LatLng(
                        station.getDouble(actionBarActivity.getString(R.string.column_latitude)),
                        station.getDouble(actionBarActivity.getString(R.string.column_longitude))
                );

                // Changed by Yu Sun on 06/04/2015
                //String stationFullName = station.getString(
                //        actionBarActivity.getString(R.string.column_station_name));
                String stationFullName = station.getString(
                        actionBarActivity.getString(R.string.column_station_brand));

                String stationShortName = station.getString(
                        actionBarActivity.getString(R.string.column_station_short_name));
                JSONArray fuelAndPriceList = station.getJSONArray(
                        actionBarActivity.getString(R.string.column_fuel_provided));

                double price = DEFAULT_PRICE_VALUE;
                for (int j = 0; j < fuelAndPriceList.length(); j++) {

                    JSONObject fuelAndPrice = fuelAndPriceList.getJSONObject(j);
                    String fuelType = fuelAndPrice.getString(actionBarActivity.getString(R.string.column_fuel));

                    if ( fuelType.equals( preferredFuelType ) ) {
                        String fuelPrice = fuelAndPrice.getString(actionBarActivity.getString(R.string.column_price));
                        price = Double.valueOf(fuelPrice);
                        break;
                    }
                }

                result.add(new mStation(location, stationFullName, stationShortName, price));

            } catch (JSONException e){
                if( station != null )
                    Log.e(LOG_TAG, "Error when parsing the json object:" + station.toString(), e);
                else
                    Log.e(LOG_TAG, "Error when getting json object form json array", e);
                return result;
            }
        }
        return result;
    }

    /**
     * Created by Nurlan Kenzhebekov and Yu Sun on 03/04/2015
     * This function determines whether the full markers of the given two stations overlap.
     * @param stationA -- the given station one
     * @param stationB -- the given station two
     * @param mMap -- the map showing the two markers
     * @return
     *      true if the two markers overlap; false otherwise.
     */
    private static boolean determineOverlap(mStation stationA, mStation stationB, GoogleMap mMap){

        int rect1Size[] = new int[2];
        int rect2Size[] = new int[2];
        rect1Size[0] = stationA.mFullMarkerIcon.getWidth();
        rect1Size[1] = stationA.mFullMarkerIcon.getHeight();
        rect2Size[0] = stationB.mFullMarkerIcon.getWidth();
        rect2Size[1] = stationB.mFullMarkerIcon.getHeight();

        Projection projection = mMap.getProjection();
        android.graphics.Point point1 = projection.toScreenLocation(stationA.mLocation);
        android.graphics.Point point2 = projection.toScreenLocation(stationB.mLocation);

        int x1 = point1.x - rect1Size[0]/2;
        int y1 = point1.y - rect1Size[1]/2;
        int x2 = point2.x - rect2Size[0]/2;
        int y2 = point2.y - rect2Size[1]/2;

        if (x1 + rect1Size[0] < x2 || x2 + rect2Size[0] < x1 || y1 + rect1Size[1]< y2 || y2 + rect2Size[1] < y1)
            return  false; // not overlap

        return true; // overlap
    }
}
