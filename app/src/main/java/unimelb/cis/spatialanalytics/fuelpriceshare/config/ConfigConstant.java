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



    /**
     * petro stations
     */

    public final static String KEY_PETRO_STATION_ID="petro_station_id";
    public final static String KEY_PETRO_STATION_NAME="petro_station_name";



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
    public static final String KEY_ERROR_MSG = "error_msg";
    public static final String KEY_UID = "_id";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_CREATED_AT = "created_at";
    public static final String KEY_Password = "password";


    /**
     * Activity for Result
     */
    public static final int REQUEST_CAMERA=1; //select image from camera
    public static final int SELECT_FILE=2; //select image from gallery.


    /**
     * HTTP Request return code for interface
     */
    public static final int REQUEST_CODE_COUCHDB=1;
    public static final int REQUEST_CODE_IMAGE_UPLOAD=2;


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
     * Fragment Control
     */

    public static final int KEY_FRAGMENT_HOME=0;
    public static final int KEY_FRAGMENT_PROFILE=1;
    public static final int KEY_FRAGMENT_REFINE_PRICE=2;
    public static final int KEY_FRAGMENT_OTHER=3;

    /**
     * CouchDB Operations
     */

    public static final String KEY_COUCHDB_DOC_ID="_id";
    public static final String KEY_COUCHDB_DOC_DATA="data";
    public static final String KEY_COUCHDB_DOC_ACTION="action";
    public static final String KEY_COUCHDB_DOC_REV="rev";


}
