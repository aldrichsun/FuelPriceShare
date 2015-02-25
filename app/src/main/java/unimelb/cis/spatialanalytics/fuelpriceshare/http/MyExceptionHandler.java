package unimelb.cis.spatialanalytics.fuelpriceshare.http;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

/**
 * Created by hanl4 on 21/02/2015.
 * Mainly handle the VolleyError (internet)
 */
public class MyExceptionHandler {

    public static void presentError(String TAG, String errorMsg,Context context,VolleyError error)
    {
        if(errorMsg==null || errorMsg.equals(""))
          errorMsg="Error";


        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
            errorMsg="TimeoutError or NoConnectionError";

        } else if (error instanceof AuthFailureError) {
            //TODO
            errorMsg="AuthFailureError";
        } else if (error instanceof ServerError) {
            //TODO
            errorMsg="ServerError";
        } else if (error instanceof NetworkError) {
            //TODO
            errorMsg="NetworkError";
        } else if (error instanceof ParseError) {
            //TODO
            errorMsg="ParseError";

        }

        errorMsg=errorMsg+" ; "+error.toString();

        Log.e(TAG,errorMsg);

        //TODO change to other way to deal with error to make it more custermized
        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();



    }
}
