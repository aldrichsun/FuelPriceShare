package unimelb.cis.spatialanalytics.fuelpriceshare;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import unimelb.cis.spatialanalytics.fuelpriceshare.fragment.ContributePriceFragment;
import unimelb.cis.spatialanalytics.fuelpriceshare.fragment.MapFragment;
import unimelb.cis.spatialanalytics.fuelpriceshare.fragment.PathFragment;
import unimelb.cis.spatialanalytics.fuelpriceshare.fragment.ProfileFragment;
import unimelb.cis.spatialanalytics.fuelpriceshare.fragment.RangeFragment;
import unimelb.cis.spatialanalytics.fuelpriceshare.maps.DrawOnMap.DrawMarkersOnMap;
import unimelb.cis.spatialanalytics.fuelpriceshare.maps.myLocation.GPSTracker;
import unimelb.cis.spatialanalytics.fuelpriceshare.maps.myLocation.MyLocation;
import unimelb.cis.spatialanalytics.fuelpriceshare.settings.SettingsActivity;

/**
 * Created by Yu Sun on 17/02/2015.
 * We release the prototype 1.0 on 04/03/2015.
 */
public class MainActivity extends ActionBarActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private ActionBar actionBar;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private String[] mDrawerItemStrings;

    private static final int FRAGMENT_MAP = 0;
//    private static final int FRAGMENT_RANGE_SEARCH = 1;
//    private static final int FRAGMENT_PATH_SEARCH = 2;

    private static final int FRAGMENT_PROFILE = 1;
    private static final int FRAGMENT_CONTRIBUTE = 2;

//    private static final int ACTIVITY_SETTING = 4;

    private Fragment fragment = null;
    private Fragment mapFragment = null;
    //    private Fragment rangeFragment = null;
//    private Fragment pathFragment = null;
    private Fragment profileFragment = null;
    private Fragment contributeFragment = null;

//    private MyLocation myLocation = null;

    private int PRESENT_FRAGMENT_ID;
    private final String PRESENT_FRAGMENT_ID_KEY = "present_fragment_id";
    private final String LAUNCH_TIME_KEY = "launch_time";
    private final int MAX_TIME_SHOW_DRAWER_AFTER_LAUNCH = 1;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        Log.e(LOG_TAG, "on create");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerItemStrings = getResources().getStringArray(R.array.nav_drawer_items);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mDrawerItemStrings));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        actionBar = this.getSupportActionBar();
//
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
// Yu Sun 19/02/2015 No need to use for v7.  R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                actionBar.setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                actionBar.setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // Yu Sun: always close the soft key-pad after launch
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setUpFragments();
        setUpCurrentLocation();

        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (savedInstanceState == null) {

            // Yu Sun 02/03/2015: Changed according to Rui's comments
            //int last_display_fragment_id = 0;
            //last_display_fragment_id = pref.getInt(PRESENT_FRAGMENT_ID_KEY, 0);
            //selectItem(last_display_fragment_id);
            selectItem(0);

            int launchTime = pref.getInt(LAUNCH_TIME_KEY, 0);
            //Log.v(LOG_TAG, "Launch time:" + launchTime);
            if (launchTime < MAX_TIME_SHOW_DRAWER_AFTER_LAUNCH) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
                pref.edit().putInt(LAUNCH_TIME_KEY, ++launchTime).commit();
                pref.edit().apply();
            }
        }
    }

    private void setUpFragments() {

        /////////// Create all the fragments to be used ////////////
        mapFragment = new MapFragment();
//        rangeFragment = new RangeFragment();
//        pathFragment = new PathFragment();
        profileFragment = new ProfileFragment();
        contributeFragment = new ContributePriceFragment();
    }

    private void setUpCurrentLocation() {
        // Get LocationManager object from System Service LOCATION_SERVICE
        //myLocation = new MyLocation(this);

        // remind the user to open the GPS settings
        GPSTracker gpsTracker = new GPSTracker(this);
        gpsTracker.checkGetLocationStatus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu items for use in the action bar
        // MenuInflater inflater = getMenuInflater();
        // inflater.inflate(R.menu.menu_navdrawer, menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

//        // If the nav drawer is open, hide action items related to the content view
//        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
//        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
//        return super.onPrepareOptionsMenu(menu);

        // If the nav drawer is open, hide action items related to the content view

        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);

        /**
         * Handle Fragment Menu if special requirements are needed
         */
        switch (PRESENT_FRAGMENT_ID) {
            case FRAGMENT_MAP:
                break;
//            case FRAGMENT_RANGE_SEARCH:
//                break;
//            case FRAGMENT_PATH_SEARCH:
//                break;
            case FRAGMENT_CONTRIBUTE:
                if (drawerOpen)
                    menu.setGroupVisible(R.id.menu_group, false);
                else
                    menu.setGroupVisible(R.id.menu_group, ContributePriceFragment.isMenuVisible);
                break;
            case FRAGMENT_PROFILE:
                break;
            default:
                Log.e(LOG_TAG, "No such case id");
                break;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    // Handle presses on the action bar items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {

        // update the main content by replacing fragments

        // Han Li and Yu Sun 26/02/2015: close the soft keypad
        if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
            InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }

        PRESENT_FRAGMENT_ID = position;
        //if(position!=FRAGMENT_CONTRIBUTE)
        // ContributePriceFragment.isMenuVisible=false;

//        if (mapFragment != null && rangeFragment != null && pathFragment != null
//                && contributeFragment != null && profileFragment != null ) {
        if (mapFragment != null && contributeFragment != null && profileFragment != null) {

            //FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            //fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            setTitle(mDrawerItemStrings[position]);
            mDrawerLayout.closeDrawer(mDrawerList);

            fragment = null;
            switch (position) {
                case FRAGMENT_MAP:
                    if (mapFragment.isAdded()) {
                        fragmentTransaction.show(mapFragment);
                    } else {
                        fragmentTransaction.add(R.id.content_frame, mapFragment);
                    }
                    if (profileFragment.isAdded()) {
                        fragmentTransaction.hide(profileFragment);
                    }
                    if (contributeFragment.isAdded()) {
                        fragmentTransaction.hide(contributeFragment);
                    }
                    fragment = mapFragment;
                    break;
//                case FRAGMENT_RANGE_SEARCH:
//                    if( rangeFragment.isAdded() ){
//                        fragmentTransaction.show(rangeFragment);
//                    }else{
//                        fragmentTransaction.add(R.id.content_frame, rangeFragment);
//                    }
//                    if(pathFragment.isAdded()){fragmentTransaction.hide(pathFragment);}
//                    if(profileFragment.isAdded()){fragmentTransaction.hide(profileFragment);}
//                    if(contributeFragment.isAdded()){fragmentTransaction.hide(contributeFragment);}
//                    fragment = rangeFragment;
//                    break;
//                case FRAGMENT_PATH_SEARCH:
//                    if( pathFragment.isAdded() ){
//                        fragmentTransaction.show(pathFragment);
//                    }else{
//                        fragmentTransaction.add(R.id.content_frame, pathFragment);
//                    }
//                    if(rangeFragment.isAdded()){fragmentTransaction.hide(rangeFragment);}
//                    if(profileFragment.isAdded()){fragmentTransaction.hide(profileFragment);}
//                    if(contributeFragment.isAdded()){fragmentTransaction.hide(contributeFragment);}
//                    fragment = pathFragment;
//                    break;
                case FRAGMENT_PROFILE:
                    if (profileFragment != null)
                        ((ProfileFragment) profileFragment).updateCredit();
                    if (profileFragment.isAdded()) {
                        fragmentTransaction.show(profileFragment);
                    } else {
                        fragmentTransaction.add(R.id.content_frame, profileFragment);
                    }
//                    if(rangeFragment.isAdded()){fragmentTransaction.hide(rangeFragment);}
//                    if(pathFragment.isAdded()){fragmentTransaction.hide(pathFragment);}
                    if (mapFragment.isAdded()) {
                        fragmentTransaction.hide(mapFragment);
                    }
                    if (contributeFragment.isAdded()) {
                        fragmentTransaction.hide(contributeFragment);
                    }
                    fragment = profileFragment;
                    break;
                case FRAGMENT_CONTRIBUTE:
                    if (contributeFragment.isAdded()) {
                        fragmentTransaction.show(contributeFragment);
                    } else {
                        fragmentTransaction.add(R.id.content_frame, contributeFragment);
                    }
//                    if(rangeFragment.isAdded()){fragmentTransaction.hide(rangeFragment);}
//                    if(pathFragment.isAdded()){fragmentTransaction.hide(pathFragment);}
                    if (mapFragment.isAdded()) {
                        fragmentTransaction.hide(mapFragment);
                    }
                    if (profileFragment.isAdded()) {
                        fragmentTransaction.hide(profileFragment);
                    }
                    fragment = contributeFragment;
                    break;
//                case ACTIVITY_SETTING:
//                    //startActivity(new Intent(this, SettingsActivity.class));
//                    break;
                default:
                    break;
            }

            fragmentTransaction.commit();
            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            setTitle(mDrawerItemStrings[position]);
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        actionBar.setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Receive all the returning results of children activities. Detailed information please refer to the
     * official android programming document
     *
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        /**
         * Directly call the present fragment to handle onActivityResult
         */
//        if( contributeFragment.isAdded() )
//            contributeFragment.onActivityResult(requestCode, resultCode, intent);
//        if( profileFragment.isAdded() )
//            profileFragment.onActivityResult(requestCode, resultCode, intent);
        if (fragment != null)
            fragment.onActivityResult(requestCode, resultCode, intent);

    }

    @Override
    protected void onDestroy() {

        //Log.e(LOG_TAG, "on destroy");

        super.onDestroy();
        pref.edit().putInt(
                PRESENT_FRAGMENT_ID_KEY, PRESENT_FRAGMENT_ID
        ).commit();
        pref.edit().apply();

        // added by Yu Sun 06/04/2015
        DrawMarkersOnMap.clearStations();
    }

//    @Override
//    protected void onResume() {
//        Log.e(LOG_TAG, "on resume");
//        super.onResume();
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        Log.e(LOG_TAG, "on start");
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        Log.e(LOG_TAG, "on pause");
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        Log.e(LOG_TAG, "on stop");
//    }
}
