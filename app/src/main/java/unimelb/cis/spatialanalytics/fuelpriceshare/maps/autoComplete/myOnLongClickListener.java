package unimelb.cis.spatialanalytics.fuelpriceshare.maps.autoComplete;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;

/**
 * Created by Yu Sun on 02/03/2015.
 */
public class myOnLongClickListener implements View.OnLongClickListener {

    ActionBarActivity actionBarActivity;

    public myOnLongClickListener(ActionBarActivity actionBarActivity){
        this.actionBarActivity = actionBarActivity;
    }

    @Override
    public boolean onLongClick(View v) {

        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) v;
        autoCompleteTextView.selectAll();

        autoCompleteTextView.requestFocus();
        InputMethodManager imm = (InputMethodManager)
                actionBarActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(autoCompleteTextView, InputMethodManager.SHOW_IMPLICIT);

        return true;
    }
}