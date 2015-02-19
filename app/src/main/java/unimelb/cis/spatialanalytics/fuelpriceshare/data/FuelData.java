package unimelb.cis.spatialanalytics.fuelpriceshare.data;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import unimelb.cis.spatialanalytics.fuelpriceshare.config.ConfigConstant;

/**
 * Created by hanl4 on 29/01/2015.
 * handle all fuel information
 */
public class FuelData {


    private ArrayList<JSONObject> fuelJsonList = new ArrayList<JSONObject>();//store all the fuel information: price & type
    private ArrayList<JSONObject> petroStationsJsonList = new ArrayList<JSONObject>();//store all the detected petro station info
    private ArrayList<String> allFuelTypeList = new ArrayList<String>();// all the fuel types
    private ArrayList<String> petroStationsNameList = new ArrayList<String>();//only contain the name info of all the detected petro station


    private final String TAG = "FuelData";


    /**
     * Clear all the data to reset
     */
    public void clearData() {

        fuelJsonList = new ArrayList<JSONObject>();
        petroStationsJsonList = new ArrayList<JSONObject>();
        allFuelTypeList = new ArrayList<String>();
        petroStationsNameList = new ArrayList<String>();

    }


    /**
     * Convert String list to CharSequence mainly for AlertDialog.Builder builder setSingleChoiceItems method
     *
     * @return CharSequence[]
     */
    public CharSequence[] convertPetroStationNameList2CharSequence() {

        final CharSequence[] items = petroStationsNameList.toArray(new CharSequence[petroStationsNameList.size()]);
        return items;
    }

    /**
     * Wrap all the required fuel information for uploading to the server to update the records
     *
     * @param transactionID the transaction id of the action of  contributing price
     * @return JSONObject but encoded in String format
     */
    public String getUploadDataInfo(String transactionID) {
        try {
            final String KEY_ID = "id";
            JSONObject json = new JSONObject();
            json.put("transaction_id", transactionID);
            json.put("user_id", Users.id);
            json.put("latitude", 1d);
            json.put("longitude", 1d);
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
            parse petro station information
             */
            JSONArray petroStationJsonArray = replyJson.getJSONArray("petro_station");

            for (int i = 0; i < petroStationJsonArray.length(); i++) {
                petroStationsJsonList.add(petroStationJsonArray.getJSONObject(i));

                petroStationsNameList.add(petroStationJsonArray.getJSONObject(i).getString(ConfigConstant.KEY_PETRO_STATION_NAME));
            }


            /*
            parse fuel information
             */
            JSONArray fuelJsonArray = replyJson.getJSONArray("fuel");
            for (int i = 0; i < fuelJsonArray.length(); i++) {
                fuelJsonList.add(fuelJsonArray.getJSONObject(i));
            }

            /*
            parse all fuel type information
             */
            JSONArray allFuelTypeJsonArray = replyJson.getJSONArray("fuel_type");

            for (int i = 0; i < allFuelTypeJsonArray.length(); i++) {
                allFuelTypeList.add(allFuelTypeJsonArray.getString(i));
            }


        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }


    }


    public ArrayList<JSONObject> getPetroStationsJsonList() {
        return petroStationsJsonList;
    }

    public void setPetroStationsJsonList(ArrayList<JSONObject> petroStationsJsonList) {
        this.petroStationsJsonList = petroStationsJsonList;
    }

    public ArrayList<String> getPetroStationsNameList() {
        return petroStationsNameList;
    }

    public void setPetroStationsNameList(ArrayList<String> petroStationsNameList) {
        this.petroStationsNameList = petroStationsNameList;
    }

    public ArrayList<JSONObject> getFuelJsonList() {
        return fuelJsonList;
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
