package unimelb.cis.spatialanalytics.fuelpriceshare.data;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import unimelb.cis.spatialanalytics.fuelpriceshare.config.ConfigConstant;
import unimelb.cis.spatialanalytics.fuelpriceshare.maps.myLocation.GPSTracker;
import unimelb.cis.spatialanalytics.fuelpriceshare.maps.myLocation.MyLocation;

/**
 * Created by hanl4 on 29/01/2015.
 * handle all fuel information (parsing the fuel information : fuel price, type, petrol station info, etc from the
 * response of the server.
 */
public class FuelData {


    private ArrayList<JSONObject> fuelJsonList = new ArrayList<JSONObject>();//store all the fuel information: price & type
    private ArrayList<JSONObject> petrolStationsJsonList = new ArrayList<JSONObject>();//store all the detected petrol station info
    private ArrayList<String> allFuelTypeList = new ArrayList<String>();// all the fuel types
    private ArrayList<String> fuelBrandList = new ArrayList<String>();//only contain the name info of all the detected petrol station


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
        fuelBrandList = new ArrayList<String>();

    }


    /**
     * Convert String list to CharSequence mainly for AlertDialog.Builder builder setSingleChoiceItems method
     *
     * @return CharSequence[]
     */
    public CharSequence[] convertPetrolStationNameList2CharSequence() {

        final CharSequence[] items = fuelBrandList.toArray(new CharSequence[fuelBrandList.size()]);
        return items;
    }

    /**
     * Wrap all the required fuel information for uploading to the server to update the records
     *
     * @param transactionID the transaction id of the action of  contributing price
     * @return JSONObject but encoded in String format
     */
    public String getUploadDataInfo(String transactionID, Context context) {
        try {
            final String KEY_ID = "id";
            JSONObject json = new JSONObject();
            json.put(ConfigConstant.KEY_CONTRIBUTE_PRICE_TRANSACTION_ID, transactionID);
            json.put(ConfigConstant.KEY_UID, Users.id);



          /*  //old version to get the current location; doesn't work for Rui's phone
            //but works well for other two test android devices
            GPSTracker gps = new GPSTracker(context);
            gps.getLocation(); //added by Yu Sun on 04/03/2015
            if (gps.canGetLocation()) {
                json.put(ConfigConstant.KEY_CAN_GET_LOCATION, true);

            } else {
                json.put(ConfigConstant.KEY_CAN_GET_LOCATION, false);
            }

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();*/

            LatLng latLng=MyLocation.getMyLocation();
            latitude=latLng.latitude;
            longitude=latLng.longitude;
            json.put(ConfigConstant.KEY_CAN_GET_LOCATION, true);//default to set it to be true always

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

//                fuelBrandList.add(petrolStationJsonArray.getJSONObject(i)
//                        .getString(ConfigConstant.KEY_PETROL_STATION_NAME));

/*
             String brand = petrolStationJsonArray.getJSONObject(i)
                        .getString(ConfigConstant.KEY_PETROL_STATION_BRAND);
                if (!station_brand.contains(brand)) { // a new brand
                    fuelBrandList.add(brand);
                    station_brand.add(brand);
                }*/
            }

            station_brand.clear();
            ///////////////////////////////////////////////////////////////////////


            /*
            parse fuel information
             */
            JSONArray fuelJsonArray = replyJson.getJSONArray(ConfigConstant.KEY_FUEL);
            for (int i = 0; i < fuelJsonArray.length(); i++)
            {
                JSONObject fuelJson=fuelJsonArray.getJSONObject(i);
                fuelJson.put(ConfigConstant.FLAG_IS_SELECTED,false);//for canvas drawing use
                fuelJsonList.add(fuelJson);
            }

/*            *//*
            parse all fuel type information
             *//*
            JSONArray allFuelTypeJsonArray = replyJson.getJSONArray(ConfigConstant.KEY_ALL_FUEL_BRAND);

            for (int i = 0; i < allFuelTypeJsonArray.length(); i++) {
                allFuelTypeList.add(allFuelTypeJsonArray.getString(i));
            }*/

            //get all the fuel type from local
            allFuelTypeList = getAllFuelTypes();


        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }


    }


    /**
     * sort petrol stations based on brand information.
     * 1) if it has multiple petrol stations nearby, sort them based on the distance from the user location;
     * 2) the followings are sorted based on the popularity of the brand
     */
    public void sortBrandList() {
        try {
            Log.d(TAG, "sort brand");
            int len = petrolStationsJsonList.size();
            double[] distArray = new double[len];
            int[] indexArray = new int[len];

            /*
            sort based on the distance: petrol station from the user's current location
             */
            //calculate the distance
            for (int i = 0; i < len; i++) {
                JSONObject fuelStationJson = petrolStationsJsonList.get(i);
                if (fuelStationJson.has(ConfigConstant.KEY_LONGITUDE) && fuelStationJson.has(ConfigConstant.KEY_LATITUDE)) {
                    double station_longitude = fuelStationJson.getDouble(ConfigConstant.KEY_LONGITUDE);
                    double station_latitude = fuelStationJson.getDouble(ConfigConstant.KEY_LATITUDE);
                    double dist = Math.pow(station_longitude - longitude, 2) + Math.pow(station_latitude - latitude, 2);
                    distArray[i] = dist;
                    indexArray[i] = i;
                }
            }

            //sort based on the distance; re-order the list
            fuelBrandList = new ArrayList<String>();//re-set the data
            ArrayList<JSONObject> petrolStationsJsonListTemp = new ArrayList<JSONObject>();//store all the detected petrol station info

            if (distArray.length > 0) {
                int[] sortedIndex = sortWithIndex(distArray, indexArray);

                for (int index : sortedIndex) {
                    JSONObject fuelStationJson = petrolStationsJsonList.get(index);
                    if (fuelStationJson.has(ConfigConstant.KEY_BRAND)) {
                        String brand = fuelStationJson.getString(ConfigConstant.KEY_BRAND);

                        //eliminate the duplications of the same brand; only keep the brand with the minimal
                        //distance from the user
                        if (!fuelBrandList.contains(brand))
                        {
                            fuelBrandList.add(brand);
                            petrolStationsJsonListTemp.add(fuelStationJson);
                        }

                    }
                }

            }
            petrolStationsJsonList = petrolStationsJsonListTemp;//re-assign the values

            /*
            sort the following brand based on popularity by default order given by SUN YU
             */
            List<String> allFuelStationBrands = getAllFuelStationBrands();
            for (String brand : allFuelStationBrands) {
                if (!fuelBrandList.contains(brand)) {
                    fuelBrandList.add(brand);
                    //assign an empty JSONObject to the brand
                    JSONObject newPetrol=new JSONObject();
                    newPetrol.put(ConfigConstant.KEY_BRAND, brand);
                    //newPetrol.put(ConfigConstant.KEY_FUEL_PROVIDED,new JSONArray());
                    petrolStationsJsonList.add(newPetrol);
                }
            }

            Log.i(TAG, petrolStationsJsonList.toString());
            Log.i(TAG, fuelBrandList.toString());


        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }


    }


    /**
     * sort with index
     * Here index array(of length equal to length of d array) contains the numbers from 0 to length of d array
     *
     * @param data
     * @param index
     * @return the sorted index
     */
    private int[] sortWithIndex(double[] data, int[] index) {
        int len = data.length;
        double temp1[] = new double[len];
        int temp2[] = new int[len];
        for (int i = 0; i < len; i++) {
            for (int j = i + 1; j < len; j++) {
                if (data[i] > data[j]) {
                    temp1[i] = data[i];
                    data[i] = data[j];
                    data[j] = temp1[i];

                    temp2[i] = index[i];
                    index[i] = index[j];
                    index[j] = temp2[i];
                }
            }
        }
        return index;
    }


    /**
     * fuel station name & its shorts
     *
     * @return
     */

    public HashMap<String, String> getNameToShortNameMap() {

        // Made according to http://www.gps-data-team.info/poi/australia/petrol/
        HashMap<String, String> nameToShortNameMap = new HashMap<>();
        nameToShortNameMap.put("BP", "BP");
        nameToShortNameMap.put("Caltex", "CTX");
        nameToShortNameMap.put("Shell", "SHL");
        nameToShortNameMap.put("United", "UNT");
        nameToShortNameMap.put("7-Eleven", "7-11");
        nameToShortNameMap.put("Mobil", "MBL");
        nameToShortNameMap.put("Coles Express", "CLZ");
        nameToShortNameMap.put("Woolworths Petrol", "WLS");
        nameToShortNameMap.put("Roadhouses", "RDH");
        nameToShortNameMap.put("E-85 Fuel", "E85");
        return nameToShortNameMap;

    }

    /**
     * all the fuel station brands
     *
     * @return
     */
    public List<String> getAllFuelStationBrands() {
        List<String> allFuelStationBrands = new ArrayList<>();
        allFuelStationBrands.add("BP");
        allFuelStationBrands.add("Caltex");
        allFuelStationBrands.add("Shell");
        allFuelStationBrands.add("United");
        allFuelStationBrands.add("7-Eleven");
        allFuelStationBrands.add("Mobil");
        allFuelStationBrands.add("Coles Express");
        allFuelStationBrands.add("Woolworths Petrol");
        allFuelStationBrands.add("Roadhouses");
        allFuelStationBrands.add("E-85 Fuel");
        return allFuelStationBrands;

    }

    /**
     * get all fuel type list from local (original fetch from the server)
     *
     * @return
     */

    public ArrayList<String> getAllFuelTypes() {

        ArrayList<String> allFuelType = new ArrayList<String>();
        allFuelType.add("E85");
        allFuelType.add("ULP");
        //   allFuelType.add("E10");
        allFuelType.add("PULP");
        allFuelType.add("UPULP");
        allFuelType.add("LPG");
        allFuelType.add("tDiesel");
        allFuelType.add("Biodiesel");
        allFuelType.add("Premium Diesel");
        allFuelType.add("UNLEADED E10");
       // allFuelType.add("Autogas");//added by Han. Might not be necessary.
        return allFuelType;

    }


    /**
     * Setters and Getters
     */

    public ArrayList<JSONObject> getPetrolStationsJsonList() {
        return petrolStationsJsonList;
    }

    public JSONArray getPetrolStationJSONArray() {
        JSONArray jsonArray = new JSONArray();
        for (JSONObject jsonObject : petrolStationsJsonList) {
            jsonArray.put(jsonObject);
        }

        return jsonArray;


    }


    public void setPetrolStationsJsonList(ArrayList<JSONObject> petrolStationsJsonList) {
        this.petrolStationsJsonList = petrolStationsJsonList;
    }

    public ArrayList<String> getPetrolStationsNameList() {
        return fuelBrandList;
    }

    public void setPetrolStationsNameList(ArrayList<String> fuelBrandList) {

        this.fuelBrandList = fuelBrandList;
    }

    public ArrayList<JSONObject> getFuelJsonList() {

        return fuelJsonList;
    }


    public JSONArray getFuelJsonArray() {
        JSONArray jsonArray = new JSONArray();
        for (JSONObject jsonObject : fuelJsonList) {
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