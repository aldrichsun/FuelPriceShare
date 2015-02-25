package unimelb.cis.spatialanalytics.fuelpriceshare.maps.DrawOnMap;

import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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
import java.util.Objects;

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
    public static void drawOnMap( ActionBarActivity actionBarActivity,
                                  GoogleMap mMap,
                                  JSONArray jsonArray){

        String preferredFuelType = PreferenceManager.getDefaultSharedPreferences(actionBarActivity)
                .getString(
                        actionBarActivity.getString(R.string.pref_key_preferred_fuel),
                        "Unleaded"
                );

        double minPrice = Double.MAX_VALUE;
        int minStationIndex = 0;
        Marker focusMarker = null;
        String fuelPrice = null;

        for(int i = 0; i < jsonArray.length(); i++){

            JSONObject station = null;
            try {
                station = jsonArray.getJSONObject(i);

                //Log.v(LOG_TAG, "Station " + i + " :" + station.toString());

                LatLng location = new LatLng(
                        station.getDouble( actionBarActivity.getString(R.string.column_latitude) ),
                        station.getDouble( actionBarActivity.getString(R.string.column_longitude) )
                );

                String stationName = station.getString(
                        actionBarActivity.getString(R.string.column_station_name));
                StringBuilder sb = new StringBuilder();
                JSONArray fuelAndPriceList = station.getJSONArray(
                        actionBarActivity.getString(R.string.column_fuel_provided) );

                for(int j = 0; j < fuelAndPriceList.length(); j++){

                    JSONObject fuelAndPrice = fuelAndPriceList.getJSONObject(j);
                    String fuelType = fuelAndPrice.getString(actionBarActivity.getString(R.string.column_fuel));

                     if( !fuelType.equals( preferredFuelType ) )
                        continue;

                    fuelPrice = fuelAndPrice.getString( actionBarActivity.getString(R.string.column_price) );

                    sb.append(fuelType + ": " + fuelPrice);
                }

                // Add a corresponding marker in the map
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(location)
                        .title(stationName)
                        .snippet(sb.toString())
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
                                CustomizeMapMarker.generateBitmapFromText(
                                        actionBarActivity.getApplicationContext(),
                                        sb.toString(),
                                        Color.BLUE
                                )
                        ));

                // keep a reference of marker representing the fuel station with the minimum price
                if( Double.valueOf(fuelPrice) < minPrice ) {

                    minPrice = Double.valueOf(fuelPrice);
                    focusMarker = mMap.addMarker(markerOptions);
                    minStationIndex = i;
                }
                else{
                    mMap.addMarker(markerOptions);
                }
                // We don't display the marker's info window after place it in the map
                //focusMarker.showInfoWindow();

            } catch (JSONException e) {
                if( station != null )
                    Log.e(LOG_TAG, "Error when parsing the json object:" + station.toString(), e);
                else
                    Log.e(LOG_TAG, "Error when getting json object form json array", e);
            }
        } // end for all stations

        //Log.v(LOG_TAG, "Station " + minStationIndex + " " + "has the lowest price: " + fuelPrice);

        // change the color of the marker representing the fuel station with the minimum price
        focusMarker.remove();

        MarkerOptions focusMarkerOptions = new MarkerOptions()
                .position(focusMarker.getPosition())
                .title(focusMarker.getTitle())
                .snippet(focusMarker.getSnippet())
//                            .icon(BitmapDescriptorFactory.fromBitmap(
//                                    CustomizeMapMarker.writeTextOnDrawable(
//                                            actionBarActivity,
//                                            actionBarActivity.getApplicationContext(),
//                                            //R.drawable.blue_rect,
//                                            R.drawable.rounded_rect,
//                                            focusMarker.getSnippet(),
//                                            Color.RED
//                                    )));
                          .icon(BitmapDescriptorFactory.fromBitmap(
                                  CustomizeMapMarker.generateBitmapFromText(
                                          actionBarActivity.getApplicationContext(),
                                          focusMarker.getSnippet(),
                                          Color.RED
                                  )));
        focusMarker = mMap.addMarker(focusMarkerOptions);

        //////// 14/02/2015 Yu Sun: zoom out the map for one level and move the focus to a fuel station ////////////
        int zoomLevel = determineZoomLevel(jsonArray, focusMarker.getPosition());

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(focusMarker.getPosition())
                .zoom(zoomLevel)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        ///////////////////////////////////////////////////////////////////////

    }

    /**
     * Yu Sun 21/02/2015: Given the returned stations and the station we need to move
     * the focus, this methods determines the zoom level of the map camera.
     * Currently, the implementation of this class is very straightforward. To customized
     * the camera view after the query results, modify this method at your will.
     * @param jsonArray -- the stations json array
     * @param focus -- the station we focus on
     * @return
     */
    private static int determineZoomLevel( JSONArray jsonArray, LatLng focus ){

        if(jsonArray.length() >= 5)
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
     * @param actionBarActivity -- the activity the map will be presented
     * @param mMap -- the map we draw markers on
     * @param jsonArray -- the json array storing the points (fuel stations)
     */
    public static void drawOnMapMaxTenDifferentColor( ActionBarActivity actionBarActivity,
                                                      GoogleMap mMap,
                                                      JSONArray jsonArray,
                                                      LatLng focusLoc
    ) {

        ArrayList<mStation> stationList = getStationList(actionBarActivity, jsonArray);
        Collections.sort(stationList);

        for (int i = 0; i < stationList.size() && i < maxNumberOfDrawnStations; i++) {

            mStation station = stationList.get(i);
            // Add a corresponding marker in the map
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(station.mLocation)
                    .title(station.mName)
                    .snippet(station.mName + ": " + station.mPrice)
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
                            CustomizeMapMarker.generateBitmapFromText(
                                    actionBarActivity.getApplicationContext(),
                                    station.mName + ": " + station.mPrice,
                                    Color.parseColor(mColors[i])
                            )
                    ));
            mMap.addMarker(markerOptions);
        }
        // end for all stations

        //////// 14/02/2015 Yu Sun: zoom out the map for one level and move the focus to a fuel station ////////////
        int zoomLevel = determineZoomLevel(jsonArray, focusLoc);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(focusLoc)
                .zoom(zoomLevel)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        ///////////////////////////////////////////////////////////////////////
    }

    private static class mStation implements Comparable<mStation>{

        public LatLng mLocation;     //The location of the station
        public String mName;  //The name of the station
        public double mPrice;    // the price of the user preferred fuel type

        public mStation(LatLng mLocation, String mName, double mPrice){
            this.mLocation = mLocation;
            this.mName = mName;
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
     * TODO add comments
     * @param actionBarActivity
     * @param jsonArray
     * @return
     */
    private static ArrayList<mStation> getStationList(ActionBarActivity actionBarActivity,
                                   JSONArray jsonArray) {

        String preferredFuelType = PreferenceManager.getDefaultSharedPreferences(actionBarActivity)
                .getString(
                        actionBarActivity.getString(R.string.pref_key_preferred_fuel),
                        "Unleaded"
                );

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

                String stationName = station.getString(
                        actionBarActivity.getString(R.string.column_station_name));
                JSONArray fuelAndPriceList = station.getJSONArray(
                        actionBarActivity.getString(R.string.column_fuel_provided));

                double price = 0.0;
                for (int j = 0; j < fuelAndPriceList.length(); j++) {

                    JSONObject fuelAndPrice = fuelAndPriceList.getJSONObject(j);
                    String fuelType = fuelAndPrice.getString(actionBarActivity.getString(R.string.column_fuel));

                    if ( fuelType.equals(preferredFuelType) ) {
                        String fuelPrice = fuelAndPrice.getString(actionBarActivity.getString(R.string.column_price));
                        price = Double.valueOf(fuelPrice);
                        break;
                    }
                }

                result.add(new mStation(location, stationName, price));

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
}
