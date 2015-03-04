package unimelb.cis.spatialanalytics.fuelpriceshare.others;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import unimelb.cis.spatialanalytics.fuelpriceshare.config.ConfigConstant;

/**
 * Created by hanl4 on 3/03/2015.
 * Used to process the image such as compress, rotate, etc.
 * Most of the resources below are taken from Google
 * http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
 */
public class ImageDecoder {

    private static final String TAG = ImageDecoder.class.getSimpleName();

    /**
     * optional not used
     *
     * @param bitmap
     * @param file
     * @return
     */

    public File getCompressImage(Bitmap bitmap, File file) {

        OutputStream outStream = null;
        try {
            outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, outStream);
            outStream.flush();
            outStream.close();
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            Log.e("Saving Compressed Image", e.getMessage());

        } catch (IOException e) {
            //e.printStackTrace();
            Log.e("Saving Compressed Image", e.getMessage());

        } catch (Exception e) {
            //e.printStackTrace();
            Log.e("Saving Compressed Image", e.getMessage());
        }

        return file;
    }


    /**
     * by google
     * http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
     *
     * @param file
     * @param reqWidth
     * @param reqHeight
     * @return
     */

    public static Bitmap decodeSampledBitmapFromFile(File file, int reqWidth, int reqHeight) {


        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
/*        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        String imageType = options.outMimeType;*/

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(file.getAbsolutePath(), options);


    }


    /**
     * by google
     * http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
     *
     * @param context
     * @param selectedImage
     * @param reqWidth
     * @param reqHeight
     * @return
     */

    public static Bitmap decodeSampledBitmapFromUri(Context context, Uri selectedImage, int reqWidth, int reqHeight) {
        try {

            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            InputStream imageStream = null;

            imageStream = context.getContentResolver().openInputStream(selectedImage);

            BitmapFactory.decodeStream(imageStream, null, options);

            imageStream.close();


            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            imageStream = context.getContentResolver().openInputStream(selectedImage);
            Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

            //img= rotateImageIfRequired(img, selectedImage);
            return img;
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            Log.e(TAG, e.toString());
        } catch (IOException e) {
            // e.printStackTrace();
            Log.e(TAG, e.toString());

        } catch (Exception e) {
            //e.printStackTrace();
            Log.e("Saving Compressed Image", e.getMessage());
        }

        return null;
    }

    /**
     * http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    /**
     * By Google
     * http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
     * mImageView.setImageBitmap(
     * decodeSampledBitmapFromResource(getResources(), R.id.myimage, 100, 100));
     *
     * @param res
     * @param resId
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }


    /**
     * Convert to URI from Bitmap
     *
     * @param inContext
     * @param inImage
     * @return
     */
    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, null, null);
        return Uri.parse(path);
    }


    /**
     * Solve the image rotation problem when taking photos
     */
    public static Bitmap rotateImage(Bitmap bitmap, String photoPath) {
        try {
            ExifInterface ei = null;

            ei = new ExifInterface(photoPath);

            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    Log.d(TAG, "Rotate image by " + 90);
                    return rotate(bitmap, 90);

                case ExifInterface.ORIENTATION_ROTATE_180:
                    Log.d(TAG, "Rotate image by " + 180);
                    return rotate(bitmap, 180);

                case ExifInterface.ORIENTATION_ROTATE_270:
                    Log.d(TAG, "Rotate image by " + 270);
                    return rotate(bitmap, 270);

                // etc.
            }

        } catch (IOException e) {
            //e.printStackTrace();
            Log.e(TAG, e.toString());
        }
        Log.d(TAG, "Image is not rotated!");
        return bitmap;
    }


    /**
     * rotate a bitmap
     *
     * @param bitmap
     * @param degree
     * @return
     */
    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.postRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }


    /**
     * create scaled bitmap for compression
     * @param bitmap
     * @param option two types are available: fuel image and profile image
     * @return
     */

    public static Bitmap createScaledBitmap(Bitmap bitmap, String option) {

        int reqWidth;
        int reqHeight;
        switch (option)
        {
            case ConfigConstant.IMAGE_TYPE_FUEL:
                //mainly for fuel image
                reqWidth=ConfigConstant.MAX_IMAGE_WIDTH;
                reqHeight=ConfigConstant.MAX_IMAGE_HEIGHT;
                break;
            case ConfigConstant.IMAGE_TYPE_PROFILE:
                //mainly for profile image
                reqWidth=ConfigConstant.PROFILE_IMAGE_WIDTH;
                reqHeight=ConfigConstant.PROFILE_IMAGE_HEIGHT;
                break;
            default:
                //default is to be fuel image
                reqWidth=ConfigConstant.MAX_IMAGE_WIDTH;
                reqHeight=ConfigConstant.MAX_IMAGE_HEIGHT;
                break;

        }


        if (bitmap.getWidth() > reqWidth)
            return Bitmap.createScaledBitmap(bitmap,
                    reqWidth,
                    (int) (reqWidth / (float) bitmap.getWidth() * bitmap.getHeight()),
                    true);
        else if (bitmap.getHeight() >reqHeight)
            return Bitmap.createScaledBitmap(bitmap,
                    (int) (reqHeight / (float) bitmap.getHeight() * bitmap.getWidth()),
                    reqHeight,
                    true);

        else
            return bitmap;
    }

    public static Bitmap setProfileImageSquare(Bitmap bitmap)
    {
        return Bitmap.createScaledBitmap(bitmap,
                ConfigConstant.PROFILE_IMAGE_WIDTH,
                ConfigConstant.PROFILE_IMAGE_HEIGHT,
                false);

    }

}
