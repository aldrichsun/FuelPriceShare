package unimelb.cis.spatialanalytics.fuelpriceshare.maps.myLocation;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Yu Sun on 26/02/2015.
 * Used exclusively (only) for ContributePriceFragment to get current user location
 * when choosing the fuel stations nearby.
 */
public class MyLocation {

//    private static LatLng currentLocation = null;
    private static LocationManager locationManager;

    public MyLocation(LocationManager locationManager){
        this.locationManager = locationManager;
//        setUpMyLocation();
    }

//    public static LatLng get(){
//        return currentLocation;
//    }

    /**
     * TODO add comments
     * @return
     */
    public static Location getMyLocation(){

        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();
        // Get the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

//        ////// set up the current location change listener /////////
//        setUpLocationChangeListener();
//        ////////////////////////////////////////////////////////////

        // Get the initial Current Location
        Location myLocation = locationManager.getLastKnownLocation(provider);
//        currentLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        return myLocation;
    }

//    // TODO Untested code: test it
//    private void setUpLocationChangeListener(){
//
//        // The minimum time (in miliseconds) the system will wait until checking if the location changed
//        int minTime = 60000; // 1 min
//        // The minimum distance (in meters) traveled until you will be notified
//        float minDistance = 15;
//        // Create a new instance of the location listener
//        MyLocationListener myLocListener = new MyLocationListener();
//        // Get the criteria you would like to use
//        Criteria criteria = new Criteria();
//        criteria.setPowerRequirement(Criteria.POWER_LOW);
//        criteria.setAccuracy(Criteria.ACCURACY_FINE);
//        criteria.setAltitudeRequired(false);
//        criteria.setBearingRequired(false);
//        criteria.setCostAllowed(true);  // may require data transformation from ISP
//        criteria.setSpeedRequired(false);
//        // Get the best provider from the criteria specified, and false to say it can turn the provider on if it isn't already
//        String bestProvider = locationManager.getBestProvider(criteria, false);
//        // Request location updates
//        locationManager.requestLocationUpdates(bestProvider, minTime, minDistance, myLocListener);
//    }
//
//    private class MyLocationListener implements LocationListener {
//
//        @Override
//        public void onLocationChanged(Location location){
//            if (location != null){
//                // Do something knowing the location changed by the distance you requested
//                currentLocation = null;
//                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
//            }
//        }
//
//        @Override
//        public void onProviderDisabled(String arg0){
//            // Do something here if you would like to know when the provider is disabled by the user
//        }
//
//        @Override
//        public void onProviderEnabled(String arg0){
//            // Do something here if you would like to know when the provider is enabled by the user
//        }
//
//        @Override
//        public void onStatusChanged(String arg0, int arg1, Bundle arg2){
//            // Do something here if you would like to know when the provider status changes
//        }
//    }
}
