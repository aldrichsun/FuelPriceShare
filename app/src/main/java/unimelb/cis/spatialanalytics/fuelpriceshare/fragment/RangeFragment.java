package unimelb.cis.spatialanalytics.fuelpriceshare.fragment;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import unimelb.cis.spatialanalytics.fuelpriceshare.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import unimelb.cis.spatialanalytics.fuelpriceshare.maps.autoComplete.AutoCompleteAdapter;
import unimelb.cis.spatialanalytics.fuelpriceshare.maps.query.RangeQuery;

/**
 * Created by Yu Sun on 17/02/2015.
 */
public class RangeFragment extends Fragment {

    private static final String LOG_TAG = RangeFragment.class.getSimpleName();

//    static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LatLng latLng;  // The current map focus location.
    private String addressText; // The current map focus address text;

    private SupportMapFragment mMapFragment;

    public RangeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_range, container, false);
    }

    // TODO re-organize the code
    // http://developer.android.com/about/versions/android-4.2.html#NestedFragments
    // https://code.google.com/p/gmaps-api-issues/issues/detail?id=5064#c1
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if(status == ConnectionResult.SUCCESS)
        {
            FragmentManager fm = getChildFragmentManager();
            mMapFragment = (SupportMapFragment) fm.findFragmentById(R.id.range_map);
            if (mMapFragment == null) {
                mMapFragment = SupportMapFragment.newInstance();
                fm.beginTransaction().replace(R.id.range_map, mMapFragment).commit();

                setUpMap();
            }
        }
        else{
            //TODO show more informative message by detecting various errors
            Log.e(LOG_TAG, "Google Play service is not enabled on this device.");
            Toast toast = Toast.makeText(getActivity(), "Google Play service is not enabled on this device.",
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();
            getActivity().finish();
        }
//        else if(status == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED){
//
//            Log.e(LOG_TAG, "You need to update Google Play Services in order to view maps");
//            Toast.makeText(getActivity(), "You need to update Google Play Services in order to view maps",
//                    Toast.LENGTH_LONG).show();
//        }
//        else if (status==ConnectionResult.SERVICE_MISSING){
//
//            Log.e(LOG_TAG, "Google Play service is not enabled on this device.");
//            Toast.makeText(getActivity(), "Google Play service is not enabled on this device.",
//                    Toast.LENGTH_LONG).show();
//        }
//        else if(status == ConnectionResult.SERVICE_INVALID){
//
//            Log.e(LOG_TAG, "The version of the Google Play services installed on this device is not authentic.");
//
//            Toast.makeText(getActivity(), "The version of the Google Play services installed on this device is not authentic.",
//                    Toast.LENGTH_LONG).show();
//
//            GooglePlayServicesUtil.getErrorDialog(status, getActivity(),
//                    REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
//        }
    }

    // TODO re-organize the code
    @Override
    public void onResume() {
        super.onResume();

        if (mMap == null) {

            int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
            if(status == ConnectionResult.SUCCESS) {
                mMap = mMapFragment.getMap();
                setUpMap();
            }
            else{
                Log.e(LOG_TAG, "Google Play service is not enabled on this device.");
                Toast toast = Toast.makeText(getActivity(), "Google Play service is not enabled on this device.",
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
                getActivity().finish();
            }
            //TODO show more informative message by detecting various errors
//            else if(status == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED){
//
//                Log.e(LOG_TAG, "You need to update Google Play Services in order to view maps");
//                Toast.makeText(getActivity(), "You need to update Google Play Services in order to view maps",
//                        Toast.LENGTH_LONG).show();
////                getActivity().finish();
//            }
//            else if (status == ConnectionResult.SERVICE_MISSING){
//
//                Log.e(LOG_TAG, "Google Play service is not enabled on this device.");
//                Toast.makeText(getActivity(), "Google Play service is not enabled on this device.",
//                        Toast.LENGTH_LONG).show();
////                getActivity().finish();
//
////                GooglePlayServicesUtil.getErrorDialog(status, getActivity(),
////                        RQS_GooglePlayServices).show();
//            }
//            else if(status == ConnectionResult.SERVICE_INVALID){
//
//                Log.e(LOG_TAG, "The version of the Google Play services installed on this device is not authentic.");
//
//                Toast.makeText(getActivity(), "The version of the Google Play services installed on this device is not authentic.",
//                        Toast.LENGTH_LONG).show();
//
//                GooglePlayServicesUtil.getErrorDialog(status, getActivity(),
//                        REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
////                getActivity().finish();
//            }
        }
    }


    /**
     * // TODO re-organize the code
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        /////////////// Set up the initial focus of the map ////////////////
        latLng = new LatLng(-37.7963, 144.9614); // "Melbourne Uni"

        ////mMap.addMarker(new MarkerOptions().position(latLng).title("Melbourne Uni"));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(14)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        ////////////////////////////////////////////////////////////////////////

        ///////////////////// Set up view adapters and event listeners ////////////////////////
        ///// Set the auto complete text view adapter //////
        // Get reference to the auto complete text view
        final AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView)
                getActivity().findViewById(R.id.auto_complete_text_view);
        // Set the auto complete text view adapter (which maps the output content to the text view)
        autoCompleteTextView.setAdapter(new AutoCompleteAdapter(
                getActivity(), R.layout.list_item));
        ///////////////////////////////////////////////////
        ///////// The AutoComplete listener ////////////
        // Set the auto complete text view item click listener
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            // Once the item is clicked, we i) close the input pad ii) geocode the input address
            // iii) add a marker on the map and move the focus to the marker
            // Tasks ii) and iii) is completed by GeocoderTask
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get user input location
                String location = (String) parent.getItemAtPosition(position);

                // Close the soft keypad
                InputMethodManager imm = (InputMethodManager)
                        getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(autoCompleteTextView.getWindowToken(), 0);

                // Geocode the address and show the corresponding marker on the map
                if( location != null && !location.equals("") ){
                    new GeocoderTask().execute(location);
                }
                // Show the auto complete address with a Toast at the bottom of the screen
                Toast.makeText(getActivity(), location, Toast.LENGTH_SHORT).show();
            }
        });
        //////////////////////////////////////////////////////

        ///////////////// Set the 'Find' button listener /////////////////////
        // Get reference to btn_find of the layout activity_main
        Button buttonFind = (Button) getActivity().findViewById(R.id.locate_address_button);
        // Define button click event listener for the find button
        View.OnClickListener findClickListener = new View.OnClickListener() {

            // Once the button is clicked, we i) close the input pad ii) geocode the input address
            // iii) add a marker on the map and move focus to the marker
            // Tasks ii) and iii) is completed by GeocoderTask
            @Override
            public void onClick(View v) {

                // Get reference to the auto complete text view to get the user input location
                AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView)
                        getActivity().findViewById(R.id.auto_complete_text_view);
                // Get the auto complete location
                String location = autoCompleteTextView.getText().toString();

                // Close the soft keypad
                InputMethodManager imm = (InputMethodManager)
                        getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(autoCompleteTextView.getWindowToken(), 0);

                if( location != null && !location.equals("") ){
                    new GeocoderTask().execute(location);
                }
            }
        };
        // Setting button click event listener for the find button
        buttonFind.setOnClickListener(findClickListener);
        ////////////////////////////////////////////////////////////////////

        ///////////// Set the range query button listener ////////////
        // Get a reference to the button
        Button rangeQueryButton = (Button) getActivity().findViewById(R.id.range_query_button);
        // Set the listener
        rangeQueryButton.setOnClickListener(new View.OnClickListener() {

            // After the button is clicked, we do the following three tasks:
            // i) use the instance variable latLng in this class, which represents the searched
            // address the user input, as the query point, and use the user preferred range distance
            // to issue the range query; ii) draw the returned points on the map with customized
            // marker icon
            @Override
            public void onClick(View v) {

                // The search location is now stored in the private instance variable latLng
                if( latLng != null ){
                    // We use the location represented by latLng to issue a range query
                    // and then display the returned results on the map
                    new RangeQueryTask().execute(latLng);
                }
            }
        });
        //////////////////////////////////////////////////////////////////////

        ///////////// Set the marker click events listener //////////////////
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            // After the marker is clicked, we display the marker info window's
            // title and snippet at the bottom of the map. Currently, the title
            // is the name of the fuel station, and the snippet is all the types
            // of fuel and price sold by the station.
            // We also show the info window anchored at the clicked marker.
            @Override
            public boolean onMarkerClick(Marker marker) {

                //get a reference to the text view
                TextView textView = (TextView) getActivity().findViewById(R.id.map_bottom_text_view);
                if( marker.getSnippet() != null )
                    textView.setText(marker.getTitle() + "\n" + marker.getSnippet());
                else
                    textView.setText(marker.getTitle());
                marker.showInfoWindow();
                return true;
            }
        });
        ////////////////////////////////////////////////////////////////////////

        /////////////// Set the path query button listener //////////////////
//        Button pathQueryJumpButton = (Button) getActivity().findViewById(R.id.path_query_jump_button);
//        pathQueryJumpButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                // Get reference to the auto complete text view to get the user input location
//                AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView)
//                        getActivity().findViewById(R.id.auto_complete_text_view);
//                // Get the auto complete location
//                String address = autoCompleteTextView.getText().toString();
//
//                Intent displayPathIntent = null;
//                displayPathIntent = new Intent(getActivity(), PathActivity.class);
//                displayPathIntent.putExtra(
//                        getString(R.string.intent_pathActivity_text), // key
//                        latLng.latitude+"##"+latLng.longitude+"##"+address
//                );
//                startActivity(displayPathIntent);
//            }
//        });
        /////////////////////////////////////////////////////////////////////////
    }


    /**
     * The function of the task is as follows:
     * i) geocode the input address, and
     * ii) add a marker corresponding to the input addrss on the map
     * and move the focus of the map to the marker, and store the marker
     * location in the private instance variable latLng
     */
    private class GeocoderTask extends AsyncTask<String, Void, List<Address>> {

        @Override
        protected List<Address> doInBackground(String... locationName) {
            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder( getActivity() );
            List<Address> addresses = null;

            try {
                Log.v(LOG_TAG, "The geocoding address is: " + locationName[0]);
                // Getting a maximum of 3 Address that matches the input text
                addresses = geocoder.getFromLocationName(locationName[0], 3);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error when doing geocoding on: " + locationName[0], e);
            }
            return addresses;
        }

        // After the geocoding, we add a marker representing the address on the map
        // and move the focus to the marker, and display the address of the marker
        // in the text view at the bottom of the map
        @Override
        protected void onPostExecute(List<Address> addresses) {

            // 06/02/2015 Yu Sun: It is wrong that we do NOT distinguish null and empty!
            if( addresses == null || addresses.size() == 0){
                Toast toast = Toast.makeText(
                        getActivity(), "Wrong address, please try another one",
                        Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }

            // Clears all the existing markers on the map
            mMap.clear();

            // Add Markers on Google Map for each matching address
            // Yu Sun 06/02/2015: This is not what we expected
            //for(int i=0; i < addresses.size(); i++){
            for(int i = 0; i < 1; i++){ // Yu Sun 06/02/2015: we only use the first one

                Address address = (Address) addresses.get(i);

                // Create an instance of point and display it on Google Map
                latLng = new LatLng(address.getLatitude(), address.getLongitude());

                addressText = String.format("%s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getCountryName());

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(addressText);

                mMap.addMarker(markerOptions);

                // Move the focus of the map to the marker's location
                if(i==0) {
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(latLng)
                            .zoom(14)
                                    //.bearing(70)
                                    //.tilt(25)
                            .build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }

                //06/02/2015 Yu Sun: since there is only one address, we display it in the
                //text view under the map
                StringBuilder sb = new StringBuilder();
                for(int j = 0; j < address.getMaxAddressLineIndex(); j++){
                    sb.append(address.getAddressLine(j).toString() + ' ');
                }
                String detailAddress = sb.toString();

                //get a reference to the text view
                TextView textView = (TextView)
                        getActivity().findViewById(R.id.map_bottom_text_view);
                textView.setText(detailAddress);

            }
        }
    }

    /**
     * The function of the class is as follows:
     * i) use the instance variable latLng in this class, which represents the searched
     * address the user input, as the query point, and use the user preferred range distance
     * to issue the range query; ii) draw the returned points on the map with customized
     * marker icon
     */
    private class RangeQueryTask extends AsyncTask<LatLng, Void, JSONArray>{

        // The range query search distance
        private double range_radius = 3.0;

        /**
         * 16/02/2015 Yu Sun:
         * Before doing the query, this function first retrieves the user preferred range distance
         * from the shared preference.
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // 16/02/2015 Yu Sun: get the users preferred radius
            String range_radius = PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getString(
                            getString(R.string.pref_key_range_distance),
                            getString(R.string.pref_default_range_distance)
                    );
            this.range_radius = Double.valueOf(range_radius);
        }

        // Use the instance variable latLng in this class, which represents the searched
        // address the user input, as the query point, and use the user preferred range
        // distance to issue the range query
        @Override
        protected JSONArray doInBackground(LatLng... params) {

            //Log.v(LOG_TAG, "Starting the range query...");
            RangeQuery rangeQuery = new RangeQuery();
            JSONArray stations = rangeQuery.executeQuery(params[0],
                    Double.valueOf(range_radius));
            //Log.v(LOG_TAG, "The results are: " + stations.toString());
            return stations;
        }

        // Draw the returned points on the map with customized marker icon
        @Override
        protected void onPostExecute(JSONArray jsonArray) {

            if( jsonArray == null ){
                Toast toast = Toast.makeText(getActivity(), "Server request errors," +
                        " please check the internet and try later", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }
            if( jsonArray.length() <= 0 ){
                Toast toast = Toast.makeText(getActivity(), "No stations within " +
                        this.range_radius + "km, " +
                        "please increase the radius", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }

            // Clears all the existing markers on the map
            mMap.clear();

            // re-add the marker for the current focus of the map
            MarkerOptions current_markerOptions = new MarkerOptions();
            current_markerOptions.position(latLng);
            current_markerOptions.title(addressText);
            Marker focus_marker = mMap.addMarker(current_markerOptions);

            //Else show all the station markers
            Marker marker = focus_marker;
            for(int i = 0; i < jsonArray.length(); i++){

                // TODO parse the json object, better use a separated method
                JSONObject station = null;
                try {
                    station = jsonArray.getJSONObject(i);

                    Log.v(LOG_TAG, "Station " + i + " :" + station.toString());

                    LatLng location = new LatLng(
                            station.getDouble( getString(R.string.column_latitude) ),
                            station.getDouble( getString(R.string.column_longitude) )
                    );

                    String stationName = station.getString(getString(R.string.column_station_name));
                    StringBuilder sb = new StringBuilder();
                    JSONArray fuelAndPriceList = station.getJSONArray( getString(R.string.column_fuel_provided) );
                    for(int j = 0; j < fuelAndPriceList.length(); j++){
                        JSONObject fuelAndPrice = fuelAndPriceList.getJSONObject(j);
                        sb.append(
                                fuelAndPrice.getString( getString(R.string.column_fuel) ) + ": " +
                                        fuelAndPrice.getString( getString(R.string.column_price) ) + " "
                        );
                    }

                    // Add a corresponding marker in the map
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(location)
                            .title(stationName)
                            .snippet(sb.toString())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.fuel_icon));
                    // We display the marker's info window after place it in the map
                    marker = mMap.addMarker(markerOptions);
                    marker.showInfoWindow();

                } catch (JSONException e) {
                    if( station != null )
                        Log.e(LOG_TAG, "Error when parsing the json object:" + station.toString(), e);
                    else
                        Log.e(LOG_TAG, "Error when getting json object form json array", e);
                }
            } // end for all stations

            //////// 14/02/2015 Yu Sun: zoom out the map for one level and move the focus to a fuel station ////////////
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(marker.getPosition())
                    .zoom(13)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            ///////////////////////////////////////////////////////////////////////
        } // end post execute
    }// end range query task

}
