package unimelb.cis.spatialanalytics.fuelpriceshare.others;

import unimelb.cis.spatialanalytics.fuelpriceshare.data.Users;

/**
 * Created by hanl4 on 4/02/2015.
 * mainly to generate unique file names and transaction ids.
 */
public class RandomGenerateUniqueIDs {


    /**
     * produce unique file name: user id + current time + file format
     * @param format file format such as "PNG", "TXT", etc
     * @return the unique file name
     */
    public static String getFileName(String format)

    {

        Long time=System.currentTimeMillis();
        String filename = Users.id+"_"+time+"."+format;

        return filename;

    }


    /**
     * get the fuel price image file unique file name
     * @param format image format such as "png"
     * @return the image file unique name
     */
    public static String getFuelPriceImageName(String format)

    {
        Long time=System.currentTimeMillis();
        String filename = Users.id+"_fuel_price_"+time+"."+format;

        return filename;

    }


    /**
     * Generate unique id: userID+current time
     * @return the unique id
     */

    public static String getUniqueID()
    {
        Long time=System.currentTimeMillis();
        String id = Users.id+"_"+time;

        return id;

    }


}
