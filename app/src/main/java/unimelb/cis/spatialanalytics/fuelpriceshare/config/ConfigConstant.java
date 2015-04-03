package unimelb.cis.spatialanalytics.fuelpriceshare.config;

/**
 * Created by hanl4 on 2/02/2015.
 * Constant parameters
 */
public class ConfigConstant {

    /**
     * Fuel Settings
     */
    public static final String KEY_FUEL_PRICE = "price";
    public static final String KEY_FUEL_BRAND = "fuel";

    //Rectangle
    public static final String KEY_RECT_LEFT = "left";
    public static final String KEY_RECT_TOP = "top";
    public static final String KEY_RECT_RIGHT = "right";
    public static final String KEY_RECT_BOTTOM = "bottom";

    public static final String FLAG_IS_SELECTED="isSelected";

    public static final String KEY_FUEL="fuel";
    public static final String KEY_ALL_FUEL_BRAND="all_fuel_brand";



    /**
     * petrol stations
     */

    public final static String KEY_PETROL_STATION="fuel_station";
    public static final  String KEY_PETROL_STATION_ID="_id";
    public final static String KEY_PETROL_STATION_NAME="name";


    /**
     * Contribute price parameters
     */

    public static final String KEY_CONTRIBUTE_PRICE_TRANSACTION_ID="transaction_id";
    public static final String KEY_LONGITUDE="longitude";
    public static final String KEY_LATITUDE="latitude";
    public static final String KEY_CAN_GET_LOCATION="can_get_location";


    /*
    Status of contributing price
     */

    public static final String KEY_CONTRIBUTE_PRICE_PROCESS_STATUS="status";
    public static final String KEY_CONTRIBUTE_PRICE_STATUS_RETRIEVE_PETROL_STATION="retrieve_petrol_station";
    public static final String KEY_CONTRIBUTE_PRICE_STATUS_PROCESS_FUEL_IMAGE="process_fuel_image";



    /**
     * File upload (image mainly)
     */
    public static final String KEY_FILE_UPLOAD_FILE_DATA="file";
    public  static final String KEY_FILE_UPLOAD_STRING_DATA="data";



    /**
     * JSON response codes
     */
    public static final String KEY_SUCCESS = "ok";
    public static final String KEY_ERROR = "error";


    /**
     * User information field
     */
    public static final String KEY_USER="user";

    public static final String KEY_ERROR_MSG = "error_msg";
    public static final String KEY_UID = "_id";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_CREATED_AT = "created_at";
    public static final String KEY_Password = "password";
    public static final String KEY_CREDIT="credit";


    /**
     * Activity for Result
     */
    public static final int REQUEST_CAMERA=1; //select image from camera
    public static final int SELECT_FILE=2; //select image from gallery.





    /**
     * Format
     */
    public static final String BIRTH_FORMAT= "dd/MM/yyyy";


    /**
     * User information stored at local (local session)
     */
    public static final String LOCAL_USER_JSON="userjson";
    public static final String LOCAL_USER_LOGIN="isLogin";



    /**
     * CouchDB Operations
     */

    public static final String KEY_COUCHDB_DOC_ID="_id";
    public static final String KEY_COUCHDB_DOC_DATA="data";
    public static final String KEY_COUCHDB_DOC_ACTION="action";
    public static final String KEY_COUCHDB_DOC_REV="rev";


    /**
     * folders to store uploaded images / files
     */

    public static final String KEY_FUEL_IMAGE_FOLDER="/fuel_image/";//Folder that stores uploaded fuel image. Need to be reset
    public static final String KEY_PROFILE_IMAGE_FOLDER="/user_profile_photo/";//Folder that stores uploaded profile image. Need to be reset


    /**
     * DecimalFormat for NumberPicker
     */
    public static final String KEY_DecimalFormat="000.0";


    /**
     * Define the maximum width and height of an image
     */
    public static final int MAX_IMAGE_WIDTH=1024;
    public static final int MAX_IMAGE_HEIGHT=1024;

    /**
     * Define the size of profile image presented in Image View
     */
    public static final int PROFILE_IMAGE_WIDTH=800;
    public static final int PROFILE_IMAGE_HEIGHT=800;

    /**
     * Image type
     */
    public static final String IMAGE_TYPE_PROFILE="profile_image";
    public static final String IMAGE_TYPE_FUEL="fuel_image";


    /**
     * Minimal size of crop rectangle
     */
    public static final int MIN_CROP_RECT_WIDTH=200;
    public static final int MIN_CROP_RECT_HEIGHT=200;


}
