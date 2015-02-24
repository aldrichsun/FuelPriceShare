package unimelb.cis.spatialanalytics.fuelpriceshare;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;

import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import unimelb.cis.spatialanalytics.fuelpriceshare.R;
import unimelb.cis.spatialanalytics.fuelpriceshare.config.ConfigConstant;
import unimelb.cis.spatialanalytics.fuelpriceshare.fragment.ModifyPricelFragment;
import unimelb.cis.spatialanalytics.fuelpriceshare.fragment.PathFragment;
import unimelb.cis.spatialanalytics.fuelpriceshare.fragment.ProfileFragment;
import unimelb.cis.spatialanalytics.fuelpriceshare.fragment.RangeFragment;
import unimelb.cis.spatialanalytics.fuelpriceshare.settings.SettingsActivity;

/**
 * Created by Yu Sun on 17/02/2015.
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

    private static final int FRAGMENT_RANGE_SEARCH = 0;
    private static final int FRAGMENT_PATH_SEARCH = 1;
    private static final int FRAGMENT_PROFILE = 2;
    private static final int FRAGMENT_CONTRIBUTE = 3;

    private static final int ACTIVITY_SETTING = 4;

    private Fragment rangeFragment = null;
    private Fragment pathFragment = null;
    private Fragment profileFragment = null;
    private Fragment contributeFragment = null;

    private int PRESENT_FRAGMENT_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        if (savedInstanceState == null) {
            selectItem(0);
            mDrawerLayout.openDrawer(Gravity.LEFT);
        }
    }

    private void setUpFragments(){

        /////////// Create all the fragments to be used ////////////
        rangeFragment = new RangeFragment();
        pathFragment = new PathFragment();
        profileFragment = new ProfileFragment();
        contributeFragment = new ModifyPricelFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu items for use in the action bar
        // TODO may check it: 20/02/2015
     // MenuInflater inflater = getMenuInflater();
     //   inflater.inflate(R.menu.menu_navdrawer, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        // TODO may check it: 20/02/2015
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
            case ConfigConstant.KEY_FRAGMENT_HOME:
                break;
            case ConfigConstant.KEY_FRAGMENT_PROFILE:
                break;
            case ConfigConstant.KEY_FRAGMENT_REFINE_PRICE:
                if (drawerOpen)
                    menu.setGroupVisible(R.id.menu_group, false);
                else // TODO discuss with Han
                    menu.setGroupVisible(R.id.menu_group, ModifyPricelFragment.isMenuVisible);
                break;
            case ConfigConstant.KEY_FRAGMENT_OTHER:

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
        switch(item.getItemId()) {
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

        PRESENT_FRAGMENT_ID = position;
        if(position!=FRAGMENT_CONTRIBUTE)
            ModifyPricelFragment.isMenuVisible=false;

        if (rangeFragment != null && pathFragment != null
                && contributeFragment != null && profileFragment != null ) {

            //FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            //fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            setTitle(mDrawerItemStrings[position]);
            mDrawerLayout.closeDrawer(mDrawerList);

            switch (position) {
                case FRAGMENT_RANGE_SEARCH:
                    if( rangeFragment.isAdded() ){
                        fragmentTransaction.show(rangeFragment);
                    }else{
                        fragmentTransaction.add(R.id.content_frame, rangeFragment);
                    }
                    if(pathFragment.isAdded()){fragmentTransaction.hide(pathFragment);}
                    if(profileFragment.isAdded()){fragmentTransaction.hide(profileFragment);}
                    if(contributeFragment.isAdded()){fragmentTransaction.hide(contributeFragment);}
                    break;
                case FRAGMENT_PATH_SEARCH:
                    if( pathFragment.isAdded() ){
                        fragmentTransaction.show(pathFragment);
                    }else{
                        fragmentTransaction.add(R.id.content_frame, pathFragment);
                    }
                    if(rangeFragment.isAdded()){fragmentTransaction.hide(rangeFragment);}
                    if(profileFragment.isAdded()){fragmentTransaction.hide(profileFragment);}
                    if(contributeFragment.isAdded()){fragmentTransaction.hide(contributeFragment);}
                    break;
                case FRAGMENT_PROFILE:
                    if(profileFragment.isAdded()){
                        fragmentTransaction.show(profileFragment);
                    }else{
                        fragmentTransaction.add(R.id.content_frame, profileFragment);
                    }
                    if(rangeFragment.isAdded()){fragmentTransaction.hide(rangeFragment);}
                    if(pathFragment.isAdded()){fragmentTransaction.hide(pathFragment);}
                    if(contributeFragment.isAdded()){fragmentTransaction.hide(contributeFragment);}
                    break;
                case FRAGMENT_CONTRIBUTE:
                    if(contributeFragment.isAdded()){
                        fragmentTransaction.show(contributeFragment);
                    }else{
                        fragmentTransaction.add(R.id.content_frame, contributeFragment);
                    }
                    if(rangeFragment.isAdded()){fragmentTransaction.hide(rangeFragment);}
                    if(pathFragment.isAdded()){fragmentTransaction.hide(pathFragment);}
                    if(profileFragment.isAdded()){fragmentTransaction.hide(profileFragment);}
                    break;
                case ACTIVITY_SETTING:
                    startActivity(new Intent(this, SettingsActivity.class));
                    break;
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
        contributeFragment.onActivityResult(requestCode, resultCode, intent);
        //fragment.onActivityResult(requestCode, resultCode, intent);

    }
}
