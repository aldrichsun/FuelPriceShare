package unimelb.cis.spatialanalytics.fuelpriceshare.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonRectangle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.software.shell.fab.ActionButton;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import unimelb.cis.spatialanalytics.fuelpriceshare.R;
import unimelb.cis.spatialanalytics.fuelpriceshare.maps.myLocation.GPSTracker;
import unimelb.cis.spatialanalytics.fuelpriceshare.maps.DrawOnMap.DecodeDirection;
import unimelb.cis.spatialanalytics.fuelpriceshare.maps.DrawOnMap.DrawMarkersOnMap;
import unimelb.cis.spatialanalytics.fuelpriceshare.maps.autoComplete.AutoCompleteAdapter;
import unimelb.cis.spatialanalytics.fuelpriceshare.maps.autoComplete.myOnLongClickListener;
import unimelb.cis.spatialanalytics.fuelpriceshare.maps.locationHistory.SearchLocationHistory;
import unimelb.cis.spatialanalytics.fuelpriceshare.maps.myLocation.MyLocation;
import unimelb.cis.spatialanalytics.fuelpriceshare.maps.query.PathQuery;
import unimelb.cis.spatialanalytics.fuelpriceshare.maps.query.RangeQuery;

/**
 * Created by Yu Sun on 1/03/2015.
 * This fragment does the improvement specified in the range fragment. It combines the RangeFragment
 * and PathFragment and has integrated functions previously separated in the above two ones.
 *
 * The logic is much more complex after combining the two fragments. Thus, requires much more
 * careful implementation.
 * Many of the comments are deprecated due to copy and paste. After everything is settle down,
 * we may update the comments in a batch fashion.
 */
public class MapFragment extends Fragment{

    private static final String LOG_TAG = MapFragment.class.getSimpleName();

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LatLng currentLocation; // The user's current location.
    private String currentLocationName; // "Current location" R.string.Your_location.
    private Marker clickedMarer = null; // The marker clicked by the user.
    private LatLng destinLatLng = null; // The geocoded destination locaiton.
    private LatLng wayPointLatLng = null; // The fuel station to which user wants to detour.
    private String wayPointName; // The name of the fuel station clicked by the user to which user wants to detour.
    private String destinAddressText; // The destination address text.

    private SupportMapFragment mMapFragment;
    private SlidingUpPanelLayout directionSliding;
    private SlidingUpPanelLayout wayPointSliding;
    private ActionButton pathFuelJumpButton;
    private ActionButton wayPointJumpButton;

    private boolean showResult = false; // whether a operation series has come to an end

    private String lastAddress = ""; // used for storing location history

    private boolean isCurrentLocationEnabled = false; // whether we can get the user's current location

    public void MapFragment(){
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (status == ConnectionResult.SUCCESS) {
            FragmentManager fm = getChildFragmentManager();
            mMapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
            if (mMapFragment == null) {
                mMapFragment = SupportMapFragment.newInstance();
                fm.beginTransaction().replace(R.id.map, mMapFragment).commit();
            }
        } else {
            Log.e(LOG_TAG, "Google Play service is not enabled on this device.");
            if (getActivity() != null) {
                Toast toast = Toast.makeText(getActivity(), "Google Play service is not enabled on this device.",
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
            getActivity().finish();
        }
    }

    @Override
    public void onResume() {

        super.onResume();

        if (mMap == null) {

            int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
            if (status == ConnectionResult.SUCCESS) {
                mMap = mMapFragment.getMap();

                if (mMap != null) {
                    setUpMap();
                    setUpDirectionSlidingPanel();
                    setUpWayPointSlidingPanel();
                }
            } else {
                Log.e(LOG_TAG, "Google Play service is not enabled on this device.");
                if (getActivity() != null) {
                    Toast toast = Toast.makeText(getActivity(), "Google Play service is not enabled on this device.",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                getActivity().finish();
            }
        }
    }

    /**
     * Here we initialize the UI components in the main view (the starting screen for
     * map fragment). We also set up responding actions for each component.
     * Each time the RangeTask, PathTask or WayPointTask is executed, we store
     * the search history of the user.
     */
    private void setUpMap() {

        /////////////// Set up the initial focus of the map, which is the user current location ////////////////
        mMap.setMyLocationEnabled(true);
        //Location myLocation = MyLocation.getMyLocation();
        GPSTracker gps = new GPSTracker( getActivity() );
        Location myLocation = gps.getLocation();
        isCurrentLocationEnabled = gps.canGetLocation();
        currentLocationName = getString(R.string.Your_location);

        if( myLocation != null ){

            currentLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            //Log.e(LOG_TAG, "The current location is: " + currentLocation.toString());
            destinLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        }
        else {
            //destinLatLng = new LatLng(-37.7963, 144.9614); // Melbourne Uni
            destinLatLng = new LatLng(0.0, 0.0);
        }
        ////mMap.addMarker(new MarkerOptions().position(latLng).title("Melbourne Uni"));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(destinLatLng)
                .zoom(14)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        ////////////////////////////////////////////////////////////////////////

        ///////////////////// Set up view adapters and event listeners ////////////////////////
        ///// Set the auto complete text view adapter //////
        // Get reference to the auto complete text view
        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView)
                getActivity().findViewById(R.id.locate_dest_autoCompleteTextView);
        // Set the auto complete text view adapter (which maps the output content to the text view)
        autoCompleteTextView.setAdapter(new AutoCompleteAdapter(
                getActivity(), R.layout.list_item));
        ///////////////////////////////////////////////////
        ///////// The AutoComplete listener ////////////
        // Set the auto complete text view item click listener
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            // i) use the searched address the user input as the query point, and use the user
            // preferred range distance to issue the range query; ii) draw the returned points
            // on the map with customized marker icon
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get user input location
                String location = (String) parent.getItemAtPosition(position);

                // Close the soft keypad
                AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView)
                        getActivity().findViewById(R.id.locate_dest_autoCompleteTextView);
                InputMethodManager imm = (InputMethodManager)
                        getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(autoCompleteTextView.getWindowToken(), 0);

                // Issue the range query and display the returned results on the map
                if( location == null || location.equals("") ) {
                    return;
                }
                new RangeQueryTask().execute(location);

                ////////// store the location history ////////////
                new AddressHistoryTask().execute(location);
                //////////////////////////////////////////////////

                // Show the direction floating action button (fab)
                if (pathFuelJumpButton.isHidden()) {
                    // first hide the way point jump button
                    if (!wayPointJumpButton.isHidden()) {
                        wayPointJumpButton.setHideAnimation(ActionButton.Animations.JUMP_TO_DOWN);
                        wayPointJumpButton.hide();
                    }
                    pathFuelJumpButton.setShowAnimation(ActionButton.Animations.JUMP_FROM_RIGHT);
                    pathFuelJumpButton.show();
                }
                // re-initialize the operation series (cycles)
                showResult = false;
            }
        });
        autoCompleteTextView.setOnLongClickListener(new myOnLongClickListener(
                (ActionBarActivity)getActivity()));
        if( isCurrentLocationEnabled ){
            autoCompleteTextView.setText(getString(R.string.Your_location));
        }

        //////////////////////////////////////////////////////

        ///////////////// Set the 'Find' button listener /////////////////////
        // Get reference to btn_find of the layout activity_main
        ButtonRectangle buttonFind = (ButtonRectangle) getActivity().findViewById(R.id.locate_destination_button);
        // Define button click event listener for the find button
        View.OnClickListener findClickListener = new View.OnClickListener() {

            // Once the button is clicked, we i) close the input pad ii) geocode the input address
            // iii) add a marker on the map and move focus to the marker
            // Tasks ii) and iii) is completed by GeocoderTask
            @Override
            public void onClick(View v) {

                // Get reference to the auto complete text view to get the user input location
                AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView)
                        getActivity().findViewById(R.id.locate_dest_autoCompleteTextView);
                // Get the auto complete location
                String location = autoCompleteTextView.getText().toString();

                // Close the soft keypad
                InputMethodManager imm = (InputMethodManager)
                        getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(autoCompleteTextView.getWindowToken(), 0);

                // Issue the range query and display the returned results on the map
                if( location == null || location.equals("") ) {
                    if(getActivity() != null) {
                        Toast toast = Toast.makeText(getActivity(), "Please enter a location",
                                Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                    return;
                }

                new RangeQueryTask().execute(location);

                ////////// store the location history ////////////
                new AddressHistoryTask().execute(location);
                //////////////////////////////////////////////////

                // Show the direction floating action button (fab)
                if (pathFuelJumpButton.isHidden()) {
                    // first hide the way point jump button
                    if (!wayPointJumpButton.isHidden()) {
                        wayPointJumpButton.setHideAnimation(ActionButton.Animations.JUMP_TO_DOWN);
                        wayPointJumpButton.hide();
                    }
                    pathFuelJumpButton.setShowAnimation(ActionButton.Animations.JUMP_FROM_RIGHT);
                    pathFuelJumpButton.show();
                }
                // re-initialize the operation series
                showResult = false;
            }
        };
        // Setting button click event listener for the find button
        buttonFind.setOnClickListener(findClickListener);
        ////////////////////////////////////////////////////////////////////

        ///////////// Set the path query (floating action) button listener ////////////
        // Get a reference to the button
        // 24/02/2015 Yu Sun: Change to floating action buttion
        pathFuelJumpButton = (ActionButton) getActivity().findViewById(R.id.path_fuel_jump_button);
        // Set the listener
        pathFuelJumpButton.setOnClickListener(new View.OnClickListener() {

            // After the button is clicked, we do the following three tasks:
            // Hide the fabs and show the path query panel
            @Override
            public void onClick(View v) {

                if(wayPointSliding.getPanelState() == PanelState.EXPANDED)
                    wayPointSliding.setPanelState(PanelState.HIDDEN);

                pathFuelJumpButton.setHideAnimation(ActionButton.Animations.FADE_OUT);
                pathFuelJumpButton.hide();

                ////// set the origin and destination text
                AutoCompleteTextView pathFuelOrigin = (AutoCompleteTextView) getActivity()
                        .findViewById(R.id.path_fuel_origin_autoCompleteTextView);
                if( isCurrentLocationEnabled ) {
                    pathFuelOrigin.setText(getString(R.string.Your_location));
                }
                AutoCompleteTextView pathFuelDestin = (AutoCompleteTextView) getActivity()
                        .findViewById(R.id.path_fuel_destination_autoCompleteTextView);
                pathFuelDestin.setText(destinAddressText);

                //show the panel
                directionSliding.setAnchorPoint(0.7f);
                directionSliding.setPanelState(PanelState.ANCHORED);
            }
        });
        pathFuelJumpButton.hide();
        //////////////////////////////////////////////////////////////////////

        /////////////// Set the way point jump button listener //////////////////
        wayPointJumpButton = (ActionButton) getActivity().findViewById(R.id.wayPoint_jump_button);
        wayPointJumpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if( wayPointSliding.getPanelState() != PanelState.EXPANDED )
                    wayPointSliding.setPanelState(PanelState.EXPANDED);

                AutoCompleteTextView originText = (AutoCompleteTextView)
                        getActivity().findViewById(R.id.wayPoint_origin_autoCompleteTextView);
                if( isCurrentLocationEnabled ) {
                    originText.setText(getString(R.string.Your_location));
                }

                AutoCompleteTextView wayPointText = (AutoCompleteTextView)
                        getActivity().findViewById(R.id.waypoint_autoCompleteTextView);
                wayPointText.setText( wayPointName );

                AutoCompleteTextView destinText = (AutoCompleteTextView)
                        getActivity().findViewById(R.id.wayPoint_destination_autoCompleteTextView);
                destinText.setText( destinAddressText );

                wayPointJumpButton.setHideAnimation(ActionButton.Animations.FADE_OUT);
                wayPointJumpButton.hide();

                directionSliding.setAnchorPoint(0.7f);
                directionSliding.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
            }
        });
        wayPointJumpButton.hide();
        /////////////////////////////////////////////////////////////////////////

        ///////////// Set the marker click events listener //////////////
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {

                // always show the info window and set the clickedMarker to this
                marker.showInfoWindow();
                clickedMarer = marker;

                // Han Li and Yu Sun 26/02/2015: always close the soft keypad
                if( getActivity().getCurrentFocus() != null && getActivity().getCurrentFocus().getWindowToken() != null) {
                    InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    keyboard.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                }

                if( showResult ) // if the  query cycle has ended, we do nothing
                    return true;

                // if the direction sliding panel is expanded, we set the way point
                // autoCompleteTextView to the name of the marker (whether or not
                // the way point sliding panel is expanded or hidden) if the marker is
                // not the destination marker
                if( directionSliding.getPanelState() == PanelState.EXPANDED ||
                    directionSliding.getPanelState() == PanelState.ANCHORED ){

                    if( !marker.getPosition().equals( destinLatLng ) ) {
                        wayPointName = marker.getTitle();
                        AutoCompleteTextView wayPoint_textView = (AutoCompleteTextView)
                                getActivity().findViewById(R.id.waypoint_autoCompleteTextView);
                        wayPoint_textView.setText(wayPointName);
                        wayPointLatLng = new LatLng(
                                marker.getPosition().latitude, marker.getPosition().longitude);
                    }
                    return true;
                }

                // if the clicked marker is the destination marker, we show the direction
                // fab (path query fab) and hide the way point fab
                if( marker.getPosition().equals( destinLatLng ) ) { // user clicks the destination

                    if( !wayPointJumpButton.isHidden() ) {
                        wayPointJumpButton.setHideAnimation(ActionButton.Animations.JUMP_TO_DOWN);
                        wayPointJumpButton.hide();
                    }
                    if( pathFuelJumpButton.isHidden() ) {
                        pathFuelJumpButton.setShowAnimation(ActionButton.Animations.JUMP_FROM_RIGHT);
                        pathFuelJumpButton.show();
                    }
                }
                else{ // user clicks any fuel station, we show the way point fab and
                      // hide the direction (path query) fab

                    if( !pathFuelJumpButton.isHidden() ){
                        pathFuelJumpButton.setHideAnimation(ActionButton.Animations.JUMP_TO_DOWN);
                        pathFuelJumpButton.hide();
                    }
                    if( wayPointJumpButton.isHidden() ) {
                        wayPointJumpButton.setShowAnimation(ActionButton.Animations.JUMP_FROM_RIGHT);
                        wayPointJumpButton.show();
                    }
                    wayPointName = marker.getTitle();
                    wayPointLatLng = new LatLng(
                            marker.getPosition().latitude, marker.getPosition().longitude);
                }
                return true;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                // this is only possible at the initial state.
                // After the user clicks 'find' (or autoCompleteTextVew) till the end of
                // the query cycle, it is guaranteed that there is always a clickedMarker.
                if( clickedMarer == null )
                    return;

                // Panel is shown, we close the panel
                if(directionSliding.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED ){
                    directionSliding.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                }
                else {
                    if( showResult ) // if the query cycle has ended
                        return;
                    //else show the direction fab
                    if( !wayPointJumpButton.isHidden() ) {
                        wayPointJumpButton.setHideAnimation(ActionButton.Animations.JUMP_TO_DOWN);
                        wayPointJumpButton.hide();
                    }
                    if( pathFuelJumpButton.isHidden() ) {
                        pathFuelJumpButton.setShowAnimation(ActionButton.Animations.JUMP_FROM_RIGHT);
                        pathFuelJumpButton.show();
                    }
                }
            }
        });
        ////////////////////////////////////////////////////////////////////////
    }

    /**
     * We here initialize and set up the path query sliding up panel components.
     * Each time the RangeTask, PathTask or WayPointTask is executed, we store
     * the search history of the user.
     */
    public void setUpDirectionSlidingPanel(){

        //Initialize the sliding up panel
        directionSliding = (SlidingUpPanelLayout) getActivity().findViewById(R.id.direction_sliding);
        directionSliding.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                //Log.i(LOG_TAG, "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelExpanded(View panel) {

//                if( !wayPointJumpButton.isHidden() ) {
//                    wayPointJumpButton.setHideAnimation(ActionButton.Animations.FADE_OUT);
//                    wayPointJumpButton.hide();
//                }
//                if( !pathFuelJumpButton.isHidden() ) {
//                    pathFuelJumpButton.setHideAnimation(ActionButton.Animations.FADE_OUT);
//                    pathFuelJumpButton.hide();
//                }
            }

            @Override
            public void onPanelCollapsed(View panel) {

                // When the panel is closed, we
                // Han Li and Yu Sun 26/02/2015: always close the soft keypad
                if( getActivity().getCurrentFocus() != null && getActivity().getCurrentFocus().getWindowToken() != null) {
                    InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    keyboard.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                }

                if( showResult ) //if the query cycle has ended, we do nothing
                    return;
                //otherwise if the previous clicked marker is the destination marker
                // we show the direction (path query) fab
                if( clickedMarer.getPosition().equals( destinLatLng ) ) {
                    // we came from the pathFuelJumpButton
                    pathFuelJumpButton.setShowAnimation(ActionButton.Animations.FADE_IN);
                    pathFuelJumpButton.show();
                //if the previous clicked marker is a fuel station marker
                // we show the way point fab
                }else{
                    // we came from the wayPointJumpButton
                    wayPointJumpButton.setShowAnimation(ActionButton.Animations.FADE_IN);
                    wayPointJumpButton.show();
                }
            }

            @Override
            public void onPanelAnchored(View panel) {

//                if( !wayPointJumpButton.isHidden() ) {
//                    wayPointJumpButton.setHideAnimation(ActionButton.Animations.FADE_OUT);
//                    wayPointJumpButton.hide();
//                }
//                if( !pathFuelJumpButton.isHidden() ) {
//                    pathFuelJumpButton.setHideAnimation(ActionButton.Animations.FADE_OUT);
//                    pathFuelJumpButton.hide();
//                }
            }

            @Override
            public void onPanelHidden(View panel) {

                // the operations are the same as the onPanelCollapsed method

                // Han Li and Yu Sun 26/02/2015: close the soft keypad
                if( getActivity().getCurrentFocus() != null && getActivity().getCurrentFocus().getWindowToken() != null) {
                    InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    keyboard.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                }

                if( showResult )
                    return;
                if( clickedMarer.getPosition().equals( destinLatLng ) ) {
                    // we came from the pathFuelJumpButton
                    pathFuelJumpButton.setShowAnimation(ActionButton.Animations.FADE_IN);
                    pathFuelJumpButton.show();
                }else{
                    // we came from the wayPointJumpButton
                    wayPointJumpButton.setShowAnimation(ActionButton.Animations.FADE_IN);
                    wayPointJumpButton.show();
                }
            }
        });
        directionSliding.setPanelState(PanelState.HIDDEN);

        //////// Set up auto complete text view and search button //////////////
        // get the references of the auto complete text view
        AutoCompleteTextView origin_textView = (AutoCompleteTextView)
                getActivity().findViewById(R.id.path_fuel_origin_autoCompleteTextView);
        AutoCompleteTextView destin_textView = (AutoCompleteTextView)
                getActivity().findViewById(R.id.path_fuel_destination_autoCompleteTextView);

        origin_textView.setAdapter(new AutoCompleteAdapter(getActivity(), R.layout.list_item));
        origin_textView.setOnItemClickListener(new pathFuelAutoCompleteOnItemClickListener(
                origin_textView, destin_textView, true));
        origin_textView.setOnLongClickListener(new myOnLongClickListener(
                (ActionBarActivity)getActivity()));

        destin_textView.setAdapter(new AutoCompleteAdapter(getActivity(), R.layout.list_item));
        destin_textView.setOnItemClickListener(new pathFuelAutoCompleteOnItemClickListener(
                origin_textView, destin_textView, false));
        destin_textView.setOnLongClickListener(new myOnLongClickListener(
                (ActionBarActivity)getActivity()));

        /////////////////////////////////////////////////////////

        /////////// Set the route button listener //////////////
        ButtonRectangle pathFuelButton = (ButtonRectangle) getActivity().findViewById(R.id.path_fuel_button);
        pathFuelButton.setOnClickListener(new View.OnClickListener() {

            /**
             * TODO add comments
             * @param v
             */
            @Override
            public void onClick(View v) {
                AutoCompleteTextView origin_textView = (AutoCompleteTextView)
                        getActivity().findViewById(R.id.path_fuel_origin_autoCompleteTextView);
                AutoCompleteTextView destin_textView = (AutoCompleteTextView)
                        getActivity().findViewById(R.id.path_fuel_destination_autoCompleteTextView);
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

                ////////// store the location history ////////////
                new AddressHistoryTask().execute(origin);
                new AddressHistoryTask().execute(destination);
                //////////////////////////////////////////////////
            }
        });

    }

    /**
     * We here initialize and set up the detour query (way point query) sliding up panel
     * components.
     * Each time the RangeTask, PathTask or WayPointTask is executed, we store
     * the search history of the user.
     */
    public void setUpWayPointSlidingPanel(){

        //Initialize the sliding up panel
        wayPointSliding = (SlidingUpPanelLayout) getActivity().findViewById(R.id.wayPoint_sliding);
        wayPointSliding.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                //Log.i(LOG_TAG, "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelExpanded(View panel) {

//                if( !wayPointJumpButton.isHidden() ) {
//                    wayPointJumpButton.setHideAnimation(ActionButton.Animations.FADE_OUT);
//                    wayPointJumpButton.hide();
//                }
//                if( !pathFuelJumpButton.isHidden() ) {
//                    pathFuelJumpButton.setHideAnimation(ActionButton.Animations.FADE_OUT);
//                    pathFuelJumpButton.hide();
//                }
            }

            @Override
            public void onPanelCollapsed(View panel) {

//                if( clickedMarer.getPosition().equals( destinLatLng ) ) {
//                    // we came from the pathFuelJumpButton
//                    pathFuelJumpButton.setShowAnimation(ActionButton.Animations.FADE_IN);
//                    pathFuelJumpButton.show();
//                }else{
//                    // we came from the wayPointJumpButton
//                    wayPointJumpButton.setShowAnimation(ActionButton.Animations.FADE_IN);
//                    wayPointJumpButton.show();
//                }
            }

            @Override
            public void onPanelAnchored(View panel) {

//                if( !wayPointJumpButton.isHidden() ) {
//                    wayPointJumpButton.setHideAnimation(ActionButton.Animations.FADE_OUT);
//                    wayPointJumpButton.hide();
//                }
//                if( !pathFuelJumpButton.isHidden() ) {
//                    pathFuelJumpButton.setHideAnimation(ActionButton.Animations.FADE_OUT);
//                    pathFuelJumpButton.hide();
//                }
            }

            @Override
            public void onPanelHidden(View panel) {

//                if( clickedMarer.getPosition().equals( destinLatLng ) ) {
//                    // we came from the pathFuelJumpButton
//                    pathFuelJumpButton.setShowAnimation(ActionButton.Animations.FADE_IN);
//                    pathFuelJumpButton.show();
//                }else{
//                    // we came from the wayPointJumpButton
//                    wayPointJumpButton.setShowAnimation(ActionButton.Animations.FADE_IN);
//                    wayPointJumpButton.show();
//                }
            }
        });
        wayPointSliding.setPanelState(PanelState.EXPANDED);
        wayPointSliding.setTouchEnabled(false);
        //////// Set up auto complete text view and search button //////////////
        // get the references of the auto complete text view
        AutoCompleteTextView origin_textView = (AutoCompleteTextView)
                getActivity().findViewById(R.id.wayPoint_origin_autoCompleteTextView);

        AutoCompleteTextView wayPoint_textView = (AutoCompleteTextView)
                getActivity().findViewById(R.id.waypoint_autoCompleteTextView);
        wayPoint_textView.setEnabled(false);

        AutoCompleteTextView destin_textView = (AutoCompleteTextView)
                getActivity().findViewById(R.id.wayPoint_destination_autoCompleteTextView);

        origin_textView.setAdapter(new AutoCompleteAdapter(getActivity(), R.layout.list_item));
        origin_textView.setOnItemClickListener(new wayPointAutoCompleteOnItemClickListener(
                origin_textView, wayPoint_textView, destin_textView, true));
        origin_textView.setOnLongClickListener(new myOnLongClickListener(
                (ActionBarActivity)getActivity()));

        destin_textView.setAdapter(new AutoCompleteAdapter(getActivity(), R.layout.list_item));
        destin_textView.setOnItemClickListener(new wayPointAutoCompleteOnItemClickListener(
                origin_textView, wayPoint_textView, destin_textView, false));
        destin_textView.setOnLongClickListener(new myOnLongClickListener(
                (ActionBarActivity)getActivity()));

        /////////////////////////////////////////////////////////

        /////////// Set the route button listener //////////////
        ButtonRectangle wayPointButton = (ButtonRectangle) getActivity().findViewById(R.id.wayPoint_button);
        wayPointButton.setOnClickListener(new View.OnClickListener() {

            /**
             * TODO add comments
             * @param v
             */
            @Override
            public void onClick(View v) {
                AutoCompleteTextView origin_textView = (AutoCompleteTextView)
                        getActivity().findViewById(R.id.wayPoint_origin_autoCompleteTextView);
                AutoCompleteTextView wayPoint_textView = (AutoCompleteTextView)
                        getActivity().findViewById(R.id.waypoint_autoCompleteTextView);
                AutoCompleteTextView destin_textView = (AutoCompleteTextView)
                        getActivity().findViewById(R.id.wayPoint_destination_autoCompleteTextView);
                String origin = origin_textView.getText().toString();
                String waypoint = wayPoint_textView.getText().toString();
                String destination = destin_textView.getText().toString();

                // close the soft key pad
                InputMethodManager imm = (InputMethodManager)
                        getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(origin_textView.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(destin_textView.getWindowToken(), 0);

                // Call the WayPointTask
                WayPointTask wayPointTask = new WayPointTask();
                wayPointTask.execute(origin, waypoint, destination); //Note the order of the params

                ////////// store the location history ////////////
                new AddressHistoryTask().execute(origin);
                new AddressHistoryTask().execute(waypoint);
                new AddressHistoryTask().execute(destination);
                //////////////////////////////////////////////////
            }
        });
    }

    /**
     * Listener for the autoCompleteTextView on the path query sliding up panel.
     *
     * Once the item is clicked, we close the soft keypad, check the origin_ and
     * destination_autoCompleteTextView, if both are not empty, we call the PathQueryTask
     * to geocode the origin and destination addresses, compute the direction (or called
     * path or route) between the origin and destination and the fuel stations near to the path.
     */
    private class pathFuelAutoCompleteOnItemClickListener implements AdapterView.OnItemClickListener {

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
        public pathFuelAutoCompleteOnItemClickListener(
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

            ////////// store the location history ////////////
            new AddressHistoryTask().execute(origin);
            new AddressHistoryTask().execute(destination);
            //////////////////////////////////////////////////
        }
    }

    /**
     * Listener for the autoCompleteTextView on the path query sliding up panel.
     *
     * Once the item is clicked, we close the soft keypad, check the origin_ and
     * destination_autoCompleteTextView, if both are not empty, we call the WayPointTask
     * to geocode the origin and destination addresses, compute the detoured direction
     * (or called path or route) between the origin and destination with the selected
     * fuel station as a way point.
     */
    private class wayPointAutoCompleteOnItemClickListener implements AdapterView.OnItemClickListener {

        AutoCompleteTextView origin_autoCompleteTextView;
        AutoCompleteTextView waypoint_autoCompleteTextView;
        AutoCompleteTextView destin_autoCompleteTextView;
        boolean clickFromOriginTextView;

        /**
         * Constructor of the listener.
         * @param origin_autoCompleteTextView -- text view reference for the origin
         * @param waypoint_autoCompleteTextView -- text view reference for the waypoint
         * @param destin_autoCompleteTextView -- text view reference for the destination
         * @param clickFromOriginTextView -- true if the click is from the origin text view, false if
         *                                the click is from the destination text view
         */
        public wayPointAutoCompleteOnItemClickListener(
                AutoCompleteTextView origin_autoCompleteTextView,
                AutoCompleteTextView waypoint_autoCompleteTextView,
                AutoCompleteTextView destin_autoCompleteTextView,
                boolean clickFromOriginTextView){
            this.origin_autoCompleteTextView = origin_autoCompleteTextView;
            this.waypoint_autoCompleteTextView = waypoint_autoCompleteTextView;
            this.destin_autoCompleteTextView = destin_autoCompleteTextView;
            this.clickFromOriginTextView = clickFromOriginTextView;
        }

        // Once the item is clicked, we close the soft keypad, check the origin_ and
        // destination_autoCompleteTextView, if both are not empty, we call the WayPointTask
        // to geocode the origin and destination addresses, compute the detoured direction
        // (or called path or route) between the origin and destination with the selected
        // fuel station as a way point.
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
            String waypoint = this.waypoint_autoCompleteTextView.getText().toString();
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

            // Call the WayPointTask
            WayPointTask wayPointTask = new WayPointTask();
            wayPointTask.execute(origin, waypoint, destination); //Note the order of the params

            ////////// store the location history ////////////
            new AddressHistoryTask().execute(origin);
            new AddressHistoryTask().execute(waypoint);
            new AddressHistoryTask().execute(destination);
            //////////////////////////////////////////////////
        }
    }

    //////////////////////////////// AsyncTasks ////////////////////////////////////////
    /**
     * The function of the class is as follows:
     * i) geocode the destination address the user input as the query point,
     * ii) and use the user preferred range distance to issue the range query,
     * ii) draw on the map the input address with normal marker and the
     * returned points with customized marker icons.
     * iv) store the destination address and location in the instance variable
     * 'destinLatLng' and 'destinAddressText', respectively.
     */
    private class RangeQueryTask extends AsyncTask<String, Void, JSONArray>{

        // The range query search distance
        private double range_radius = 3.0;
        ProgressDialog progressDialog;
        private boolean geocodeError = false;
        private boolean curLocationError = false;

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

            //Location location = MyLocation.getMyLocation();
            curLocationError = false;
            GPSTracker gpsTracker = new GPSTracker( getActivity() );
            Location location = gpsTracker.getLocation();
            isCurrentLocationEnabled = gpsTracker.canGetLocation();
            if( location != null )
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

            // the geo-coding error flag
            geocodeError = false;

            // progress dialog
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Please wait...");
            progressDialog.setMessage("Retrieving fuel stations...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        /**
         * Use the searched address input by the user as the query point, and the user preferred range
         * distance as range to issue the range query.
         * If geo-coding error occurs, it returns null and sets the private instance variable geocodeError
         * to true.
         * @param locationName -- the address input by the user
         * @return i) A list of stations (in Json array) within the range, which is empty is no
         * such station exists.
         * ii) null, if geo-coding error or query error occurs.
         */
        @Override
        protected JSONArray doInBackground(String... locationName) {

            if( locationName[0].equals( currentLocationName ) ){
                if( currentLocation == null ){
                    curLocationError = true;
                    return null;
                }
                //else
                destinLatLng = new LatLng(currentLocation.latitude, currentLocation.longitude);
                destinAddressText = currentLocationName;
            }
            else {
                //We first geo-code the input address
                Geocoder geocoder = new Geocoder(getActivity());
                List<Address> addresses = null;

                try {
                    addresses = geocoder.getFromLocationName(locationName[0], 1);
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Internet error when doing geo-coding on: " + locationName[0], e);
                    geocodeError = true;
                    return null;
                }
                if (addresses == null || addresses.size() == 0) {
                    geocodeError = true;
                    return null;
                }
                // We have successfully get the searched address
                // First store it as the destination
                Address address = (Address) addresses.get(0);
                destinLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                destinAddressText = String.format("%s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getCountryName());
            }

            // Then start the range query
            //Log.v(LOG_TAG, "Starting the range query...");
            RangeQuery rangeQuery = new RangeQuery();
            JSONArray stations = rangeQuery.executeQuery(destinLatLng,
                    Double.valueOf(range_radius));
            //Log.v(LOG_TAG, "The results are: " + stations.toString());
            return stations;
        }

        // Draw the returned points on the map with customized marker icon
        @Override
        protected void onPostExecute(JSONArray jsonArray) {

            progressDialog.dismiss();
            // 06/02/2015 Yu Sun: It may be wrong that we do NOT distinguish null and empty.
            if( geocodeError ) {
                if( getActivity() != null ) {
                    Toast toast = Toast.makeText(
                            getActivity(), "Internet error or wrong address, " +
                                    "please check and try later",
                            Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                return;
            }
            if( curLocationError ){
                if( getActivity() != null ) {
                    Toast toast = Toast.makeText(
                            getActivity(), "Sorry, cannot get current location",
                            Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                return;
            }

            // Clears all the existing markers on the map
            mMap.clear();

            // Add the destination marker
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(destinLatLng);
            markerOptions.title(destinAddressText);

            // by default, we make the user 'click' the destination marker
            clickedMarer = mMap.addMarker(markerOptions);

            if( jsonArray == null ){

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(destinLatLng)
                        .zoom(14)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                if( getActivity() != null ) {
                    Toast toast = Toast.makeText(getActivity(), "Station request errors," +
                            " please check the address or the internet connection and try later", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                return;
            }
            if( jsonArray.length() <= 0 ){

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(destinLatLng)
                        .zoom(14)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                if( getActivity() != null ) {
                    Toast toast = Toast.makeText(getActivity(), "No stations within " +
                            this.range_radius + "km, " +
                            "please increase the radius", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                return;
            }

            //Else show all the station markers as well and move the map camera accordingly
            DrawMarkersOnMap.drawOnMapMaxTenDifferentColor(
                    (android.support.v7.app.ActionBarActivity) getActivity(),
                    mMap,
                    jsonArray,
                    destinLatLng
            );
        } // end post execute
    }// end range query task

    /**
     * The function of the task is as follows:
     * i) geocode the origin and destination addresses,
     * ii) compute the direction (or called path or route) between the origin
     * and destination and the fuel stations near to the path
     * iii) draw the path and fuel stations on the map
     * and iv) move the focus of the map to the path.
     * v) store the destination address and location in the instance variable
     * 'destinLatLng' and 'destinAddressText', respectively, if the destination
     * address is changed at this stage.
     * vi) close the direction sliding up panel if the query executes successfully.
     *
     * We choose to CHECK the address in the origin_ and destination_autoCompleteTextView
     * each time as 1) it's difficult to keep track of a LatLng object for the origin
     * or destination since the user behaviour is unpredictable and 2) it's easier later for
     * us to move the geocode task to the server.
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
        ProgressDialog progressDialog;

        /**
         * Before retrieving the directions, we display a message telling the user to wait,
         * and retrieve the user preferred path distance (store it in this.path_distance).
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //read and store the user preferred path distance
            String path_dist = PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getString(
                            getString(R.string.pref_key_path_distance),
                            getString(R.string.pref_default_path_distance)
                    );
            this.path_distance = Double.valueOf(path_dist);
            // update the current location
            //Location location = MyLocation.getMyLocation();
            GPSTracker gpsTracker = new GPSTracker( getActivity() );
            Location location = gpsTracker.getLocation();
            isCurrentLocationEnabled = gpsTracker.canGetLocation();
            if( location != null )
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            // progress dialog
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Please wait...");
            progressDialog.setMessage("Retrieving the direction and fuel stations...");
            progressDialog.setCancelable(false);
            progressDialog.show();
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
            double o_lat, o_lng, d_lat, d_lng;

            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(getActivity());

            try {
                /////// If any error occurs, we don't do the path query and return ///////
                /////// the error object directly ////////////////////////////////////////
                Log.v(LOG_TAG, "Geocoding the origin...");
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
                }else if( origin_addr.equals( destinAddressText ) ){
                    if( destinLatLng == null ){
                        try {
                            errorObj.put(this.ORIGIN_ERROR_KEY, this.ERROR);
                        } catch (JSONException e) {
                        }
                        return errorObj;
                    }
                    //else
                    o_lat = destinLatLng.latitude;
                    o_lng = destinLatLng.longitude;
                }
                else {
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
                Log.v(LOG_TAG, "Geocoding the destination...");
                if( destin_addr.equals( currentLocationName ) ){
                    if( currentLocation == null ){
                        try {
                            errorObj.put(this.DESTIN_ERROR_KEY, this.ERROR);
                        } catch (JSONException e) {
                        }
                        return errorObj;
                    }
                    //else
                    d_lat = currentLocation.latitude;
                    d_lng = currentLocation.longitude;
                }else if( destin_addr.equals( destinAddressText ) ) {
                    if( destinLatLng == null ){
                        try {
                            errorObj.put(this.DESTIN_ERROR_KEY, this.ERROR);
                        } catch (JSONException e) {
                        }
                        return errorObj;
                    }
                    //else
                    d_lat = destinLatLng.latitude;
                    d_lng = destinLatLng.longitude;
                }
                else {
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
                    // First store it as the private instance variable
                    Address address = (Address) addresses_d.get(0);
                    destinLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                    destinAddressText = String.format("%s, %s",
                            address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                            address.getCountryName());
                    //
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
                        this.path_distance
                );
                Log.v(LOG_TAG, "done!");
                if( result == null ){
                    try {
                        errorObj.put(RESULT_STATION_KEY, ERROR);
                        errorObj.put(RESULT_DIRECTION_KEY, ERROR);
                    } catch (JSONException json_e) {
                        Log.e(LOG_TAG, "Error for errorObj json");
                    }
                    return errorObj;
                }else{
                    return result;
                }
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
        }

        /**
         * After getting the result, we draw the path and fuel stations on the pathMap
         * and move the focus of the map to the path.
         */
        @Override
        protected void onPostExecute(JSONObject result) {

            progressDialog.dismiss();

            ///////// first check the validness of the geocoding result /////////////
            if (result.optString(ORIGIN_ERROR_KEY).equals(ERROR)) {
                if( getActivity() != null ) {
                    Toast toast = Toast.makeText(getActivity(), "Error origin address, please try another one ",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                return;
            }
            if (result.optString(DESTIN_ERROR_KEY).equals(ERROR)) {
                if( getActivity() != null ) {
                    Toast toast = Toast.makeText(getActivity(), "Error destination address, please try another one ",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                return;
            }

            ///// then check the validness of the Google directions API returned result /////////
            ///// and geocoding connection errors ////////////////
            if (result == null || result.optString(RESULT_DIRECTION_KEY).isEmpty() ||
                    result.optString(RESULT_DIRECTION_KEY).equals(ERROR)) {
                if( getActivity() != null ) {
                    Toast toast = Toast.makeText(getActivity(), "Error getting the directions, please check" +
                                    "the addresses or internet connection and try again",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                return;
            }

            // Clears all the existing markers on the map
            mMap.clear();

            // Add the destination marker
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(destinLatLng);
            markerOptions.title(destinAddressText);

            // by default, we make the user 'click' the destination marker
            clickedMarer = mMap.addMarker(markerOptions);

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
                    .target(directionPoints.get((directionPoints.size() / 100)))
                    .zoom(13)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
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
                if( getActivity() != null ) {
                    Toast toast = Toast.makeText(getActivity(), "No fuel stations near the route, please " +
                            "increase the range and try again", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                return;
            }
            ///////////////////////////////////////////////////////////////////////////////

            // draw the stations on the map
            JSONArray stations = null;
            try {
                stations = result.getJSONArray(RESULT_STATION_KEY);

                //Else show all the stations
                DrawMarkersOnMap.drawOnMapMaxTenDifferentColor(
                        (android.support.v7.app.ActionBarActivity) getActivity(),
                        mMap,
                        stations,
                        directionPoints.get((directionPoints.size() / 100)));

                // Hide the sliding up panel
                directionSliding.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                // And we show the path fuel fab

                return;
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Wrong json array for the fuel stations", e);
                return;
            }
        }
    }

    /**
     * The function of the task is as follows:
     * i) geocode the origin and destination addresses,
     * ii) compute the direction (or called path or route) from the origin to the way point
     * and from the way point to the destination.
     * iii) draw the path and the selected fuel station as a regular marker on the map
     * and iv) move the focus of the map to the path.
     * v) We DONOT store the destination address or location in the instance variable
     * 'destinLatLng' or 'destinAddressText'. Instead we set the instance variable
     * 'showResult' to true if the execution succeeds, which ends the quering cicle by
     * terminating all interaction operations except the 'find' button (an the
     * autoCompleteTextView). When the user 'find' again, we set the 'showResult' to false.
     * vi) close the direction sliding up panel if the query executes successfully.
     */
    private class WayPointTask extends AsyncTask<String, Void, JSONObject>{

        private static final String RESULT_DIRECTION_KEY = "direction";
        private static final String RESULT_DIRECTION_ONE_KEY = "direction_one";
        private static final String RESULT_DIRECTION_TWO_KEY = "direction_two";
        private static final String ERROR = "error";
        private static final String ORIGIN_ERROR_KEY = "origin_error";
        private static final String WAYPOINT_ERROR_KEY = "waypoint_error";
        private static final String DESTIN_ERROR_KEY = "destin_error";
        ProgressDialog progressDialog;

        /**
         * Before retrieving the directions, we display a message telling the user to wait,
         * and retrieve the user preferred path distance (store it in this.path_distance).
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // update the current location
            //Location location = MyLocation.getMyLocation();
            GPSTracker gpsTracker = new GPSTracker( getActivity() );
            Location location = gpsTracker.getLocation();
            isCurrentLocationEnabled = gpsTracker.canGetLocation();
            if( location != null )
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

            // progress dialog
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Please wait...");
            progressDialog.setMessage("Retrieving the detour route...");
            progressDialog.setCancelable(false);
            progressDialog.show();
//
//            // Show the auto complete address with a Toast at the bottom of the screen
//            if( getActivity() != null ) {
//                Toast toast = Toast.makeText(getActivity(), "Retrieving the route, please wait...",
//                        Toast.LENGTH_SHORT);
//                toast.setGravity(Gravity.CENTER, 0, 0);
//                toast.show();
//            }
        }

        /**
         * TODO add comments
         */
        @Override
        protected JSONObject doInBackground(String... originWayPointDestination) {

            JSONObject result = null;
            JSONObject result_one = null;
            JSONObject result_two = null;
            JSONObject errorObj = new JSONObject();

            String origin_addr = originWayPointDestination[0];
            String waypoint_addr = originWayPointDestination[1];
            String destin_addr = originWayPointDestination[2];

            double o_lat, o_lng, w_lat, w_lng, d_lat, d_lng;

            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(getActivity());

            try {
                /////// If any error occurs, we don't do the path query and return ///////
                /////// the error object directly ////////////////////////////////////////
                //////////////// For origin /////////////////
                Log.v(LOG_TAG, "Geocoding the origin...");
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
                }else if( origin_addr.equals( destinAddressText ) ){
                    if( destinLatLng == null ){
                        try {
                            errorObj.put(this.ORIGIN_ERROR_KEY, this.ERROR);
                        } catch (JSONException e) {
                        }
                        return errorObj;
                    }
                    //else
                    o_lat = destinLatLng.latitude;
                    o_lng = destinLatLng.longitude;
                }
                else if( origin_addr.equals( wayPointName ) ){
                    if( wayPointLatLng == null ){
                        try {
                            errorObj.put(this.ORIGIN_ERROR_KEY, this.ERROR);
                        } catch (JSONException e) {
                        }
                        return errorObj;
                    }
                    //else
                    o_lat = wayPointLatLng.latitude;
                    o_lng = wayPointLatLng.longitude;
                }
                else {
                    List<Address> addresses_o = geocoder.getFromLocationName(origin_addr, 1);
                    if (addresses_o == null || addresses_o.size() == 0) {
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
                //////////////// For way point ///////////////////
                Log.v(LOG_TAG, "Geocoding the waypoint...");
                if( waypoint_addr.equals( currentLocationName ) ){
                    if( currentLocation == null ){
                        try {
                            errorObj.put(this.WAYPOINT_ERROR_KEY, this.ERROR);
                        } catch (JSONException e) {
                        }
                        return errorObj;
                    }
                    //else
                    w_lat = currentLocation.latitude;
                    w_lng = currentLocation.longitude;
                }
                else if( waypoint_addr.equals( wayPointName ) ){
                    if( wayPointLatLng == null ){
                        try {
                            errorObj.put(this.WAYPOINT_ERROR_KEY, this.ERROR);
                        } catch (JSONException e) {
                        }
                        return errorObj;
                    }
                    //else
                    w_lat = wayPointLatLng.latitude;
                    w_lng = wayPointLatLng.longitude;
                }
                else if( waypoint_addr.equals( destinAddressText ) ){
                    if( destinLatLng == null ){
                        try {
                            errorObj.put(this.WAYPOINT_ERROR_KEY, this.ERROR);
                        } catch (JSONException e) {
                        }
                        return errorObj;
                    }
                    //else
                    w_lat = destinLatLng.latitude;
                    w_lng = destinLatLng.longitude;
                }
                else {
                    List<Address> addresses_w = geocoder.getFromLocationName(waypoint_addr, 1);
                    if (addresses_w == null || addresses_w.size() == 0) {
                        try {
                            errorObj.put(this.WAYPOINT_ERROR_KEY, this.ERROR);
                        } catch (JSONException e) {
                        }
                        return errorObj;
                    }
                    //else
                    w_lat = addresses_w.get(0).getLatitude();
                    w_lng = addresses_w.get(0).getLongitude();
                }
                //////////////// For destination /////////////////
                Log.v(LOG_TAG, "Geocoding the destination...");
                if( destin_addr.equals( wayPointName ) ){
                    if( wayPointLatLng == null ){
                        try {
                            errorObj.put(this.DESTIN_ERROR_KEY, this.ERROR);
                        } catch (JSONException e) {
                        }
                        return errorObj;
                    }
                    //else
                    d_lat = wayPointLatLng.latitude;
                    d_lng = wayPointLatLng.longitude;
                }
                else if( destin_addr.equals( currentLocationName ) ){
                    if( currentLocation == null ){
                        try {
                            errorObj.put(this.DESTIN_ERROR_KEY, this.ERROR);
                        } catch (JSONException e) {
                        }
                        return errorObj;
                    }
                    //else
                    d_lat = currentLocation.latitude;
                    d_lng = currentLocation.longitude;
                }else if( destin_addr.equals( destinAddressText ) ) {
                    if( destinLatLng == null ){
                        try {
                            errorObj.put(this.DESTIN_ERROR_KEY, this.ERROR);
                        } catch (JSONException e) {
                        }
                        return errorObj;
                    }
                    //else
                    d_lat = destinLatLng.latitude;
                    d_lng = destinLatLng.longitude;
                }
                else {
                    List<Address> addresses_d = geocoder.getFromLocationName(destin_addr, 1);
                    if (addresses_d == null || addresses_d.size() == 0) {
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
                result_one = pq.executeQuery(
                        o_lat,
                        o_lng,
                        w_lat,
                        w_lng,
                        0.0 // for direction only
                );
                result_two = pq.executeQuery(
                        w_lat,
                        w_lng,
                        d_lat,
                        d_lng,
                        0.0 // for direction only
                );
                Log.v(LOG_TAG, "done!");

                try{
                    result = new JSONObject();
                    if( result_one == null || result_one.optString(RESULT_DIRECTION_KEY).isEmpty() ||
                            result_one.optString(RESULT_DIRECTION_KEY).equals(ERROR)){
                        result.put(this.RESULT_DIRECTION_ONE_KEY, ERROR);
                    }else {
                        // at this point result_one must be a valid json object with valid result
                        String direction_one = result_one.getString(RESULT_DIRECTION_KEY);
                        result.put(this.RESULT_DIRECTION_ONE_KEY, direction_one);
                    }
                    if( result_two == null || result_two.optString(RESULT_DIRECTION_KEY).isEmpty() ||
                            result_two.optString(RESULT_DIRECTION_KEY).equals(ERROR)) {
                        result.put(this.RESULT_DIRECTION_TWO_KEY, ERROR);
                    }else {
                        String direction_two = result_two.getString(RESULT_DIRECTION_KEY);
                        // at this point result_two must be a valid json object with valid result
                        result.put(this.RESULT_DIRECTION_TWO_KEY, direction_two);
                    }
                }catch (JSONException e){
                    Log.e(LOG_TAG, "Error for errorObj json", e);
                }
            } catch (Exception e) {
                // This error is the same as result = pq.executeQuery returns null.
                // In most cases, the error is caused by internet connection, i.e.,
                // IOException. Therefore, we use the same double error json object
                // to represent this error type
                Log.e(LOG_TAG, "Errors occur while retrieving the direction", e);
                try {
                    errorObj.put(RESULT_DIRECTION_ONE_KEY, ERROR);
                    errorObj.put(RESULT_DIRECTION_TWO_KEY, ERROR);
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
                if( getActivity() != null ) {
                    Toast toast = Toast.makeText(getActivity(), "Error getting origin location, please try another one",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                progressDialog.dismiss();
                return;
            }
            if (result.optString(WAYPOINT_ERROR_KEY).equals(ERROR)) {
                if( getActivity() != null ) {
                    Toast toast = Toast.makeText(getActivity(), "Error getting fuel station location, please try another one",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                progressDialog.dismiss();
                return;
            }
            if (result.optString(DESTIN_ERROR_KEY).equals(ERROR)) {
                if( getActivity() != null ) {
                    Toast toast = Toast.makeText(getActivity(), "Error getting destination location, please try another one",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                progressDialog.dismiss();
                return;
            }

            ///// then check the validness of the Google directions API returned result /////////
            ///// and geocoding connection errors ////////////////
            if (result == null ||
                    result.optString(RESULT_DIRECTION_ONE_KEY).isEmpty() ||
                    result.optString(RESULT_DIRECTION_ONE_KEY).equals(ERROR) ||
                    result.optString(RESULT_DIRECTION_TWO_KEY).isEmpty() ||
                    result.optString(RESULT_DIRECTION_TWO_KEY).equals(ERROR) ) {
                if( getActivity() != null ) {
                    Toast toast = Toast.makeText(getActivity(), "Error getting the directions, please check" +
                                    "the internet connection and try again later",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                progressDialog.dismiss();
                return;
            }

            // Clears all the existing markers on the map
            mMap.clear();

            ////////////////// Draw the two paths on the map //////////////////
            // At this step, the google directions API call (must) have returned the valid route
            ArrayList<LatLng> directionPoints_one = null;
            ArrayList<LatLng> directionPoints_two = null;
            try {
                directionPoints_one =
                        DecodeDirection.getAllDirectionPoints(result.getString(RESULT_DIRECTION_ONE_KEY));
                directionPoints_two =
                        DecodeDirection.getAllDirectionPoints(result.getString(RESULT_DIRECTION_TWO_KEY));
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error wrong json formats in directions", e);
                progressDialog.dismiss();
                return;
            }

            // redraw the clicked marker on the map
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(wayPointLatLng)
                    .title(clickedMarer.getTitle())
                    .snippet(clickedMarer.getSnippet());
            mMap.addMarker(markerOptions);

            Polyline newPolyline;
            PolylineOptions rectLine = new PolylineOptions().width(12).color(Color.BLUE);
            for (int i = 0; i < directionPoints_one.size(); i++) {
                rectLine.add(directionPoints_one.get(i));
            }
            for (int i = 0; i < directionPoints_two.size(); i++) {
                rectLine.add(directionPoints_two.get(i));
            }
            //return a reference for future use
            newPolyline = mMap.addPolyline(rectLine);
            //////////////////////////////////////////////////////////////////////

            ////////////// move the focus of the map to the middle of the path //////////////
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(directionPoints_one.get((directionPoints_one.size() / 100)))
                    .zoom(13)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            /////////////////////////////////////////////////////////////////////////////////
            showResult = true;
            progressDialog.dismiss();
            // Hide the sliding up panel
            directionSliding.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            // And we show neither fab
        }

    }

    /**
     * This task stores the user input address in the background.
     * It has only one parameter: the user input address.
     * When error occurs, we do noting currently.
     */
    private class AddressHistoryTask extends AsyncTask<String, Void, Boolean> {

        public AddressHistoryTask(){
        }

        @Override
        protected Boolean doInBackground(String... params) {

            String address = params[0];
            if( address.equals( lastAddress ) || address.equals( currentLocationName )
                || address.equals( wayPointName) || address.equals( destinAddressText ) ){

                return false;

            }else{ // we store the input address into the server

                lastAddress = address; //update the last address
                SearchLocationHistory.store(address);
                return true;
            }
        }

    }
}