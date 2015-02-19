package unimelb.cis.spatialanalytics.fuelpriceshare.others;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.util.Locale;

import unimelb.cis.spatialanalytics.fuelpriceshare.config.ConfigConstant;

/**
 * Created by hanl4 on 5/02/2015.
 * This class is used to select image either from camera or local files.
 */
public class ImagePicker {

    private Activity activity;

    private final int REQUEST_CAMERA = ConfigConstant.REQUEST_CAMERA;
    private final int SELECT_FILE = ConfigConstant.SELECT_FILE;

    private File imageFile;
    private String title;

    private final String TAG="ImagePicker";//For Log

    /**
     * Construction.
     *
     * @param activity The activity calls ImagePicker
     * @param title    set the title of the ImagePicker Dialog. Default is set to "Choose an Image".
     */
    public ImagePicker(Activity activity, @Nullable String title) {
        this.activity = activity;
        if (title == null || title.equals(""))
            title = "Choose an Image";
        this.title = title;
    }


    /**
     * Enables user choosing image either by camera or local file
     */
    public void selectImageBoth() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                String fileName = generateImageName();
                if (items[item].equals("Take Photo")) {
                    /**
                     * Take a photo by camera
                     */
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    /**
                     * Get the image file. Note that, the image captured by camera is cached in local storage, which means
                     * it has to be manually removed later if you do not store a copy on disk.
                     */
                    imageFile = new File(android.os.Environment
                            .getExternalStorageDirectory() + "/" + fileName);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
                    activity.startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Choose from Library")) {
                    /**
                     * Choose image from library
                     */
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    activity.startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    /**
                     * Cancel the action
                     */
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    /**
     * Only support selecting image by camera
     */
    public void selectImageCamera() {
        String fileName = generateImageName();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        /**
         * Get the image file. Note that, the image captured by camera is cached in local storage, which means
         * it has to be manually removed later if you do not store a copy on disk.
         */
        imageFile = new File(android.os.Environment
                .getExternalStorageDirectory() + "/" + fileName);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
        activity.startActivityForResult(intent, REQUEST_CAMERA);
    }


    /**
     * Generate random unique image name
     *
     * @return image name
     */

    public String generateImageName() {
        return RandomGenerateUniqueIDs.getFileName("png");

    }

    /**
     * Get image file
     *
     * @return image file
     */

    public File getImageFile() {
        return imageFile;
    }

    /**
     * Get image real path when selecting image from local files
     *
     * @param uri image URI resource
     * @return the real path of the image stored in the local storage.
     */

    public String getImagePath(Uri uri) {
        if(uri==null)
        {
            Log.e(TAG,"uri is null");
            return null;
        }
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index).toLowerCase(Locale.ENGLISH);
        return path;
    }


}
