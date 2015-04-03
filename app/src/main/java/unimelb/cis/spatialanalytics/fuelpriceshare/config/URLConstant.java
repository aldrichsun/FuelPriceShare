package unimelb.cis.spatialanalytics.fuelpriceshare.config;

/**
 * Created by Yu Sun on 3/03/2015.
 * Just to avoid searching the project to change the constants, as we use the
 * ConfigURL as the SINGLE configuration entry point.
 */
public class URLConstant {

    public static final String RANGE_QUERY_BASE_URL = ConfigURL.getRangeQueryServlet();
    public static final String PATH_QUERY_BASE_URL = ConfigURL.getPathQueryServlet();
    public static final String LOC_HISTORY_BASE_URL = ConfigURL.getLocationHistoryServlet();

}