package unimelb.cis.spatialanalytics.fuelpriceshare.config;


/**
 * Created by hanl4 on 2/02/2015.
 * Constant parameters mainly for internet URL configurations.
 */
public class ConfigURL {

    private static String ip = "http://128.250.26.229";//server IP address
    private static String tomPort = "8080";//Tomcat port number

/*	private  static String ip="http://spatialanalytics.cis.unimelb.edu.au";
    private static String tomPort="8082";*/

    private static String serverProject = "FuelPriceShare";//server project name

    private static String couchDBServlet = "CouchDBHandlerServlet";//talk to CouchDB servlet

    private static String loginServlet = "LoginServlet";//for log in servlet

    private static String registerServlet = "RegisterServlet";//register servlet
    private static String uploadImageServlet = "UploadImageServlet";//upload image servlet

    private static String fuelPriceImageProcessServlet = "FuelPriceImageProcessServlet";//process fuel image servlet
    private static String uploadRefinedResultServlet = "UploadRefinedResultServlet";//upload refined fuel information servlet


    public static String getUploadRefinedResultServlet() {
        return ip + ":" + tomPort + "/" + serverProject + "/" + uploadRefinedResultServlet;

    }


    public static String getDefaultProfileImageURL() {
        return ip + ":" + tomPort + "/" + serverProject + "/" + "ic_action_picture.png";


    }


    public static String getLoginURL() {
        return ip + ":" + tomPort + "/" + serverProject + "/" + loginServlet;

    }

    public static String getRegisterURL() {
        return ip + ":" + tomPort + "/" + serverProject + "/" + registerServlet;

    }

    public static String getCouchDBURL() {
        return ip + ":" + tomPort + "/" + serverProject + "/" + couchDBServlet;


    }

    public static String getUploadImageServlet() {
        return ip + ":" + tomPort + "/" + serverProject + "/" + uploadImageServlet;

    }


    public static String getImagePathBase() {
        return ip + ":" + tomPort + "/" + serverProject + "/" + "user_profile_photo/";

    }


    public static String getFuelPriceImageProcessServlet() {
        return ip + ":" + tomPort + "/" + serverProject + "/" + fuelPriceImageProcessServlet;

    }


}
