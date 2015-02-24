package unimelb.cis.spatialanalytics.fuelpriceshare.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Path;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
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
import java.util.ArrayList;
import java.util.List;

import unimelb.cis.spatialanalytics.fuelpriceshare.maps.autoComplete.AutoCompleteAdapter;
import unimelb.cis.spatialanalytics.fuelpriceshare.maps.query.PathQuery;
import unimelb.cis.spatialanalytics.fuelpriceshare.maps.query.RangeQuery;
import unimelb.cis.spatialanalytics.fuelpriceshare.others.CustomizeMapMarker;
import unimelb.cis.spatialanalytics.fuelpriceshare.others.DecodeDirection;
import unimelb.cis.spatialanalytics.fuelpriceshare.others.DrawMarkersOnMap;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.software.shell.fab.ActionButton;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import com.gc.materialdesign.views.ButtonRectangle;

/**
 * Created by Yu Sun on 17/02/2015.
 * Yu Sun 23/02/2015: Currently, we separated the RangeFragment and PathFragment, each of which
 * use their own map fragment. Later, one direction for improvement is that we use only one (small) map
 * fragment, and dynamically hide and show (small) fragments corresponding to functions in the
 * current RangeFragment and PathFragment with the one map fragment. The logic for the dynamically
 * break and combine (small) fragments might take a long time to implement. Therefore, we leave it
 * for the next improved version. A byproduct in that version is we can use the PreferenceFragment
 * instead of PreferenceActivity.
 */
public class RangeFragment extends Fragment {

    private static final String LOG_TAG = RangeFragment.class.getSimpleName();

//    static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LatLng latLng;  // The map focus location.
    private LatLng currentLocation; // The user's current location.
    private LatLng fuelStationClickedLatLng; // The location of the station clicked by the user.
    private String addressText; // The current map focus address text.
    private String currentLocationName; // "Your location" R.string.Your_location
    private String fuelStationClickedName; // The name of the station clicked by the user.

    private SupportMapFragment mMapFragment;
    private SlidingUpPanelLayout mLayout;
    private ActionButton rangeQueryButton;
    private ActionButton directionJumpButton;


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

                if( mMap != null) {
                    setUpMap();
                    setUpSlidingPanel();
                }
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

        /////////////// Set up the initial focus of the map, which is the user current location ////////////////
        /////// TODO implement onMyLocationChangeListener
        mMap.setMyLocationEnabled(true);
        // Get LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager)
                getActivity().getSystemService(Context.LOCATION_SERVICE);
        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();
        // Get the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);
        // Get Current Location
        Location myLocation = locationManager.getLastKnownLocation(provider);
        if( myLocation != null ){
            currentLocationName = getString(R.string.Your_location);
            currentLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        }
        else {
            latLng = new LatLng(-37.7963, 144.9614); // Melbourne Uni
        }

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
        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView)
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
                AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView)
                        getActivity().findViewById(R.id.auto_complete_text_view);
                InputMethodManager imm = (InputMethodManager)
                        getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(autoCompleteTextView.getWindowToken(), 0);

                // Geocode the address and show the corresponding marker on the map
                if( location != null && !location.equals("") ){
                    new GeocoderTask().execute(location);
                }
                // Show the auto complete address with a Toast at the bottom of the screen
                // Toast.makeText(getActivity(), location, Toast.LENGTH_SHORT).show();

                if( (!directionJumpButton.isHidden()) && rangeQueryButton.isHidden() ){
                    directionJumpButton.setHideAnimation(ActionButton.Animations.JUMP_TO_DOWN);
                    directionJumpButton.hide();
                    rangeQueryButton.setShowAnimation(ActionButton.Animations.JUMP_FROM_RIGHT);
                    rangeQueryButton.show();
                }

            }
        });
        //////////////////////////////////////////////////////

        ///////////////// Set the 'Find' button listener /////////////////////
        // Get reference to btn_find of the layout activity_main
        ButtonRectangle buttonFind = (ButtonRectangle) getActivity().findViewById(R.id.locate_address_button);
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

                if( (!directionJumpButton.isHidden()) && rangeQueryButton.isHidden() ){
                    directionJumpButton.setHideAnimation(ActionButton.Animations.JUMP_TO_DOWN);
                    directionJumpButton.hide();
                    rangeQueryButton.setShowAnimation(ActionButton.Animations.JUMP_FROM_RIGHT);
                    rangeQueryButton.show();
                }
            }
        };
        // Setting button click event listener for the find button
        buttonFind.setOnClickListener(findClickListener);
        ////////////////////////////////////////////////////////////////////

        ///////////// Set the range query (floating action) button listener ////////////
        // Get a reference to the button
        // 24/02/2015 Yu Sun: Change to floating action buttion
        rangeQueryButton = (ActionButton) getActivity().findViewById(R.id.range_query_button);
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
                    // TODO show the progress bar and disable the button
                    new RangeQueryTask().execute(latLng);
                }
            }
        });
        //////////////////////////////////////////////////////////////////////

        ///////////// Set the marker click events listener //////////////////
        // 24/02/2015 deleted by Yu Sun
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            // After the marker is clicked, we display the marker info window's
            // title and snippet at the bottom of the map. Currently, the title
            // is the name of the fuel station, and the snippet is all the types
            // of fuel and price sold by the station.
            // We also show the info window anchored at the clicked marker.
            @Override
            public boolean onMarkerClick(Marker marker) {

                //get a reference to the text view
//                TextView textView = (TextView) getActivity().findViewById(R.id.map_bottom_text_view);
//                if( marker.getSnippet() != null )
//                    textView.setText(marker.getTitle() + "\n" + marker.getSnippet());
//                else
//                    textView.setText(marker.getTitle());
                marker.showInfoWindow();
                rangeQueryButton.setHideAnimation(ActionButton.Animations.JUMP_TO_DOWN);
                rangeQueryButton.hide();
                directionJumpButton.setShowAnimation(ActionButton.Animations.JUMP_FROM_RIGHT);
                directionJumpButton.show();

                AutoCompleteTextView destinationText = (AutoCompleteTextView)
                        getActivity().findViewById(R.id.direction_destination_autoCompleteTextView);
                fuelStationClickedName = marker.getTitle();
                destinationText.setText(fuelStationClickedName);
                fuelStationClickedLatLng = new LatLng(
                        marker.getPosition().latitude, marker.getPosition().longitude);

                return true;
            }
        });
        ////////////////////////////////////////////////////////////////////////

        /////////////// Set the direction jump button listener //////////////////
        directionJumpButton = (ActionButton) getActivity().findViewById(R.id.direction_button);
        directionJumpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                mLayout.setAnchorPoint(0.7f);
                mLayout.setPanelState(PanelState.ANCHORED);

                AutoCompleteTextView originText = (AutoCompleteTextView)
                        getActivity().findViewById(R.id.direction_origin_autoCompleteTextView);
                originText.setText( getString(R.string.Your_location) );

            }
        });
        directionJumpButton.hide();
        /////////////////////////////////////////////////////////////////////////
    }

    public void setUpSlidingPanel(){

        //Initialize the sliding up panel
        mLayout = (SlidingUpPanelLayout) getActivity().findViewById(R.id.sliding_layout);
        mLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                //Log.i(LOG_TAG, "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelExpanded(View panel) {
                directionJumpButton.setHideAnimation(ActionButton.Animations.FADE_OUT);
                directionJumpButton.hide();
            }

            @Override
            public void onPanelCollapsed(View panel) {
                directionJumpButton.setShowAnimation(ActionButton.Animations.FADE_IN);
                directionJumpButton.show();
            }

            @Override
            public void onPanelAnchored(View panel) {
                directionJumpButton.setHideAnimation(ActionButton.Animations.FADE_OUT);
                directionJumpButton.hide();
            }

            @Override
            public void onPanelHidden(View panel) {
                directionJumpButton.setShowAnimation(ActionButton.Animations.FADE_IN);
                directionJumpButton.show();
            }
        });
        //////// Set up auto complete text view and search button //////////////
        // get the references of the auto complete text view
        AutoCompleteTextView origin_textView = (AutoCompleteTextView)
                getActivity().findViewById(R.id.direction_origin_autoCompleteTextView);
        AutoCompleteTextView destin_textView = (AutoCompleteTextView)
                getActivity().findViewById(R.id.direction_destination_autoCompleteTextView);

        origin_textView.setAdapter(new AutoCompleteAdapter(getActivity(), R.layout.list_item));
        origin_textView.setOnItemClickListener(new directionAutoCompleteOnItemClickListener(
                origin_textView, destin_textView, true));

        destin_textView.setAdapter(new AutoCompleteAdapter(getActivity(), R.layout.list_item));
        destin_textView.setOnItemClickListener(new directionAutoCompleteOnItemClickListener(
                origin_textView, destin_textView, false));
        /////////////////////////////////////////////////////////

        /////////// Set the route button listener //////////////
        ButtonRectangle pathQueryButton = (ButtonRectangle) getActivity().findViewById(R.id.obtain_direction_button);
        pathQueryButton.setOnClickListener(new View.OnClickListener() {

            /**
             * TODO add comments
             * @param v
             */
            @Override
            public void onClick(View v) {
                AutoCompleteTextView origin_textView = (AutoCompleteTextView)
                        getActivity().findViewById(R.id.direction_origin_autoCompleteTextView);
                AutoCompleteTextView destin_textView = (AutoCompleteTextView)
                        getActivity().findViewById(R.id.direction_destination_autoCompleteTextView);
                String origin = origin_textView.getText().toString();
                String destination = destin_textView.getText().toString();

                // close the soft key pad
                InputMethodManager imm = (InputMethodManager)
                        getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(origin_textView.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(destin_textView.getWindowToken(), 0);

                // Hide the sliding up panel
                mLayout.setPanelState(PanelState.HIDDEN);

                // Call the PathQueryTask
                DirectionTask directionTask = new DirectionTask();
                directionTask.execute(origin, destination); //Note the order of the params
            }
        });

    }


    //////////////////////////////// AsyncTasks ////////////////////////////////////////
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

                // 24/02/2015 deleted by Yu Sun
                // 06/02/2015 Yu Sun: since there is only one address, we display it in the
                //text view under the map
//                StringBuilder sb = new StringBuilder();
//                for(int j = 0; j < address.getMaxAddressLineIndex(); j++){
//                    sb.append(address.getAddressLine(j).toString() + ' ');
//                }
//                String detailAddress = sb.toString();

//                //get a reference to the text view
//                TextView textView = (TextView)
//                        getActivity().findViewById(R.id.map_bottom_text_view);
//                textView.setText(detailAddress);

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
        ProgressDialog progressDialog;

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
            // progress dialog
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Retrieving fuel stations...");
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
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
            mMap.addMarker(current_markerOptions);

            //Else show all the station markers
            DrawMarkersOnMap.drawOnMap(
                    (android.support.v7.app.ActionBarActivity) getActivity(),
                    mMap,
                    jsonArray
            );
            progressDialog.dismiss();

        } // end post execute
    }// end range query task


    /**
     *  Once the item is clicked, we close the soft keypad, check the origin_ and
     *  destination_autoCompleteTextView, if both are not empty, we call the PathQueryTask
     *  to geocode the origin and destination addresses, compute the direction (or called
     *  path or route) between the origin and destination.
     */
    private class directionAutoCompleteOnItemClickListener implements AdapterView.OnItemClickListener {

        AutoCompleteTextView origin_autoCompleteTextView;
        AutoCompleteTextView destin_autoCompleteTextView;
        boolean clickFromOriginTextView;

        /**
         * Constructor of the listener.
         * @param origin_autoCompleteTextView -- text view reference for the origin destination
         * @param destin_autoCompleteTextView -- text view reference for the destination destination
         * @param clickFromOriginTextView -- true if the click is from the origin text view, false if
         *                                the click is from the destination text view
         */
        public directionAutoCompleteOnItemClickListener(
                AutoCompleteTextView origin_autoCompleteTextView,
                AutoCompleteTextView destin_autoCompleteTextView,
                boolean clickFromOriginTextView){
            this.origin_autoCompleteTextView = origin_autoCompleteTextView;
            this.destin_autoCompleteTextView = destin_autoCompleteTextView;
            this.clickFromOriginTextView = clickFromOriginTextView;
        }

        // Once the item is clicked, we close the soft keypad, check the origin_ and
        // destination_autoCompleteTextView, if both are not empty, we call the PathQueryTask
        // to geocode the origin and destination addresses, compute the direction (or called
        // path or route) between the origin and destination and the fuel stations near to the path.
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            // Get user input location
            String location = (String) parent.getItemAtPosition(position);

            // Close the soft keypad (no matter whether the %location is empty or not)
            InputMethodManager imm = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if(this.clickFromOriginTextView)
                imm.hideSoftInputFromWindow(origin_autoCompleteTextView.getWindowToken(), 0);
            else
                imm.hideSoftInputFromWindow(destin_autoCompleteTextView.getWindowToken(), 0);

            // Geocode the address and show the corresponding marker on the map
            if( location == null || location.isEmpty() )
                return;

            String origin;
            String destination;

            // check the address in the origin_ or destination_autoCompleteTextView
            if(this.clickFromOriginTextView) {
                origin = location;
                destination = this.destin_autoCompleteTextView.getText().toString();
                if( destination == null || destination.isEmpty() )
                    return;
            }
            else{
                origin = this.origin_autoCompleteTextView.getText().toString();
                if( origin == null || origin.isEmpty() )
                    return;
                destination = location;
            }

            // Hide the sliding up panel
            mLayout.setPanelState(PanelState.HIDDEN);

            // Call the DirectionTask
            DirectionTask directionTask = new DirectionTask();
            directionTask.execute(origin, destination); //Note the order of the params
        }
    }

    /**
     * The function of the task is as follows:
     * i) geocode the origin and destination addresses,
     * ii) compute the direction (or called path or route) between the origin
     * and destination
     * iii) draw the path on the map
     * and iv) move the focus of the map to the path.
     * We choose to CHECK the address in the origin_ and destination_autoCompleteTextView
     * each time as 1) it's difficult to keep track of a LatLng object for the origin
     * or destination since the user behaviour is unpredictable and 2) it's easier later for
     * us to move the geocode task to the server.
     */
    private class DirectionTask extends AsyncTask<String, Void, JSONObject> {

        private static final String RESULT_DIRECTION_KEY = "direction";
        private static final String ERROR = "error";
        private static final String ORIGIN_ERROR_KEY = "origin_geocoding";
        private static final String DESTIN_ERROR_KEY = "destin_geocoding";

        /**
         * Before retrieving the directions, we display a message telling the user to wait,
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Show the auto complete address with a Toast at the bottom of the screen
            Toast toast = Toast.makeText(getActivity(), "Retrieving the route...",
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }

        /**
         * The first string is the origin and the second is the destination
         *
         * @param originAndDestination -- the first string is the origin and the second is the destination
         * @return i) A Json object with 1) the name of "direction" and value of the response
         * string of google directions API call.
         * ii) If there are errors in the origin/destination geoCoding, there will be a field with the name
         * of "origin/destin_geocoding" and value of "error" (string)
         * iii) If there are errors in any other field, the corresponding value would also be "error".
         * iv) If there are no direction result, the corresponding value would be "empty".
         * <p/>
         * One example result is {"direction":"error"}
         */
        @Override
        protected JSONObject doInBackground(String... originAndDestination) {

            JSONObject result = null;
            JSONObject errorObj = new JSONObject();

            String origin_addr = originAndDestination[0];
            String destin_addr = originAndDestination[1];

            double o_lat, o_lng, d_lat, d_lng;

            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(getActivity());

            try {
                /////// If any error occurs, we don't do the path query and return ///////
                /////// the error object directly ////////////////////////////////////////
                //////////////// For origin /////////////////
                if( origin_addr.equals( currentLocationName ) ){
                    if( currentLocation == null ){
                        try {
                            errorObj.put(this.ORIGIN_ERROR_KEY, this.ERROR);
                        } catch (JSONException e) {
                        }
                        return errorObj;
                    }
                    //else
                    o_lat = currentLocation.latitude;
                    o_lng = currentLocation.longitude;
                }
                else {
                    Log.v(LOG_TAG, "Geocoding the origin...");
                    List<Address> addresses_o = geocoder.getFromLocationName(origin_addr, 1);
                    if (addresses_o == null || addresses_o.size() == 0) {
                    /*13/03/2015 Yu Sun: this will cause Looper.prepare() problems
                    So we just indicate error in the onPostExecute() */
                        //Toast.makeText(getBaseContext(),
                        //        "Wrong origin address, please try another one",
                        //        Toast.LENGTH_SHORT).show();
                        try {
                            errorObj.put(this.ORIGIN_ERROR_KEY, this.ERROR);
                        } catch (JSONException e) {
                        }
                        return errorObj;
                    }
                    //else
                    o_lat = addresses_o.get(0).getLatitude();
                    o_lng = addresses_o.get(0).getLongitude();
                }
                //////////////// For destination /////////////////
                if( destin_addr.equals( fuelStationClickedName ) ){
                    if( fuelStationClickedLatLng == null ){
                        try {
                            errorObj.put(this.DESTIN_ERROR_KEY, this.ERROR);
                        } catch (JSONException e) {
                        }
                        return errorObj;
                    }
                    //else
                    d_lat = fuelStationClickedLatLng.latitude;
                    d_lng = fuelStationClickedLatLng.longitude;
                }
                else {
                    Log.v(LOG_TAG, "Geocoding the destination...");
                    List<Address> addresses_d = geocoder.getFromLocationName(destin_addr, 1);
                    if (addresses_d == null || addresses_d.size() == 0) {
                    /*13/03/2015 Yu Sun: this will cause Looper.prepare() problems
                    So we just indicate error in the onPostExecute() */
                        //Toast.makeText(getBaseContext(),
                        //        "Wrong destination address, please try another one",
                        //        Toast.LENGTH_SHORT).show();
                        try {
                            errorObj.put(this.DESTIN_ERROR_KEY, this.ERROR);
                        } catch (JSONException e) {
                        }
                        return errorObj;
                    }
                    //else
                    d_lat = addresses_d.get(0).getLatitude();
                    d_lng = addresses_d.get(0).getLongitude();
                }
                ///////////////////////////////////////////////////////////////////////////////

                PathQuery pq = new PathQuery();
                Log.v(LOG_TAG, "Retrieving results from server...");
                result = pq.executeQuery(
                        o_lat,
                        o_lng,
                        d_lat,
                        d_lng,
                        0.0     //getting the directions only
                );
                Log.v(LOG_TAG, "done!");
            } catch (IOException e) {
                // This error is the same as result = pq.executeQuery returns null.
                // In most cases, the error is caused by internet connection, i.e.,
                // IOException. Therefore, we use the same double error json object
                // to represent this error type
                Log.e(LOG_TAG, "Error connecting to the geocoding server", e);
                try {
                    errorObj.put(RESULT_DIRECTION_KEY, ERROR);
                } catch (JSONException json_e) {
                    Log.e(LOG_TAG, "Error for errorObj json");
                }
                return errorObj;
            }
            return result;
        }

        /**
         * After getting the result, we draw the path and fuel stations on the pathMap
         * and move the focus of the map to the path.
         */
        @Override
        protected void onPostExecute(JSONObject result) {

            ///////// first check the validness of the geocoding result /////////////
            if (result.optString(ORIGIN_ERROR_KEY).equals(ERROR)) {
                Toast toast = Toast.makeText(getActivity(), "Error origin address, please try another one ",
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }
            if (result.optString(DESTIN_ERROR_KEY).equals(ERROR)) {
                Toast toast = Toast.makeText(getActivity(), "Error destination address, please try another one ",
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }

            ///// then check the validness of the Google directions API returned result /////////
            ///// and geocoding connection errors ////////////////
            if (result == null || result.optString(RESULT_DIRECTION_KEY).isEmpty() ||
                    result.optString(RESULT_DIRECTION_KEY).equals(ERROR)) {

                Toast toast = Toast.makeText(getActivity(), "Error getting the directions, please check" +
                                "the internet connection and try again",
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }

            // Clears all the existing markers on the map
            mMap.clear();

            ////////////////// Draw the path on the map //////////////////
            // At this step, the google directions API call (must) have returned the valid route
            ArrayList<LatLng> directionPoints = null;
            try {
                directionPoints =
                        DecodeDirection.getAllDirectionPoints(result.getString(RESULT_DIRECTION_KEY));
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error wrong json formats in directions", e);
                return;
            }
            Polyline newPolyline;
            ////GoogleMap mMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            PolylineOptions rectLine = new PolylineOptions().width(12).color(Color.BLUE);
            for (int i = 0; i < directionPoints.size(); i++) {
                rectLine.add(directionPoints.get(i));
            }
            //return a reference for future use
            newPolyline = mMap.addPolyline(rectLine);
            //////////////////////////////////////////////////////////////////////

            ////////////// move the focus of the map to the middle of the path //////////////
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(directionPoints.get((directionPoints.size() / 5)))
                    .zoom(13)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            /////////////////////////////////////////////////////////////////////////////////
        }
    }
}
