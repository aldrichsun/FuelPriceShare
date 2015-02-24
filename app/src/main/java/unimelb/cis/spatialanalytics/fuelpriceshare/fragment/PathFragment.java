package unimelb.cis.spatialanalytics.fuelpriceshare.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.Toast;

import unimelb.cis.spatialanalytics.fuelpriceshare.R;

import com.gc.materialdesign.views.ButtonRectangle;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.software.shell.fab.ActionButton;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import unimelb.cis.spatialanalytics.fuelpriceshare.maps.autoComplete.AutoCompleteAdapter;
import unimelb.cis.spatialanalytics.fuelpriceshare.maps.query.PathQuery;
import unimelb.cis.spatialanalytics.fuelpriceshare.others.CustomizeMapMarker;
import unimelb.cis.spatialanalytics.fuelpriceshare.others.DecodeDirection;
import unimelb.cis.spatialanalytics.fuelpriceshare.others.DrawMarkersOnMap;

/**
 * Created by Yu Sun on 19/02/2015.
 * Yu Sun 23/02/2015: Currently, we separated the RangeFragment and PathFragment, each of which
 * use their own map fragment. Later, one direction for improvement is that we use only one (small) map
 * fragment, and dynamically hide and show (small) fragments corresponding to functions in the
 * current RangeFragment and PathFragment with the one map fragment. The logic for the dynamically
 * break and combine (small) fragments might take a long time to implement. Therefore, we leave it
 * for the next improved version. A byproduct in that version is we can use the PreferenceFragment
 * instead of PreferenceActivity.
 */
public class PathFragment extends Fragment {

    private static final String LOG_TAG = PathFragment.class.getSimpleName();

    private GoogleMap pathMap; // Might be null if Google Play services APK is not available.

    private SupportMapFragment pathMapFragment;
    private SlidingUpPanelLayout mLayout;
    private ActionButton wayPointButton;
    private ActionButton closePanelButton;

    public PathFragment(){
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_path, container, false);
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
            pathMapFragment = (SupportMapFragment) fm.findFragmentById(R.id.path_map);
            if (pathMapFragment == null) {
                pathMapFragment = SupportMapFragment.newInstance();
                fm.beginTransaction().replace(R.id.path_map, pathMapFragment).commit();

                setUpMap();
                setUpSlidingUpPanel();
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

        if (pathMap == null) {

            int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
            if(status == ConnectionResult.SUCCESS) {
                pathMap = pathMapFragment.getMap();
                setUpMap();
                setUpSlidingUpPanel();
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
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #pathMap} is not null.
     */
    private void setUpMap() {

        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(getString(R.string.intent_pathActivity_text))) {

            /////////////// Set up the initial focus of the map ////////////////
            // passedMessage = "location_latitude##location_longitude##location_address"
            String passedMessage = intent.getStringExtra(
                    getString(R.string.intent_pathActivity_text));
            Log.v(LOG_TAG, "Passed message is: " + passedMessage);

            String[] tmp = passedMessage.split("##");
            if (tmp.length == 3) { // it has the initial address

                // add marker
                LatLng first_loc = new LatLng(Double.valueOf(tmp[0]), Double.valueOf(tmp[1]));
                pathMap.addMarker(new MarkerOptions().position(first_loc).title(tmp[2]));
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(first_loc)
                        .zoom(14)
                        .build();
                pathMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                // add the address to the DESTINATION auto complete text view
                AutoCompleteTextView dest_texdView = (AutoCompleteTextView)
                        getActivity().findViewById(R.id.destination_autoCompleteTextView);
                dest_texdView.setText(tmp[2]);
            }
            else{
                moveToDefaultFocus();
            }
            ////////////////////////////////////////////////////////////////////////
        }else{
            moveToDefaultFocus();
        }

        //////////////////////////// Set the listeners ///////////////////////////////
        ///////// Set the auto complete listeners ///////////
        // get the references of the auto complete text view
        AutoCompleteTextView origin_textView = (AutoCompleteTextView)
                getActivity().findViewById(R.id.origin_autoCompleteTextView);
        AutoCompleteTextView destin_textView = (AutoCompleteTextView)
                getActivity().findViewById(R.id.destination_autoCompleteTextView);

        origin_textView.setAdapter(new AutoCompleteAdapter(getActivity(), R.layout.list_item));
        origin_textView.setOnItemClickListener(new myAutoCompleteOnItemClickListener(
                origin_textView, destin_textView, true));

        destin_textView.setAdapter(new AutoCompleteAdapter(getActivity(), R.layout.list_item));
        destin_textView.setOnItemClickListener(new myAutoCompleteOnItemClickListener(
                origin_textView, destin_textView, false));
        /////////////////////////////////////////////////////////

        /////////// Set the route button listener //////////////
        ButtonRectangle pathQueryButton = (ButtonRectangle) getActivity().findViewById(R.id.path_query_button);
        pathQueryButton.setOnClickListener(new View.OnClickListener() {

            /**
             * TODO add comments
             * @param v
             */
            @Override
            public void onClick(View v) {
                AutoCompleteTextView origin_textView = (AutoCompleteTextView)
                        getActivity().findViewById(R.id.origin_autoCompleteTextView);
                AutoCompleteTextView destin_textView = (AutoCompleteTextView)
                        getActivity().findViewById(R.id.destination_autoCompleteTextView);
                String origin = origin_textView.getText().toString();
                String destination = destin_textView.getText().toString();

                // close the soft key pad
                InputMethodManager imm = (InputMethodManager)
                        getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(origin_textView.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(destin_textView.getWindowToken(), 0);

                // Call the PathQueryTask
                PathQueryTask pathQueryTask = new PathQueryTask();
                pathQueryTask.execute(origin, destination); //Note the order of the params
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }
        });

        ///////// Set the swap button listener ////////////
        // TODO add it
        ///////////////////////////////////////////////////
    }

    private void setUpSlidingUpPanel(){

        mLayout = (SlidingUpPanelLayout) getActivity().findViewById(R.id.path_sliding_layout);
        mLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                //Log.i(LOG_TAG, "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelExpanded(View panel) {
            }

            @Override
            public void onPanelCollapsed(View panel) {
            }

            @Override
            public void onPanelAnchored(View panel) {

            }

            @Override
            public void onPanelHidden(View panel) {
            }
        });
        mLayout.setTouchEnabled(false);

        wayPointButton = (ActionButton) getActivity().findViewById(R.id.way_point_button);
        wayPointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO issue the way point path query and redraw the route
                // User must select a marker
            }
        });

        closePanelButton = (ActionButton) getActivity().findViewById(R.id.close_panel_button);
        closePanelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            }
        });
    }


    /**
     * Move the camera focus of the map to an initial default location
     */
    private void moveToDefaultFocus(){
        // set the map an arbitrary initial location
        LatLng latLng = new LatLng(-37.7963, 144.9614); // "Melbourne Uni"

        ////mMap.addMarker(new MarkerOptions().position(latLng).title("Melbourne Uni"));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(14)
                .build();
        pathMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }


    /**
     *  Once the item is clicked, we close the soft keypad, check the origin_ and
     *  destination_autoCompleteTextView, if both are not empty, we call the PathQueryTask
     *  to geocode the origin and destination addresses, compute the direction (or called
     *  path or route) between the origin and destination and the fuel stations near to the path.
     */
    private class myAutoCompleteOnItemClickListener implements AdapterView.OnItemClickListener {

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
        public myAutoCompleteOnItemClickListener(
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

            // Call the PathQueryTask
            PathQueryTask pathQueryTask = new PathQueryTask();
            pathQueryTask.execute(origin, destination); //Note the order of the params

            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        }
    }

    /**
     * The function of the task is as follows:
     * i) geocode the origin and destination addresses,
     * ii) compute the direction (or called path or route) between the origin
     * and destination and the fuel stations near to the path
     * iii) draw the path and fuel stations on the pathMap
     * and iv) move the focus of the map to the path.
     * We choose to CHECK the address in the origin_ and destination_autoCompleteTextView
     * each time as 1) it's difficult to keep track of a LatLng object for the origin
     * or destination since the user behaviour is unpredictable and 2) it's easier later for
     * us to move the geocode task to the server.
     * TODO !!!!! the app crashes when the server is shut down
     */
    private class PathQueryTask extends AsyncTask<String, Void, JSONObject> {

        private static final String RESULT_DIRECTION_KEY = "direction";
        private static final String RESULT_STATION_KEY = "fuel_station";
        private static final String ERROR = "error";
        private static final String EMPTY = "empty";
        private static final String ORIGIN_ERROR_KEY = "origin_geocoding";
        private static final String DESTIN_ERROR_KEY = "destin_geocoding";

        // the private variable storing the user preferred path distance
        private double path_distance = 1.0;

        /**
         * Before retrieving the directions, we display a message telling the user to wait,
         * and retrieve the user preferred path distance (store it in this.path_distance).
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Show the auto complete address with a Toast at the bottom of the screen
            Toast toast = Toast.makeText(getActivity(), "Retrieving the route, please wait...",
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            //read and store the user preferred path distance
            String path_dist = PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getString(
                            getString(R.string.pref_key_path_distance),
                            getString(R.string.pref_default_path_distance)
                    );
            this.path_distance = Double.valueOf(path_dist);
        }

        /**
         * The first string is the origin and the second is the destination
         *
         * @param originAndDestination -- the first string is the origin and the second is the destination
         * @return i) A Json object with 1) the name of "fuel_station" and value of a Json array contains a
         * list of the required stations, and 2) the name of "direction" and value of the response
         * string of google directions API call.
         * ii) If there are errors in the origin/destination geoCoding, there will be a field with the name
         * of "origin/destin_geocoding" and value of "error" (string)
         * iii) If there are errors in any other field, the corresponding value would also be "error".
         * iv) If there are no query results, the corresponding value of "fuel_station" would be "empty".
         * <p/>
         * One example result is {"fuel_station":"empty","direction":"error"}
         */
        @Override
        protected JSONObject doInBackground(String... originAndDestination) {

            JSONObject result = null;
            JSONObject errorObj = new JSONObject();

            String origin_addr = originAndDestination[0];
            String destin_addr = originAndDestination[1];

            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(getActivity());

            try {
                /////// If any error occurs, we don't do the path query and return ///////
                /////// the error object directly ////////////////////////////////////////
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
                ///////////////////////////////////////////////////////////////////////////////

                PathQuery pq = new PathQuery();
                Log.v(LOG_TAG, "Retrieving results from server...");
                result = pq.executeQuery(
                        addresses_o.get(0).getLatitude(),
                        addresses_o.get(0).getLongitude(),
                        addresses_d.get(0).getLatitude(),
                        addresses_d.get(0).getLongitude(),
                        this.path_distance
                );
                Log.v(LOG_TAG, "done!");
            } catch (IOException e) {
                // This error is the same as result = pq.executeQuery returns null.
                // In most cases, the error is caused by internet connection, i.e.,
                // IOException. Therefore, we use the same double error json object
                // to represent this error type
                Log.e(LOG_TAG, "Error connecting to the geocoding server", e);
                try {
                    errorObj.put(RESULT_STATION_KEY, ERROR);
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

            if(result == null )

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
            pathMap.clear();

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
            newPolyline = pathMap.addPolyline(rectLine);
            //////////////////////////////////////////////////////////////////////

            ////////////// move the focus of the map to the middle of the path //////////////
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(directionPoints.get((directionPoints.size() / 5)))
                    .zoom(13)
                    .build();
            pathMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            /////////////////////////////////////////////////////////////////////////////////

            ////////////////// Show the stations near to the path ///////////////////
            //////////// check the validness of the Queried Stations /////////////////
            if (result.optString(RESULT_STATION_KEY).equals(ERROR)
                    || result.optString(RESULT_STATION_KEY).equals(EMPTY)) {
                Log.e(LOG_TAG, "Error finding fuel stations, please check the server response: "
                        + result.optString(RESULT_STATION_KEY));
                return;
            }
            if (result.optJSONArray(RESULT_STATION_KEY).length() == 0) {

                Toast toast = Toast.makeText(getActivity(), "No fuel stations near the route, please " +
                        "increase the range and try again", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }
            ///////////////////////////////////////////////////////////////////////////////

            // draw the stations on the map
            JSONArray stations = null;
            try {
                stations = result.getJSONArray(RESULT_STATION_KEY);

                //Else show all the stations
                DrawMarkersOnMap.drawOnMap(
                        (android.support.v7.app.ActionBarActivity) getActivity(),
                        pathMap,
                        stations);
                return;
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Wrong json array for the fuel stations", e);
                return;
            }
        }
    }
}