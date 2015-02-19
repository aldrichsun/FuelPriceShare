package unimelb.cis.spatialanalytics.fuelpriceshare.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import unimelb.cis.spatialanalytics.fuelpriceshare.config.ConfigConstant;


/**
 * Created by hanl4 on 2/02/2015.
 * This class is used to draw fuel information onto a given input fuel image in the format of Bitmap,
 * and translate the whole drawing into a new bitmap and return it back.
 */

public class CustermizedCanvasView extends View {


    Context context;// parent context
    Paint paint_text = new Paint();//draw the text view
    Paint paint_rect = new Paint();//draw the rectangle encapsulate the text
    Paint paint_select = new Paint();// draw the rectangle when it is selected

    Bitmap bitmap;// input original fuel price image in Bitmap format


    public CustermizedCanvasView(Context context, Bitmap bitmap) {
        super(context);
        this.bitmap = bitmap;
        this.context = context;

        /**
         * Mainly set up the styles for each paint such as text size, color, etc.
         */
        paint_text = new Paint();
        paint_text.setColor(Color.BLUE);
        /**
         * set text size. TO DO: This setting might be changed later to make the size of text dynamically
         * changed based on the input to make it adjustable.
         */
        paint_text.setTextSize(30);
        Paint.FontMetrics fm = new Paint.FontMetrics();
        paint_text.setTextAlign(Paint.Align.CENTER);
        paint_text.getFontMetrics(fm);


        paint_rect.setColor(Color.BLUE);
        paint_rect.setStyle(Paint.Style.STROKE);

        paint_select.setColor(Color.RED);
        paint_select.setStyle(Paint.Style.STROKE);
        paint_select.setStrokeWidth(10);


    }

    public void setBitmap(Bitmap bitmap)
    {
        this.bitmap = bitmap;
    }

    /**
     * Draw all the fuel information onto the given input bitmap, known as the fuel price image; and then store
     * the drawing into a new Bitmap object called newBitmap, and then return it back.
     * @param fuelJsonArray fuel information stored in ArrayList<JSONObject>
     * @return new drawing bitmap of the current view
     */
    public Bitmap getBitmap(ArrayList<JSONObject> fuelJsonArray) {


        Bitmap newBitmap = null;


        Bitmap.Config config = bitmap.getConfig();
        if (config == null) {
            config = Bitmap.Config.ARGB_8888;
        }


        newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), config);
        //make the size of new drawing equal to the original input bitmap
        Rect bitmap_rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        Canvas canvas = new Canvas(newBitmap);

        canvas.drawBitmap(bitmap, null, bitmap_rect, new Paint());

        /**
         * recursively draw every fuel information onto the bitmap. Each fuel information was encapsulated
         * by a rectangle. Please notice that, once the rectangle is selected, the style of drawing might
         * change to make it outstanding from other ones.
         */
        try {
            String str;
            int left, top, right, bottom;
            Rect rect;
            float x, y;
            boolean flag = false;


            for (int i = 0; i < fuelJsonArray.size(); i++)

            {
                JSONObject json = fuelJsonArray.get(i);

                if (json.has(ConfigConstant.KEY_FUEL_BRAND))
                    str = json.getString(ConfigConstant.KEY_FUEL_BRAND);
                else
                    str = json.getString(ConfigConstant.KEY_FUEL_PRICE);


                flag = json.getBoolean(ConfigConstant.FLAG_IS_SELECTED);
                /**
                 * set up the coordinates for rectangle
                 */
                left = json.getInt(ConfigConstant.KEY_RECT_LEFT);
                top = json.getInt(ConfigConstant.KEY_RECT_TOP);
                right = json.getInt(ConfigConstant.KEY_RECT_RIGHT);
                bottom = json.getInt(ConfigConstant.KEY_RECT_BOTTOM);
                rect = new Rect(left, top, right, bottom);

                /**
                 * Dynamically define where to put the rectangle to make it alienable to the text.
                 * It is a little bit tricky cause layout of text drawing that we have to define them
                 * in a certain way to make them compatible each other.
                 */

                x = (rect.right + rect.left) / 2;
                y = (rect.bottom + rect.top + paint_text.getTextSize()) / 2;

                canvas.drawText(str, x, y, paint_text);
                if (flag)
                    canvas.drawRect(rect, paint_select);
                else
                    canvas.drawRect(rect, paint_rect);


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return newBitmap;
    }


}