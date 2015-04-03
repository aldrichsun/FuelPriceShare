package unimelb.cis.spatialanalytics.fuelpriceshare.http;

/**
 * Created by hanl4 on 18/02/2015.
 * make customized HTTP request but not including files (image)
 */
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class CustomRequest extends Request<JSONObject> {

    private Listener<JSONObject> listener;//listener to handle the response from the server
    private Map<String, String> params; //params passing to server


    /**
     * Make a GET request and return a parsed object from JSON.
     * @param url URL of the request to make
     * @param params Map of request parameters
     * @param reponseListener response listener
     * @param errorListener error listener
     */
    public CustomRequest(String url, Map<String, String> params,
                         Listener<JSONObject> reponseListener, ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        this.listener = reponseListener;
        this.params = params;
    }


    /**
     * Make a customized  GET/PUT/DELETE request and return a parsed object from JSON.
     * @param url URL of the request to make
     * @param params Map of request parameters
     * @param reponseListener response listener
     * @param errorListener error listener
     */
    public CustomRequest(int method, String url, Map<String, String> params,
                         Listener<JSONObject> reponseListener, ErrorListener errorListener) {
        super(method, url, errorListener);
        this.listener = reponseListener;
        this.params = params;
    }


    /**
     * Get the params of the request
     * @return params
     * @throws com.android.volley.AuthFailureError
     */
    protected Map<String, String> getParams()
            throws com.android.volley.AuthFailureError {
        return params;
    };

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            return Response.success(new JSONObject(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }

    @Override
    protected void deliverResponse(JSONObject response) {

        listener.onResponse(response);
    }


}