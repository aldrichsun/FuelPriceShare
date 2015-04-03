package unimelb.cis.spatialanalytics.fuelpriceshare.maps.DrawOnMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBarActivity;

import unimelb.cis.spatialanalytics.fuelpriceshare.R;

/**
 * Created by Yu Sun on 20/02/2015.
 * This 'functional class' creates the customized markers to show on Google Map.
 * The customized marker shows the fuel price directly without any clicking.
 * Thanks to: http://stackoverflow.com/questions/13763545/android-maps-api-v2-with-custom-markers
 */
public class CustomizeMapMarker {


    /**
     * This function generates a bitmap using the given text (with a transparent rectangle
     * background), or for better understanding converts the text into a bitmap image.
     * @param context -- the context (or activity) we do the conversion
     * @param text -- the text to be converted
     * @param color -- the color of the text in the generated image
     * @return a bitmap with a transparent background showing the given text in the given color
     */
    public static Bitmap generateBitmapFromText( Context context, String text, int color ){

        if( text == null || text.isEmpty() )
            return generateDotBitmap();


        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);

        Paint paint = new Paint();
        paint.setStyle(Style.FILL);
        paint.setColor(color);
        paint.setTypeface(tf);
        paint.setTextAlign(Align.CENTER);
        paint.setTextSize(convertToPixels(context, 15));

        Rect textRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRect);

        // For text background: a rectangle
        int rect_width = (int)(textRect.width()*1.2f);
        int rect_height = (int)(textRect.height()*1.2f);
        // For the dot to show the precise location
        int radius = 5; // pixels

        Bitmap bm = Bitmap.createBitmap( rect_width, rect_height + 2*radius, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);

        ///////////////////////// draw the background rect //////////////////////
        Paint bgPaint = new Paint();
        bgPaint.setAlpha(10);
        canvas.drawRect(0, 0, rect_width, rect_height, bgPaint);
        //////////////////////////////////////////////////////////////////////////

        ////////////////////////// draw the text ////////////////////
        //Calculate the positions
        int xPos = (rect_width / 2); // - 2;     //-2 is for regulating the x position offset

        //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
        int yPos = (int) ((rect_height / 2) - ((paint.descent() + paint.ascent()) / 2)) ;

        canvas.drawText(text, xPos, yPos, paint);
        //////////////////////////////////////////////////////////////

        ////////////////// draw the dot to show the exact location //////////////
        Paint circlePaint = new Paint();
        // set color
        //circlePaint.setColor(context.getResources().getColor(R.color.orange));
        //circlePaint.setColor(context.getResources().getColor(R.color.red));
        circlePaint.setColor(Color.RED);
        // set style
        circlePaint.setStyle(Style.FILL);
        // draw circle with radius 30
        int c_xPos = (rect_width / 2);
        int c_yPos = rect_height + radius;
        canvas.drawCircle(c_xPos, c_yPos, radius, circlePaint);
        //////////////////////////////////////////////////////////////////////////

        return bm;
    }

    /**
     * This function generates a bitmap with a single red dot of radius 5.
     * @return a bitmap with a single red dot of radius 5.
     */
    private static Bitmap generateDotBitmap( ){

        int radius = 5; // the dot radius in pixels
        int color = Color.RED; //the color of the dot

        Bitmap bm = Bitmap.createBitmap( 2*radius, 2*radius, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);

        ////////////////// draw the dot to show the exact location //////////////
        Paint circlePaint = new Paint();
        // set color
        //circlePaint.setColor(context.getResources().getColor(R.color.orange));
        //circlePaint.setColor(context.getResources().getColor(R.color.red));
        circlePaint.setColor( color );
        // set style
        circlePaint.setStyle(Style.FILL);
        // draw circle with radius 30
        int c_xPos = radius;
        int c_yPos = radius;
        canvas.drawCircle(c_xPos, c_yPos, radius, circlePaint);
        //////////////////////////////////////////////////////////////////////////

        return bm;
    }

    /**
     * This function generates a bitmap using the given text with the given background.
     * In other words, it writes the text into the given image.
     * The given background (image) is a drawable file, which is either a Shape Drawable (xml file)
     * or a Bitmap file.
     * @param actionBarActivity -- the activity we perform the generation
     * @param context -- the context (or activity) we perform the generation
     * @param drawableId -- the id of the drawable file
     * @param text -- the text to be written into the background
     * @param color -- the color of the text in the generated image
     * @return a bitmap with the given background showing the given text in the given color
     */
    public static Bitmap writeTextOnDrawable(ActionBarActivity actionBarActivity,
              Context context, int drawableId, String text, int color) {

        Bitmap bm = getBitmap(actionBarActivity, drawableId);

        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);

        Paint paint = new Paint();
        paint.setStyle(Style.FILL);
        paint.setColor( color );
        paint.setTypeface(tf);
        paint.setTextAlign(Align.CENTER);
        paint.setTextSize(convertToPixels(context, 15));

        Rect textRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRect);

        Canvas canvas = new Canvas(bm);

        //If the text is bigger than the canvas , reduce the font size
        if(textRect.width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
            paint.setTextSize(convertToPixels(context, 12));        //Scaling needs to be used for different dpi's

        //Calculate the positions
        int xPos = (canvas.getWidth() / 2) + 2;     //+2 is for regulating the x position offset

        //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) ;

        canvas.drawText(text, xPos, yPos, paint);

        return bm;
    }

    /**
     * This function converts the metrics DP into pixel.
     * Increase the given nDP if need a larger text.
     */
    private static int convertToPixels(Context context, int nDP){

        final float conversionScale = context.getResources().getDisplayMetrics().density;

        return (int) ((nDP * conversionScale) + 0.5f) ;
    }


    /** This function converts a Shape drawable into a bitmap or creates a copy of a Btimap,
     * depending on the given drawable resource type. */
    // Yu Sun 20/02/2015:
    // Thanks to:
    // http://stackoverflow.com/questions/24389043/bitmapfactory-decoderesourse-returns-null-for-shape-defined-in-xml-drawable
    // http://stackoverflow.com/questions/3035692/how-to-convert-a-drawable-to-a-bitmap
    private static Bitmap getBitmap ( ActionBarActivity actionBarActivity, int drawableId ) {

        Drawable drawable = actionBarActivity.getResources().getDrawable(drawableId);

        // This is for Bitmap file.
        if (drawable instanceof BitmapDrawable) {
            return BitmapFactory.decodeResource(actionBarActivity.getResources(), drawableId)
                .copy(Bitmap.Config.ARGB_8888, true);
        }
        else {
            // Yu Sun 20/02/2015: This is for Shape Drawable (xml file)
            Bitmap bitmap = Bitmap.createBitmap(
                    drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }
    }

}
