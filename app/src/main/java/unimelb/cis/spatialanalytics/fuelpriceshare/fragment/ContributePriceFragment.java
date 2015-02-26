package unimelb.cis.spatialanalytics.fuelpriceshare.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.soundcloud.android.crop.Crop;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import unimelb.cis.spatialanalytics.fuelpriceshare.R;
import unimelb.cis.spatialanalytics.fuelpriceshare.config.ConfigConstant;
import unimelb.cis.spatialanalytics.fuelpriceshare.config.ConfigURL;
import unimelb.cis.spatialanalytics.fuelpriceshare.data.FuelData;
import unimelb.cis.spatialanalytics.fuelpriceshare.data.Users;
import unimelb.cis.spatialanalytics.fuelpriceshare.http.AppController;
import unimelb.cis.spatialanalytics.fuelpriceshare.http.CustomRequest;
import unimelb.cis.spatialanalytics.fuelpriceshare.http.MultiPartRequest;
import unimelb.cis.spatialanalytics.fuelpriceshare.http.MyExceptionHandler;
import unimelb.cis.spatialanalytics.fuelpriceshare.others.ImagePicker;
import unimelb.cis.spatialanalytics.fuelpriceshare.others.RandomGenerateUniqueIDs;
import unimelb.cis.spatialanalytics.fuelpriceshare.views.CustermizedCanvasView;
import unimelb.cis.spatialanalytics.fuelpriceshare.views.MyNumberPicker;

/**
 * Created by hanl4 on 17/02/2015.
 * the action of contributing fuel price to the server including:
 * 1) take or select a fuel price image
 * 2) upload fuel image to the server
 * 3) extract fuel information from the image and return the information in text
 * 4) retrieve current user petrol location
 * 5) user is able to modify the returned fuel information returned from the server manually
 * 6) upload the refined result to the server to make contribution
 */
public class ContributePriceFragment extends Fragment implements DialogInterface.OnDismissListener {

    private ImagePicker imagePicker;// Pick up image from camera, library, etc. Defined by Han
    private final int REQUEST_CAMERA = ConfigConstant.REQUEST_CAMERA;//Image captured by camera call back code
    private final int SELECT_FILE = ConfigConstant.SELECT_FILE;//Image captured by selecting call back code
    private Bitmap bitmapUpload;//Captured fuel image to be uploaded
    private String transactionID;//The ID of the action of contributing price.


    /**
     * Petrol Station and Fuel Information parameters
     */

    private FuelData fuelData = new FuelData();
    /**
     * Main view panel components
     */
    private ImageView imageViewFuel;//present fuel image
    private ImageView imageViewTakePhoto;//take fuel image icon
    private TextView textViewLabel;//labels for taking an image of fuel price


    private ActionBar actionBar;
    private Menu menuActionBar;
    private boolean isEditingView = false;


    /**
     * Click position records
     */
    private int positionX;// X coordinate in pixel of a given image
    private int positionY;// Y coordinate in pixel of a given image

    /**
     * Canvas view drawer
     */
    private CustermizedCanvasView canvasView;
    private Bitmap bitmapCanvas;

    /**
     * The latest selected rectangle id
     */
    private int selectedID;

    //The selected petrol station id
    private int selectedStationID;


    private final String TAG = ContributePriceFragment.class.getSimpleName();

    private Activity activity;

    private String[] actionBarTitles = {"Take Fuel Price Photo", "Fuel Editing Panel", "In Editing Mode"};

    public static boolean isMenuVisible = false;

    public ContributePriceFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_contribute_price, container, false);
        activity = getActivity();
        setHasOptionsMenu(true);


        actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle(actionBarTitles[0]);
        //actionBar.setDisplayHomeAsUpEnabled(false);


        imageViewTakePhoto = (ImageView) rootView.findViewById(R.id.do_refine_take_photo);
        textViewLabel = (TextView) rootView.findViewById(R.id.do_refine_txtLabel);
        imageViewFuel = (ImageView) rootView.findViewById(R.id.do_refine_image_fuel);


        imageViewTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto(v);
            }
        });


        /**
         * Set up the click listener to handle the click events for make fuel information modifications later.
         */
        imageViewFuel.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {

                        /**
                         * Handle the click event to check whether the click is within the fuel information represented by
                         * rectangle or not.
                         *
                         */
                        int widthImageView = imageViewFuel.getWidth();
                        int heightImageView = imageViewFuel.getHeight();

                        int widthBitmap = bitmapUpload.getWidth();
                        int heightBitmap = bitmapUpload.getHeight();

                        float ratio_w = (float) widthBitmap / widthImageView;
                        float ratio_h = (float) heightBitmap / heightImageView;

                        /**
                         * Convert the screen coordinates to pixel
                         */
                        positionX = (int) (event.getX() * ratio_w);
                        positionY = (int) (event.getY() * ratio_h);


                        /**
                         * check the click position, and get the corresponding fuel information
                         */
                        obtainSelectedRectData();

                    }
                }
                return true;
            }
        });


        return rootView;
    }


    /**
     * Upload the final refined fuel information data to the central server. The information includes:
     * 1) User profile, e.g., id, username, etc.
     * 2) Fuel price & type
     * 3) Petrol station
     */
    public void updateData2Server() {
        JSONObject json = new JSONObject();
        JSONObject userJson = Users.getUserJSONWithoutPassword();
        try {
            json.put(ConfigConstant.KEY_CONTRIBUTE_PRICE_TRANSACTION_ID, transactionID);
            json.put(ConfigConstant.KEY_USER, userJson);
            json.put(ConfigConstant.KEY_FUEL, fuelData.getFuelJsonList());
            json.put(ConfigConstant.KEY_PETROL_STATION, fuelData.getPetrolStationsJsonList().size() > 0 ? fuelData.getPetrolStationsJsonList().get(selectedStationID) : fuelData.getPetrolStationsJsonList());

            /**
             * User Volley API developed by Google to handle the request!
             * make HTTP request to the server to upload the data, and received the response from the server
             */

            // Tag used to cancel the request
            String tag_json_obj = TAG;

            final ProgressDialog pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Uploading to the server......");
            pDialog.setCancelable(false);
            pDialog.show();

            Map<String, String> params = new HashMap<String, String>();
            params.put("action", "Upload");
            params.put("json", json.toString());


            CustomRequest customRequest = new CustomRequest(ConfigURL.getUploadRefinedResultServlet(), params, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, response.toString());
                    if (response.has(ConfigConstant.KEY_ERROR))
                    {
                        Log.e(TAG, "Upload Failed!"+response.toString());
                        Toast.makeText(getActivity(), "Upload Failed!"+response.toString(), Toast.LENGTH_SHORT).show();

                    }
                    else {
                        Log.d(TAG, "Upload Refined Result to Server : Success");
                        /**
                         * Update the views and clean the data for later use
                         */
                        isEditingView = false;
                        switchViews(false);

                        Toast.makeText(getActivity(), "Upload Success!", Toast.LENGTH_SHORT).show();


                    }
                    pDialog.dismiss();
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    pDialog.dismiss();

                    MyExceptionHandler.presentError(TAG, "upload refined result to server failed!", getActivity(), error);

                }
            });
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(customRequest, tag_json_obj);


        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }


    }


    /**
     * Handle the event of confirmation. If user clicks the "Upload" button, which means he wants to upload the
     * finalized (probably after refine some fuel items) fuel information to the central server.
     * It needs to process couple things
     * 1) send the refined fuel information results to the server
     * 2) update views presented to the user
     * 3) handle the condition of multiple petrol stations
     */

    public void handleUpload() {
        {
            if (fuelData.getPetrolStationsJsonList().size() > 1) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Which of the following petrol station are you currently at?")

                        //choose the fuel station if multiple stations are detected
                        .setSingleChoiceItems(fuelData.convertPetrolStationNameList2CharSequence(), selectedStationID, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG,"Selecting : " + which);
                                selectedStationID = which;

                            }
                        })


                                // Set the action buttons
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // User clicked OK, so save the mSelectedItems results somewhere
                                // or return them to the component that opened the dialog
                                updateData2Server();


                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {


                            }
                        });


                AlertDialog dialog = builder.create();
                dialog.show();

            } else {
                if (fuelData.getPetrolStationsJsonList().size() == 0)

                    Log.e(TAG, "No petrol station was detected around!");

                updateData2Server();

            }


        }
    }


    /**
     * If the user click the "Leave" button or Home "Back" menu, user will exist current editing panel and return back
     * to the main panel. This operation is not encouraged since it will close all the views without saving
     * the data neither on local nor server. THE DATA WILL LOST.
     */
    public void handleCancel() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Add the buttons
        builder.setPositiveButton("Leave", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked Leave button
                isEditingView = false;
                switchViews(false);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.dismiss();
            }
        });
        // Set other dialog properties
        builder.setTitle("Are you sure to leave current panel?");
        builder.setMessage("If you insist leaving current panel without uploading your editing to the server, all your modifications will be lost and can not be recovered.");

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();

/*        Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);*/


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        /**
         * Remove all existing items from the menu, leaving it empty as if it had
         * just been created.
         */
        menu.clear();//remove host activity menu
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_contribute_price, menu);


        // Inflate the menu; this adds items to the action bar if it is present.

        /**
         * Initialize menu bar
         */
        menu.setGroupVisible(R.id.menu_group, false);
        //menu.findItem(R.id.menu_cancel).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        //menu.findItem(R.id.menu_save).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menuActionBar = menu;

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                //leave current editing panel
             /*   if (isEditingView)
                    handleCancel();*/
                return true;

            case R.id.menu_save:
                /**
                 * upload the editing to the server
                 */
                handleUpload();
                return true;

            case R.id.menu_cancel:
                //withdraw current editing; same as back function
                handleCancel();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Take fuel photo to process
     */
    public void takePhoto(View view) {

        imagePicker = new ImagePicker(getActivity(), "Take a photo");
        imagePicker.selectImageBoth();


    }


    /**
     * Receive all the returning results of children activities. Detailed information please refer to the
     * official android programming document
     *
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        String imagePath, data, filename;
        if (resultCode == getActivity().RESULT_OK) {

            switch (requestCode) {
                case REQUEST_CAMERA:
                    /**
                     * Capture the image from camera and then crop it.
                     */

                    File imageFile = imagePicker.getImageFile();
                    Uri uri = Uri.fromFile(imageFile);
                    cropImage(uri);
                    //imageFile.delete();

                    break;


                case SELECT_FILE:
                    /**
                     * Capture the image from selecting from library, files or gallery. ANd then crop it.
                     */
                    if (intent != null) {

                        cropImage(intent.getData());

                    } else {
                        Log.e(TAG, "No image file is selected");
                    }
                    break;

                case Crop.REQUEST_PICK:
                    // cropImage(result.getData());
                    break;
                case Crop.REQUEST_CROP:
                    /**
                     * Get the cropped image result
                     */
                    if (intent != null)
                        try {

                            //get the cropped result
                            Uri imageUri = Crop.getOutput(intent);

                            bitmapUpload = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                            filename = RandomGenerateUniqueIDs.getFuelPriceImageName("png");
                            transactionID = RandomGenerateUniqueIDs.getUniqueID();
                            data = fuelData.getUploadDataInfo(transactionID);

                            /*
                            upload the image to the server to process
                             */

                            // Tag used to cancel the request
                            String tag_json_obj = TAG;

                            /**
                             * Use google Volley lib to upload an image
                             */
                            final ProgressDialog pDialog = new ProgressDialog(getActivity());
                            pDialog.setMessage("Processing fuel image......");
                            pDialog.setCancelable(false);
                            pDialog.show();
                            MultiPartRequest multiPartRequest = new MultiPartRequest(ConfigURL.getFuelPriceImageProcessServlet(), new File(imageUri.getPath()), filename, data, new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.d(TAG, response.toString());

                                    if (!response.has(ConfigConstant.KEY_ERROR)) {

                                            pDialog.dismiss();
                                            //Image was successfully uploaded to the server
                                            fuelData.parseFuelPriceImageReplyData(response);
                                            isEditingView = true;
                                            switchViews(true);
                                            //The view image might be scaled up to fill the entire view. for more detailed info, please refer
                                            //the website: https://guides.codepath.com/android/Working-with-the-ImageView
                                            //imageViewFuel.setImageBitmap(bitmapUpload);

                                            /**
                                             * Initialize canvas view and update current view
                                             */
                                            canvasView = new CustermizedCanvasView(getActivity(), bitmapUpload);
                                            updateCanvasView();



                                    } else {
                                        pDialog.setMessage(response.toString());
                                        Log.e(TAG, response.toString());
                                        pDialog.dismiss();
                                    }
                                }
                            }, new Response.ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError error) {

                                    pDialog.dismiss();
                                    MyExceptionHandler.presentError(TAG, "Update profile image failed!", getActivity(), error);


                                }
                            });

                            /**
                             * Set the timeout
                             */

                            int socketTimeout = 60000;//60 seconds - change to what you want
                            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                            multiPartRequest.setRetryPolicy(policy);


                            // Adding request to request queue
                            AppController.getInstance().addToRequestQueue(multiPartRequest, tag_json_obj);


                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(TAG, e.toString());
                        }
                    break;


                case Crop.RESULT_ERROR:
                    Log.e(TAG, Crop.getError(intent).getMessage());
                    break;
                default:
                    Log.e(TAG, "No such operation for switch");

                    break;

            }
        }
    }


    /**
     * Crop input image either from camera or files. There are couple cropping options available including asSquare, withAspect, withMaxSize.
     * By default we use the withMaxSize. For more detailed information, please refer to the CropImage Library.
     *
     * @param source URI
     */

    private void cropImage(Uri source) {
        Uri outputUri = Uri.fromFile(new File(getActivity().getCacheDir(), "cropped"));
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), source);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());


        }

        if (bitmap != null)
            new Crop(source).output(outputUri).withMaxSize(bitmap.getWidth(), bitmap.getHeight()).start(getActivity());

        // new Crop(source).output(outputUri).withMaxSize(1000, 2000).start(getActivity());
    }


    /**
     * Update canvas view under two conditions:
     * 1) click event was triggered and the click falls into the predefined rectangle area
     * 2) fuel item modifications were made and confirmed
     *
     * @param //isClean// to control whether to clean the FLAG_IS_SELECTED attribute to be false or not.
     *                    The clicked rectangle needs to be enlarged when in the editing mode and re-set to
     *                    normal when the modification was completed and confirmed.
     */
    public void updateCanvasView() {

        bitmapCanvas = canvasView.getBitmap(fuelData.getFuelJsonList());
        imageViewFuel.setImageBitmap(bitmapCanvas);

    }

    /**
     * Set the visibility of the bottom menu that controls uploading the refined result to the server. When in the action of editing and
     * by default model, it is invisible to the user unless the user is in editing panel but not in an action of performing any modification
     * tasks.
     *
     * @param flag if flag is true, it is visible to the user; otherwise hidden.
     */

    public void setActionBarMenuVisibility(boolean flag) {
        isMenuVisible = flag;
        menuActionBar.setGroupVisible(R.id.menu_group, flag);
        // actionBar.setDisplayHomeAsUpEnabled(flag);
        if (flag)
            actionBar.setTitle(actionBarTitles[1]);
        else {
            if (isEditingView)
                actionBar.setTitle(actionBarTitles[2]);
            else
                actionBar.setTitle(actionBarTitles[0]);
        }


    }


    /**
     * Control the total views of current window.
     *
     * @param flag if true, present fuel image to the user; otherwise set it invisible and only show the take photo icon.
     */

    public void switchViews(boolean flag) {
        if (flag) {
            /**
             * When in editing model, hide the take photo icon and bring the fuel image to the front.
             */
            textViewLabel.setVisibility(View.GONE);
            imageViewTakePhoto.setVisibility(View.GONE);
            imageViewFuel.setVisibility(View.VISIBLE);
            setActionBarMenuVisibility(true);

        } else {

            /**
             * After finishing editing or withdrawing the refinement, back to main panel, and set everything
             * to initial status.
             */

            textViewLabel.setVisibility(View.VISIBLE);
            imageViewTakePhoto.setVisibility(View.VISIBLE);
            imageViewFuel.setVisibility(View.GONE);
            setActionBarMenuVisibility(false);
        }

    }


    /**
     * Handle screen (ImageView) click or touch event to determine the touch area, and get the corresponding rectangle that is defined by the data set
     * fuel price and fuel type respectively.
     * author: Han Li
     */
    public void obtainSelectedRectData() {
        //define rectangle coordinates
        int left, top, right, bottom;

        try {

            //iterate the entire data set to find the rectangle's corresponding data set.
            for (int i = 0; i < fuelData.getFuelJsonList().size(); i++)

            {
                //get JSON Object from JSONArray
                JSONObject json = fuelData.getFuelJsonList().get(i);

                /********************************FUEL TYPE SETTINGS*******************************************/

                //mapping json data field from fuel type and define a rectangle area
                left = json.getInt(ConfigConstant.KEY_RECT_LEFT);
                top = json.getInt(ConfigConstant.KEY_RECT_TOP);
                right = json.getInt(ConfigConstant.KEY_RECT_RIGHT);
                bottom = json.getInt(ConfigConstant.KEY_RECT_BOTTOM);


                //check the touch point within the rectangle defined by the json data object or not

                if (isInsideRectangle(left, right, top, bottom)) {

                    //hide the bottom menu in modification action
                    setActionBarMenuVisibility(false);

                    if (json.has(ConfigConstant.KEY_FUEL_BRAND)) {

                        String str = json.getString(ConfigConstant.KEY_FUEL_BRAND);
                        json.put(ConfigConstant.FLAG_IS_SELECTED, true);
                        updateCanvasView(false);
                        selectedID = i;

                        /**
                         * Draw the editing view for fuel type
                         */

                        showModifyFuelTypeDialog(fuelData.getAllFuelTypeList().indexOf(str));


                    } else if (json.has(ConfigConstant.KEY_FUEL_PRICE)) {

                        /********************************FUEL PRICE SETTINGS*******************************************/
                        json.put(ConfigConstant.FLAG_IS_SELECTED, true);
                        updateCanvasView(false);
                        selectedID = i;

                        showModifyFuelPriceDialog();


                    }


                }


            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }


    }


    /**
     * present the dialog for editing fuel information
     */
    public void showModifyFuelPriceDialog() {
        //get the price of the corresponding json object

        try {
            double price = fuelData.getFuelJsonList().get(selectedID).getDouble(ConfigConstant.KEY_FUEL_PRICE);


            final Dialog dialog = new Dialog(getActivity(), R.style.DialogCustomTheme);

            final MyNumberPicker myNumberPicker = new MyNumberPicker(getActivity());

            LinearLayout linearLayout = (LinearLayout) ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_modify_fuel_price, null);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout linearLayoutNumberPicker = (LinearLayout) linearLayout.findViewById(R.id.linearLayoutNumberPicker);

            for (int index = 0; index < myNumberPicker.getCounts(); index++) {
                setNumberPickerTextColor(myNumberPicker.getNumberPicker(index), Color.WHITE);
                linearLayoutNumberPicker.addView(myNumberPicker.getNumberPicker(index));
            }

            myNumberPicker.setValue(price, null);//set numberPicker default values


            Button buttonCancel = (Button) linearLayout.findViewById(R.id.button_cancel);
            Button buttonDone = (Button) linearLayout.findViewById(R.id.button_done);

            buttonCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();

                }
            });

            buttonDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    /**
                     * Handle the event that the client made some changes about the returned fuel information result from central server
                     * and confirm the modification. Mainly handle the fuel information modification and the views such as canvas draw,
                     * etc.
                     */
                    double price = myNumberPicker.getValues();
                    try {
                        fuelData.getFuelJsonList().get(selectedID).put(ConfigConstant.KEY_FUEL_PRICE, price);
                    } catch (JSONException e) {
                        Log.e(TAG, e.toString());
                        e.printStackTrace();
                    }

                    dialog.dismiss();

                }
            });


            dialog.setContentView(linearLayout);
            dialog.setOnDismissListener(this);//override this method. when it is called, the canvas view will be updated.
            dialog.setCancelable(false);
            dialog.show();

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("Json Modify Price Dialog", e.toString());
        }
    }


    /**
     * Set NumberPicker Text color
     *
     * @param numberPicker
     * @param color
     * @return
     */

    public boolean setNumberPickerTextColor(NumberPicker numberPicker, int color) {
        final int count = numberPicker.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = numberPicker.getChildAt(i);
            if (child instanceof EditText) {
                try {
                    Field selectorWheelPaintField = numberPicker.getClass()
                            .getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    ((Paint) selectorWheelPaintField.get(numberPicker)).setColor(color);
                    ((EditText) child).setTextColor(color);
                    numberPicker.invalidate();
                    return true;
                } catch (NoSuchFieldException e) {
                    Log.w("setNumberPickerTextColor", e);
                } catch (IllegalAccessException e) {
                    Log.w("setNumberPickerTextColor", e);
                } catch (IllegalArgumentException e) {
                    Log.w("setNumberPickerTextColor", e);
                }
            }
        }
        return false;
    }

    /**
     * Build floating dialog for editing fuel information
     */

    public void showModifyFuelTypeDialog(int index) {
        final Dialog dialog = new Dialog(getActivity(), R.style.DialogCustomTheme);

        LinearLayout linearLayout = (LinearLayout) ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_modify_fuel_type, null);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        final NumberPicker numberPicker = (NumberPicker) linearLayout.findViewById(R.id.numberPicker);

        setNumberPickerTextColor(numberPicker, Color.WHITE);//set the text color to be white
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(fuelData.getAllFuelTypeList().size() - 1);
        if (index >= 0)
            numberPicker.setValue(index);


        numberPicker.setFormatter(new NumberPicker.Formatter() {

            @Override
            public String format(int value) {
                // TO DO Auto-generated method stub
                return fuelData.getAllFuelTypeList().get(value);
            }
        });

        Button buttonCancel = (Button) linearLayout.findViewById(R.id.button_cancel);
        Button buttonDone = (Button) linearLayout.findViewById(R.id.button_done);

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();

            }
        });

        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /**
                 * Handle the event that the client made some changes about the returned fuel information result from central server
                 * and confirm the modification. Mainly handle the fuel information modification and the views such as canvas draw,
                 * etc.
                 */

                try {
                    fuelData.getFuelJsonList().get(selectedID).put(ConfigConstant.KEY_FUEL_BRAND, fuelData.getAllFuelTypeList().get(numberPicker.getValue()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                dialog.dismiss();

            }
        });


        dialog.setContentView(linearLayout);
        dialog.setCancelable(true);
        dialog.setOnDismissListener(this);
        dialog.show();


    }


    /**
     * Update canvas view under two conditions:
     * 1) click event was triggered and the click falls into the predefined rectangle area
     * 2) fuel item modifications were made and confirmed
     *
     * @param isClean// to control whether to clean the FLAG_IS_SELECTED attribute to be false or not.
     *                  The clicked rectangle needs to be enlarged when in the editing mode and re-set to
     *                  normal when the modification was completed and confirmed.
     */
    public void updateCanvasView(boolean isClean) {
        if (isClean)
            try {
                fuelData.getFuelJsonList().get(selectedID).put(ConfigConstant.FLAG_IS_SELECTED, false);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, e.toString());
            }


        bitmapCanvas = canvasView.getBitmap(fuelData.getFuelJsonList());
        imageViewFuel.setImageBitmap(bitmapCanvas);

    }


    /**
     * Given a point (coordinate), check whether it falls into the rectangle or not.
     * author: Han Li
     *
     * @param left   left of the rectangle
     * @param right  right of the rectangle
     * @param top    top of the rectangle
     * @param bottom bottom of the rectangle
     * @return return true if the coordinate within the rectangle; otherwise return false
     */

    public boolean isInsideRectangle(int left, int right, int top, int bottom) {
        if (positionX >= left && positionX <= right && positionY >= top && positionY <= bottom)
            return true;
        else
            return false;

    }

    /**
     * Floating dialog dismiss function.
     *
     * @param dialog
     */

    @Override
    public void onDismiss(DialogInterface dialog) {
        //update canvas view
        updateCanvasView(true);
        //set the visibility of bottom menu to be visible to the user
        setActionBarMenuVisibility(true);


    }


}


