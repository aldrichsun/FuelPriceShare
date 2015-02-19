package unimelb.cis.spatialanalytics.fuelpriceshare.http;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import unimelb.cis.spatialanalytics.fuelpriceshare.config.ConfigConstant;
import unimelb.cis.spatialanalytics.fuelpriceshare.config.ConfigURL;


/**
 * Created by hanl4 on 5/02/2015.
 * This function is used to upload an image to the server
 */
public class ImageUploader extends AsyncTask<Object, Void, Object> {

    private String filename;//image file name
    private Integer quality;//compression level 0-100. The higher value the less compression.
    private String data;//transmitted string data
    private Bitmap bitmap;//image to upload
    private ImageUploaderReply reply;//interface object
    private int requestCode;//request code

    private String url = ConfigURL.getUploadImageServlet();

    private final String TAG="ImageUploader";


/*
    *//**
     * Construction Class. path, data are required; but fileName and quality are optional.
     * PLEASE NOTE THAT: this construction is usually used for profile image.
     *
     * @param reply  interface object, usually refers to parent context
     * @param path image path
     * @param data string data of along with the image
     * @param requestCode returning code to parent activity (distinguish the request, similarly as setActivityForResult)
     * @param filename image file name
     * @param quality Hint to the compressor, 0-100. 0 meaning compress for
     *                 small size, 100 meaning compress for max quality. Some
     *                 formats, like PNG which is lossless, will ignore the
     *                 quality setting
     *//*

    public ImageUploader(ImageUploaderReply reply, String path, @Nullable String data, int requestCode, @Nullable String filename, @Nullable Integer quality) {

        if (quality == null)
            this.quality = 100;

        this.execute();
    }*/



    /**
     * Construction Class. path, data are required; but fileName and quality are optional.
     *
     * @param reply  interface object, usually refers to parent context
     * @param bitmap image to upload
     * @param data string data of along with the image
     * @param requestCode returning code to parent activity (distinguish the request, similarly as setActivityForResult)
     * @param filename image file name
     * @param quality Hint to the compressor, 0-100. 0 meaning compress for
     *                 small size, 100 meaning compress for max quality. Some
     *                 formats, like PNG which is lossless, will ignore the
     *                 quality setting
     */
    public ImageUploader(ImageUploaderReply reply, Bitmap bitmap, String data, int requestCode, String filename, @Nullable Integer quality) {
        this.reply = reply;
        this.bitmap = bitmap;
        this.requestCode = requestCode;
        this.data = data;
        this.filename = filename;
        if (quality == null)
            this.quality = 100;

        this.execute();
    }



    /**
     * Construction Class. path, data are required; but fileName and quality are optional.
     * PLEASE NOTE THAT: this construction is usually used for general image uploading
     *
     * @param reply  interface object, usually refers to parent context
     * @param url customized URL
     * @param bitmap image to upload
     * @param data string data of along with the image
     * @param requestCode returning code to parent activity (distinguish the request, similarly as setActivityForResult)
     * @param filename image file name
     * @param quality Hint to the compressor, 0-100. 0 meaning compress for
     *                 small size, 100 meaning compress for max quality. Some
     *                 formats, like PNG which is lossless, will ignore the
     *                 quality setting
     */
    public ImageUploader(ImageUploaderReply reply, String url, Bitmap bitmap, String data, int requestCode, String filename, @Nullable Integer quality) {
        this.url = url;
        this.reply = reply;
        this.bitmap = bitmap;
        this.requestCode = requestCode;
        this.data = data;
        this.filename = filename;
        if (quality == null)
            this.quality = 100;

        this.execute();
    }


    /**
     * Upload image to the server
     * @return response from the server in String
     */
    public String uploadImage() {
        String json = "";
        try {

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, bos);//compress the image
            byte[] imageData = bos.toByteArray();
            ByteArrayBody byteArrayBody = new ByteArrayBody(imageData, filename);
            MultipartEntityBuilder mpEntity = MultipartEntityBuilder.create();

            mpEntity.addPart(ConfigConstant.KEY_FILE_UPLOAD_FILE_DATA, byteArrayBody);

            if (data != null && !data.equals(""))
                mpEntity.addTextBody(ConfigConstant.KEY_FILE_UPLOAD_STRING_DATA, data);

            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(url);

            post.setEntity(mpEntity.build());
            HttpResponse response = client.execute(post);
            HttpEntity resEntity = response.getEntity();
            json = EntityUtils.toString(resEntity);
            Log.d("Response", json);
            client.getConnectionManager().shutdown();

            return json;

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
            String errorMsg = "{\"error\":" + "\"" + e.toString() + "\"" + "}";
            return errorMsg;

        } catch (ClientProtocolException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
            String errorMsg = "{\"error\":" + "\"" + e.toString() + "\"" + "}";
            return errorMsg;

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
            String errorMsg = "{\"error\":" + "\"" + e.toString() + "\"" + "}";
            return errorMsg;

        }


    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    protected Object doInBackground(Object... obj) {

        try {

            return uploadImage();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
            return null;
        }

    }

    protected void onPostExecute(Object obj) {
        if (obj != null) {
            reply.imageUploaderReply(obj, requestCode);

        } else {

            Log.e(TAG, "Image doest not exist or Network Error");
        }
    }


    /**
     * Define an interface, and will be implemented in the calling class (activity) as to receive the response from the server
     */
    public interface ImageUploaderReply {
        /**
         * Interface implementation. Receive the response from the server of which the request was usually made by new ImageUploader
         *
         * @param reply       reply message from the server, known as response
         * @param requestCode for the switch case when multiple requests are made
         */
        public void imageUploaderReply(Object reply, int requestCode);


    }

}
