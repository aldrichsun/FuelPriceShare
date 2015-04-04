package unimelb.cis.spatialanalytics.fuelpriceshare.maps.autoComplete;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.test.LoaderTestCase;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import unimelb.cis.spatialanalytics.fuelpriceshare.R;
import unimelb.cis.spatialanalytics.fuelpriceshare.data.Users;
import unimelb.cis.spatialanalytics.fuelpriceshare.maps.locationHistory.SearchLocationHistory;

/**
 * To set the auto complete adapter we must implement the filterable interface, as
 * required by the android auto complete documentation
 * Created by Yu Sun on 6/02/2015.
 */
public class AutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {

    private static final String LOG_TAG = AutoCompleteAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<String> resultList; // the result list storing the auto complete

    private int HISTORY_COUNT = 0;
    private int MAX_HISTORY_RESULT = 2; // the maximum number of history results we shown

    private int TYPE_HISTORY = 0; // the type of historical address list view
    private int TYPE_API = 1; // the type of api address list view

    private String SEP_TOKEN = ", "; // the separation token for addresses

    //private boolean BLACK_TEXT = true; // Added by Yu Sun on 09/03/2015: used for avoiding
                // non-historical results shown as a 'Dark Green' color. We also cannot set
                // the text color for each drop down list entry, as that will produce a
                // significant delay in the text color change for historical results.

    /**
     * Constructor of the adapter
     * @param context -- the activity where to display the auto complete addresses
     * @param textViewResourceId -- the text view resource id in the layout of the activity
     */
    public AutoCompleteAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        mContext = context;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public String getItem(int index) {

        //Log.e(LOG_TAG, "In getItem, the result size is: " + resultList.size());
        //Log.e(LOG_TAG, "In getItem, the position is: " + index);
        return resultList.get(index);
    }

    @Override
    public Filter getFilter() {

        Filter filter = new Filter() {

            /**
             * Yu Sun 01/02/2015: Given the user input, this function constructs the
             * content of the auto complete prediction list.
             *
             * @param constraint -- the user input
             * @return -- a list of the prediction addresses including (added on 04/03/2015)
             * the historical search result
             */
            // Function required by the auto complete documentation
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults filterResults = new FilterResults();
                if (constraint != null) {

//                    if( resultList != null ){ // clear the
//
//                    }
                    ArrayList<String> newResultList = new ArrayList<>();
                    if( constraint.equals( mContext.getString(R.string.Your_location) ) ){

                        resultList = newResultList;
                        // Assign the data to the FilterResults
                        filterResults.values = newResultList;
                        filterResults.count = newResultList.size();

                        return filterResults;
                    }

                    ///////////////// Historical addresses //////////////////
                    // 03/03/2015 Yu Sun: search in the retrieved history and add the
                    // historical addresses BEGINS with the "constraint"
                    HISTORY_COUNT = 0;
                    String constraint_string = constraint.toString();
                    ArrayList<String> history = SearchLocationHistory.get( Users.id );
                    for(int i = 0; i < history.size(); i++){

                        String address = history.get(i);
                        if( address.toLowerCase().startsWith( constraint_string.toLowerCase() ) ){
                            newResultList.add( address );
                            ++HISTORY_COUNT;
                            if( HISTORY_COUNT >= MAX_HISTORY_RESULT )
                                break;
                        }
                    }

                    /////////////////////////////////////////////////////////
                    //////////////// Google auto complete addresses ///////////////

                    //resultList.addAll( GoogleSuggestion.autoComplete(constraint.toString()) );

                    ArrayList<String> googleResult =
                            GoogleSuggestion.autoComplete(constraint.toString());
                    // Remove the duplicate predictions in history and google results
                    for(int i = 0; i < googleResult.size(); i++){

                        boolean added = false;
                        // Since there are very few history results, we use the simple nested loop.
                        // If there are more history results, we could use a TreeSet.
                        for(int j = 0; j < HISTORY_COUNT; j++){
                            if( googleResult.get(i).equals( newResultList.get(j) ) ){
                                added = true; break;
                            }
                        }
                        if( !added )
                            newResultList.add( googleResult.get(i) );
                    }
                    ///////////////////////////////////////////////////////

                    resultList = newResultList;
                    // Assign the data to the FilterResults
                    filterResults.values = newResultList;
                    filterResults.count = newResultList.size();
                }
                else{ // 03/03/2015 Yu Sun: TODO show the search history when nothing is input

                }
                return filterResults;
            }

            // Function required by the auto complete documentation
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }

    /**
     * Override by Yu Sun on 03/03/2015:
     * to customize the auto complete drop down list view
     * @param position -- the position (index) of the item in the list
     * @param convertView -- the UI view where we obtain UI components to manipulate
     * @param parent -- (not used in current implementation)
     * @return the UI view
     *
     * revise the list view according to
     * http://www.survivingwithandroid.com/2014/08/android-listview-with-multiple-row.html
     * http://android.amberfog.com/?p=296
     * in order to show the history addresses with a different ICON
     *
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        int type = getItemViewType(position);
        if( convertView == null ) {

            if( type == TYPE_HISTORY )
                convertView = ((ActionBarActivity) mContext).getLayoutInflater()
                        .inflate(R.layout.address_historical_list_item, parent, false);
            else // type == TYPE_API
                convertView = ((ActionBarActivity) mContext).getLayoutInflater()
                        .inflate(R.layout.address_api_list_item, parent, false);
        }

        TextView nameView = (TextView) convertView.findViewById(R.id.location_name);
        TextView otherView = (TextView) convertView.findViewById(R.id.location_suburb_country);

        String address = getItem(position);

        // TODO either use the regular expression that can recognize the user input or modify the user input when storing
        String[] addrArray = address.split( SEP_TOKEN );

        String first_line;
        String second_line;
        // TODO improve this view
        if( addrArray.length <= 1 && addrArray.length > 0 ){
            first_line = addrArray[0];
            second_line = "Australia"; //TODO change this if needs international access
        }
        else if( addrArray.length <= 4 && addrArray.length >= 2 ){
            first_line = addrArray[0];
            second_line = addrArray[1];
            for(int i = 2; i < addrArray.length; i++)
                second_line += SEP_TOKEN + addrArray[i];
        }
        else{ // length >= 5
            /* changed by Yu Sun on 03/04/2015 */
//            first_line = addrArray[0] + SEP_TOKEN + addrArray[1];
//            second_line = addrArray[2];
//            for(int i = 3; i < addrArray.length; i++)
//                second_line += SEP_TOKEN + addrArray[i];
            first_line = addrArray[0];
            second_line = addrArray[1];
            for(int i = 2; i < addrArray.length; i++)
                second_line += SEP_TOKEN + addrArray[i];
        }

        nameView.setText(first_line);
        otherView.setText(second_line);

        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return ( position < HISTORY_COUNT ) ? TYPE_HISTORY : TYPE_API;
    }
}
