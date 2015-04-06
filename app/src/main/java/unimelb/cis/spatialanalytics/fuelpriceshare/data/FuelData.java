package unimelb.cis.spatialanalytics.fuelpriceshare.data;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;

import unimelb.cis.spatialanalytics.fuelpriceshare.config.ConfigConstant;
import unimelb.cis.spatialanalytics.fuelpriceshare.maps.myLocation.GPSTracker;

/**
 * Created by hanl4 on 29/01/2015.
 * handle all fuel information (parsing the fuel information : fuel price, type, petrol station info, etc from the
 * response of the server.
 */
public class FuelData {


    private ArrayList<JSONObject> fuelJsonList = new ArrayList<JSONObject>();//store all the fuel information: price & type
    private ArrayList<JSONObject> petrolStationsJsonList = new ArrayList<JSONObject>();//store all the detected petrol station info
    private ArrayList<String> allFuelTypeList = new ArrayList<String>();// all the fuel types
    private ArrayList<String> petrolStationsNameList = new ArrayList<String>();//only contain the name info of all the detected petrol station

    private double latitude;
    private double longitude;

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    private final String TAG = "FuelData";


    /**
     * Clear all the data to reset
     */
    public void clearData() {

        fuelJsonList = new ArrayList<JSONObject>();
        petrolStationsJsonList = new ArrayList<JSONObject>();
        allFuelTypeList = new ArrayList<String>();
        petrolStationsNameList = new ArrayList<String>();

    }


    /**
     * Convert String list to CharSequence mainly for AlertDialog.Builder builder setSingleChoiceItems method
     *
     * @return CharSequence[]
     */
    public CharSequence[] convertPetrolStationNameList2CharSequence() {

        final CharSequence[] items = petrolStationsNameList.toArray(new CharSequence[petrolStationsNameList.size()]);
        return items;
    }

    /**
     * Wrap all the required fuel information for uploading to the server to update the records
     *
     * @param transactionID the transaction id of the action of  contributing price
     * @return JSONObject but encoded in String format
     */
    public String getUploadDataInfo(String transactionID,Context context) {
        try {
            final String KEY_ID = "id";
            JSONObject json = new JSONObject();
            json.put(ConfigConstant.KEY_CONTRIBUTE_PRICE_TRANSACTION_ID, transactionID);
            json.put(ConfigConstant.KEY_UID, Users.id);

            GPSTracker gps = new GPSTracker(context);
            gps.getLocation(); //added by Yu Sun on 04/03/2015
            if(gps.canGetLocation()) {
                json.put(ConfigConstant.KEY_CAN_GET_LOCATION,true);

            }else
            {
                json.put(ConfigConstant.KEY_CAN_GET_LOCATION,false);
            }

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            json.put(ConfigConstant.KEY_LATITUDE, latitude);
            json.put(ConfigConstant.KEY_LONGITUDE, longitude);



            //BY SUN YU
          /*  Location currentLocation = new MyLocation(context).getMyLocation();
            json.put(ConfigConstant.KEY_LATITUDE, currentLocation.getLatitude());
            json.put(ConfigConstant.KEY_LONGITUDE, currentLocation.getLongitude());*/
            return json.toString();
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
            return null;
        }


    }


    /**
     * Parse the response from the server after uploading the fuel image, and mapping the information locally.
     *
     * @param replyJson the response from the server
     */
    public void parseFuelPriceImageReplyData(JSONObject replyJson) {
        try {
            Log.d(TAG, "parse fuel image response data from server");
            clearData();//reset data
            /*
            parse petrol station information
             */
            JSONArray petrolStationJsonArray = replyJson.getJSONArray(ConfigConstant.KEY_PETROL_STATION);

            //////////////////////////////////////////////////////////////////////
            // Modified by Yu Sun on 06/04/2015 to show the fuel station brand instead of name
            HashSet<String> station_brand = new HashSet<>();

            for (int i = 0; i < petrolStationJsonArray.length(); i++) {
                petrolStationsJsonList.add(petrolStationJsonArray.getJSONObject(i));

//                petrolStationsNameList.add(petrolStationJsonArray.getJSONObject(i)
//                        .getString(ConfigConstant.KEY_PETROL_STATION_NAME));

                String brand = petrolStationJsonArray.getJSONObject(i)
                        .getString(ConfigConstant.KEY_PETROL_STATION_BRAND);
                if( !station_brand.contains( brand ) ) { // a new brand
                    petrolStationsNameList.add( brand );
                    station_brand.add( brand );
                }
            }

            station_brand.clear();
            ///////////////////////////////////////////////////////////////////////


            /*
            parse fuel information
             */
            JSONArray fuelJsonArray = replyJson.getJSONArray(ConfigConstant.KEY_FUEL);
            for (int i = 0; i < fuelJsonArray.length(); i++) {
                fuelJsonList.add(fuelJsonArray.getJSONObject(i));
            }

            /*
            parse all fuel type information
             */
            JSONArray allFuelTypeJsonArray = replyJson.getJSONArray(ConfigConstant.KEY_ALL_FUEL_BRAND);

            for (int i = 0; i < allFuelTypeJsonArray.length(); i++) {
                allFuelTypeList.add(allFuelTypeJsonArray.getString(i));
            }


        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }


    }


    public ArrayList<JSONObject> getPetrolStationsJsonList() {
        return petrolStationsJsonList;
    }

    public JSONArray getPetrolStationJSONArray()
    {
        JSONArray jsonArray = new JSONArray();
        for(JSONObject jsonObject:petrolStationsJsonList)
        {
            jsonArray.put(jsonObject);
        }

        return jsonArray;


    }


    public void setPetrolStationsJsonList(ArrayList<JSONObject> petrolStationsJsonList) {
        this.petrolStationsJsonList = petrolStationsJsonList;
    }

    public ArrayList<String> getPetrolStationsNameList() {
        return petrolStationsNameList;
    }

    public void setPetrolStationsNameList(ArrayList<String> petrolStationsNameList) {
        this.petrolStationsNameList = petrolStationsNameList;
    }

    public ArrayList<JSONObject> getFuelJsonList() {
        return fuelJsonList;
    }


    public JSONArray getFuelJsonArray()
    {
        JSONArray jsonArray = new JSONArray();
        for(JSONObject jsonObject:fuelJsonList)
        {
            jsonArray.put(jsonObject);
        }

        return jsonArray;


    }

    public void setFuelJsonList(ArrayList<JSONObject> fuelJsonList) {
        this.fuelJsonList = fuelJsonList;
    }

    public ArrayList<String> getAllFuelTypeList() {
        return allFuelTypeList;
    }

    public void setAllFuelTypeList(ArrayList<String> allFuelTypeList) {
        this.allFuelTypeList = allFuelTypeList;
    }


}
