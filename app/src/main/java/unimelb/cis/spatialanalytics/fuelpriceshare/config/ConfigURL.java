package unimelb.cis.spatialanalytics.fuelpriceshare.config;


/**
 * Created by hanl4 on 2/02/2015.
 * Constant parameters mainly for internet URL configurations.
 */
public class ConfigURL {


 /*   private static String ip = "http://128.250.26.228";//server IP address
    private static String tomPort = "8080";//Tomcat port number*/

//    private static String ip="http://spatialanalytics.cis.unimelb.edu.au";
//    private static String tomPort="8082";

    private  static String ip = "http://128.250.26.229";
    private static String tomPort="8080";

    private static String serverProject = "FuelPriceShare";//server project name

    private static String couchDBServlet = "CouchDBHandlerServlet";//talk to CouchDB servlet

    private static String loginServlet = "LoginServlet";//for log in servlet

    private static String registerServlet = "RegisterServlet";//register servlet
    private static String uploadImageServlet = "UploadImageServlet";//upload image servlet

    private static String fuelPriceImageProcessServlet = "FuelPriceImageProcessServlet";//process fuel image servlet
    private static String uploadRefinedResultServlet = "UploadRefinedResultServlet";//upload refined fuel information servlet


    /**
     * get upload refined fuel information servlet
     * @return
     */
    public static String getUploadRefinedResultServlet() {
        return ip + ":" + tomPort + "/" + serverProject + "/" + uploadRefinedResultServlet;

    }




    /**
     * get login activity servlet
     * @return
     */

    public static String getLoginURL() {
        return ip + ":" + tomPort + "/" + serverProject + "/" + loginServlet;

    }

    /**
     * get register activity servlet
     * @return
     */

    public static String getRegisterURL() {
        return ip + ":" + tomPort + "/" + serverProject + "/" + registerServlet;

    }

    /**
     * get couchDB servlet
     * @return
     */

    public static String getCouchDBURL() {
        return ip + ":" + tomPort + "/" + serverProject + "/" + couchDBServlet;


    }

    /**
     * get image upload servlet
     * @return
     */
    public static String getUploadImageServlet() {
        return ip + ":" + tomPort + "/" + serverProject + "/" + uploadImageServlet;

    }


    /**
     * get the path of profile image stored at the server side
     * @return
     */
    public static String getServerProfileImageFolderBase() {
        return ip + ":" + tomPort + "/" + serverProject + ConfigConstant.KEY_PROFILE_IMAGE_FOLDER;

    }


    /**
     * get the fuel image process servlet
     * @return
     */
    public static String getFuelPriceImageProcessServlet() {
        return ip + ":" + tomPort + "/" + serverProject + "/" + fuelPriceImageProcessServlet;

    }


}
