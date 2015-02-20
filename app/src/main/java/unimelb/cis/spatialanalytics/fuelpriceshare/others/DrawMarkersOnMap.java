package unimelb.cis.spatialanalytics.fuelpriceshare.others;

import android.graphics.Color;
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

import unimelb.cis.spatialanalytics.fuelpriceshare.R;

/**
 * Created by Yu Sun on 20/02/2015.
 */
public class DrawMarkersOnMap {

    private static final String LOG_TAG = DrawMarkersOnMap.class.getSimpleName();

    /**
     * Created by Yu Sun on 21/02/2015
     * @param actionBarActivity
     * @param mMap
     * @param jsonArray
     * @param preferredFuelType
     */
    public static void drawOnMap( ActionBarActivity actionBarActivity,
                                  GoogleMap mMap,
                                  JSONArray jsonArray,
                                  String preferredFuelType){

        double minPrice = Double.MAX_VALUE;
        int minStationIndex = 0;
        Marker focusMarker = null;
        String fuelPrice = null;

        for(int i = 0; i < jsonArray.length(); i++){

            JSONObject station = null;
            try {
                station = jsonArray.getJSONObject(i);

                Log.v(LOG_TAG, "Station " + i + " :" + station.toString());

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
//                                            (android.support.v7.app.ActionBarActivity) getActivity(),
//                                            getActivity().getApplicationContext(),
//                                            //R.drawable.blue_rect,
//                                            R.drawable.rounded_rect,
//                                            sb.toString(),
//                                            Color.RED
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

        Log.v(LOG_TAG, "Station " + minStationIndex + " " + "has the lowest price: " + fuelPrice);

        // change the color of the marker representing the fuel station with the minimum price
        focusMarker.remove();

        MarkerOptions focusMarkerOptions = new MarkerOptions()
                .position(focusMarker.getPosition())
                .title(focusMarker.getTitle())
                .snippet(focusMarker.getSnippet())
//                            .icon(BitmapDescriptorFactory.fromBitmap(
//                                    CustomizeMapMarker.writeTextOnDrawable(
//                                            (android.support.v7.app.ActionBarActivity) getActivity(),
//                                            getActivity().getApplicationContext(),
//                                            //R.drawable.blue_rect,
//                                            R.drawable.rounded_rect,
//                                            sb.toString(),
//                                            Color.RED
//                                    )));
                .icon(BitmapDescriptorFactory.fromBitmap(
                        CustomizeMapMarker.generateBitmapFromText(
                                actionBarActivity.getApplicationContext(),
                                focusMarker.getSnippet(),
                                Color.RED
                        )
                ));
        focusMarker = mMap.addMarker(focusMarkerOptions);

        //////// 14/02/2015 Yu Sun: zoom out the map for one level and move the focus to a fuel station ////////////
        int zoomLevel = determineZoomLevel(jsonArray, focusMarker);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(focusMarker.getPosition())
                .zoom(zoomLevel)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        ///////////////////////////////////////////////////////////////////////

    }

    /**
     * TODO add comments
     * @param jsonArray
     * @param focusMarker
     * @return
     */
    private static int determineZoomLevel( JSONArray jsonArray, Marker focusMarker ){

        if(jsonArray.length() > 10)
            return 16;
        //else
        return 15;
    }
}
