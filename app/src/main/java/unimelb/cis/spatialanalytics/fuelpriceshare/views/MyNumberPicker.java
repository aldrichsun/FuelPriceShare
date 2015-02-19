package unimelb.cis.spatialanalytics.fuelpriceshare.views;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import java.text.DecimalFormat;

import unimelb.cis.spatialanalytics.fuelpriceshare.R;


/**
 * Created by hanl4 on 2/02/2015.
 * This is a customized NumberPicker class enabling multiple NumberPickers to present certain values.
 * One numberpicker indicates one digit.
 */
public class MyNumberPicker {
    NumberPicker[] nps;
    int counts=5;//number of NumberPickers in the NumberPicker array nps.
    String[] values = new String[10];//default NumberPicker value represents a digit from 0 to 9

    String[] decimalVales = new String[10];//default NumberPicker value represents a digit from 0 to 9


    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    int decimalIndex = 3;

    public MyNumberPicker(Context context) {

        nps = new NumberPicker[counts];
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        /**
         * Initialize NumberPicker, and can be modified later by xml approach
         */
        for (int i = 0; i < counts; i++) {

            nps[i] = (NumberPicker) inflater.inflate(R.layout.my_numberpicker,null);
            //nps[i] = new NumberPicker(context);
            //nps[i].setLayoutParams(params);
            nps[i].setScaleX(0.6f);
            nps[i].setScaleY(0.6f);

        }

        /**
         * Get the numbers
         */

        for (int i = 0; i < values.length; i++) {

            decimalVales[i] = "." + Integer.toString(i);

            values[i] = Integer.toString(i);


        }

        /**
         * Set up every NumberPicker
         */
        for (int i = 0; i < counts; i++) {
            nps[i].setMaxValue(values.length - 1);
            nps[i].setMinValue(0);
            if (i == decimalIndex)
                nps[i].setDisplayedValues(decimalVales);
            else
                nps[i].setDisplayedValues(values);

            nps[i].setOnValueChangedListener(onValueChanged);
        }


    }


    /**
     * Ge the values of each NumberPicker
     */
    NumberPicker.OnValueChangeListener onValueChanged
            = new NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(
                NumberPicker picker,
                int oldVal,
                int newVal) {

            String temp = "";
            for (int i = 0; i < counts; i++) {
                String[] values = nps[i].getDisplayedValues();
                temp = values[nps[i].getValue()] + temp;

            }

            System.out.println("Display Number is: " + temp);

        }
    };


    /**
     * Get the NumberPicker of the array by position id.
     *
     * @param i NumberPicker position id in the array nps.
     * @return NumberPicker locating at the position i of array nps
     */
    public NumberPicker getNumberPicker(int i) {
        if (i > counts) {
            Log.e("MyNumberPicker", "Over Number of NumberPickers");
            return null;
        }
        return nps[i];
    }


    /**
     * Set the value to myNumberPicker;
     *
     * @param val     the value want to assign
     * @param pattern the format of the value
     * @return true means the setting is success; otherwise return false.
     */

    public boolean setValue(double val, String pattern) {
        if (pattern == null)
            pattern = "000.00";

        if (pattern.length() - 1 > counts) {
            Log.e("MyNumberPicker", "Over Number of NumberPickers");
            return false;
        }
        String newVal = new DecimalFormat(pattern).format(val);


        int i = 0;

        for (char letter : newVal.toCharArray()) {
            if (letter != '.') {
                int dig = Character.getNumericValue(letter);
                nps[i].setValue(dig);
                i++;
            }

        }


        return true;


    }


    /**
     * get the value of myNumberPicker ( the number the the set of NumberPickers represent)
     *
     * @return the summation of every NumberPicker in the array nps
     */
    public double getValues() {
        double result = 0;

        for (int i = 0; i < counts; i++) {
            result = result + (double) nps[i].getValue() * Math.pow(10, counts - i - 3);
        }




        DecimalFormat f = new DecimalFormat("000.00");
        String newVal = f.format(result);


        return Double.valueOf(newVal);

    }


    /**
     * Set visibility of NumberPickers
     *
     * @param flag if flag is true, NumberPicker is visible; otherwise it is invisible.
     */
    public void setVisibility(boolean flag) {
        for (int i = 0; i < counts; i++) {
            if (flag)
                nps[i].setVisibility(View.VISIBLE);
            else
                nps[i].setVisibility(View.INVISIBLE);

        }

    }

    public int getCounts(){
        return counts;
    }


}
