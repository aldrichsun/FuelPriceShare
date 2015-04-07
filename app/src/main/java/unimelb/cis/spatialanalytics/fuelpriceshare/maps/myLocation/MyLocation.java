package unimelb.cis.spatialanalytics.fuelpriceshare.maps.myLocation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import unimelb.cis.spatialanalytics.fuelpriceshare.fragment.MapFragment;

/**
 * Created by Yu Sun on 26/02/2015.
 * Used exclusively (only) for ContributePriceFragment to get current user location
 * when choosing the fuel stations nearby.
 */
//@Deprecated // by Yu Sun and Han Li on 04/03/2015
// Re-used by Yu Sun on 07/04/2015
public class MyLocation {

//    private static LocationManager locationManager;
//    private static Context context;
    private static  String LOG_TAG = MyLocation.class.getSimpleName();

//    @Deprecated
//    public MyLocation (Context context){
//        this.context=context;
//        this.locationManager= (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
//        //isGPSEnabled();
//    }

    /**
     * Get the current location from the best location service provider.
     * @return
     * i) the current location
     * ii) null if error ocurs
     */
    public static LatLng getMyLocation(){
        Log.v(LOG_TAG, "The current location is " + MapFragment.currentLocation.toString());
        return MapFragment.currentLocation;
    }




    /**
     * Get the current location from the best location service provider.
     * @return
     * i) the current location
     * ii) null if error ocurs
     */
//    @Deprecated
//    public static Location getMyLocation_old(){
//        //before fetching the location, we need to enable the GPS server.
//        //isGPSEnabled();
//
//        // Create a criteria object to retrieve provider
//        Criteria criteria = new Criteria();
//        // Get the name of the best provider
//        String provider = locationManager.getBestProvider(criteria, true);
//
//
//        // Get the initial Current Location
//        Location myLocation = locationManager.getLastKnownLocation(provider);
//        return myLocation;
//    }

//    @Deprecated //by Yu Sun on 07/04/2015
//    public static void isGPSEnabled()
//    {
//        Log.e(LOG_TAG,"check GPS");
//        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            //Ask the user to enable GPS
//            AlertDialog.Builder builder = new AlertDialog.Builder(context);
//            builder.setTitle("Location Manager");
//            builder.setMessage("Would you like to enable GPS?");
//            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    //Launch settings, allowing user to make a change
//                    Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                    context.startActivity(i);
//                }
//            });
//            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    //No location service, no Activity
//                    dialog.dismiss();
//                }
//            });
//            builder.create().show();
//        }
//    }

}
