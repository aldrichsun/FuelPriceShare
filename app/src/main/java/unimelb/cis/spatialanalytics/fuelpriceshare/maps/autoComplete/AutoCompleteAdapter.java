package unimelb.cis.spatialanalytics.fuelpriceshare.maps.autoComplete;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;

/**
 * To set the auto complete adapter we must implement the filterable interface, as
 * required by the android auto complete documentation
 * Created by Yu Sun on 6/02/2015.
 */
public class AutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {

    private ArrayList<String> resultList; // the result list storing the auto complete

    /**
     * Constructor of the adapter
     * @param context -- the activity where to display the auto complete addresses
     * @param textViewResourceId -- the text view resource id in the layout of the activity
     */
    public AutoCompleteAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public String getItem(int index) {
        return resultList.get(index);
    }

    @Override
    public Filter getFilter() {

        Filter filter = new Filter() {

            // Function required by the auto complete documentation
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults filterResults = new FilterResults();
                if (constraint != null) {

                    // Retrieve the auto complete results.
                    resultList = AutoComplete.autoComplete(constraint.toString());

                    // Assign the data to the FilterResults
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
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
}
