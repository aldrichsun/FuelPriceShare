package unimelb.cis.spatialanalytics.fuelpriceshare.others;

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

/**
 * Created by Yu Sun on 20/02/2015.
 * This class creates the customized markers to show on Google Map.
 * The customized marker shows the fuel price directly without any clicking.
 * Thanks to: http://stackoverflow.com/questions/13763545/android-maps-api-v2-with-custom-markers
 */
public class CustomizeMapMarker {


    public static Bitmap generateBitmapFromText( Context context, String text, int color ){

        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);

        Paint paint = new Paint();
        paint.setStyle(Style.FILL);
        paint.setColor(color);
        paint.setTypeface(tf);
        paint.setTextAlign(Align.CENTER);
        paint.setTextSize(convertToPixels(context, 15));

        Rect textRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRect);

        Bitmap bm = Bitmap.createBitmap((int)(textRect.width()*1.2f), (int)(textRect.height()*1.2f), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);

        Paint bgPaint = new Paint();
        bgPaint.setAlpha(10);
        canvas.drawRect(0, 0, (int)(textRect.width()*1.2f), (int)(textRect.height()*1.2f), bgPaint);

        //Calculate the positions
        int xPos = (canvas.getWidth() / 2); // - 2;     //-2 is for regulating the x position offset

        //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) ;

        canvas.drawText(text, xPos, yPos, paint);

        return bm;
    }

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

    private static int convertToPixels(Context context, int nDP){

        final float conversionScale = context.getResources().getDisplayMetrics().density;

        return (int) ((nDP * conversionScale) + 0.5f) ;
    }


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
