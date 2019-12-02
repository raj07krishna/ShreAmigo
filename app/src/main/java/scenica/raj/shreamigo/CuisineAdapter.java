package scenica.raj.shreamigo;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DELL on 12/13/2016.
 */

public class CuisineAdapter extends ArrayAdapter<Cuisine> {

    private NameFilter filter;
    private ArrayList<Cuisine> cuisineData = new ArrayList<>();
    private ArrayList<Cuisine> originalList;

    public CuisineAdapter(Activity context, List<Cuisine> cuisineArrayList) {
        super(context, 0, cuisineArrayList);
        cuisineData = (ArrayList) cuisineArrayList;
        this.originalList = new ArrayList<>();
        this.originalList.addAll(cuisineArrayList);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listItemView = convertView;
        CuisineHolder cuisineHolder = null;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.cuisine_view, parent, false);
            cuisineHolder = new CuisineHolder();
            cuisineHolder.textView = (TextView) listItemView.findViewById(R.id.cuisine_textview);
            cuisineHolder.checkbox = (CheckBox) listItemView.findViewById(R.id.itemCheckBox);
            cuisineHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int getPosition = (Integer) buttonView.getTag();
                    cuisineData.get(getPosition).setSelected(buttonView.isChecked());
                }
            });

            listItemView.setTag(cuisineHolder);
            listItemView.setTag(R.id.cuisine_textview, cuisineHolder.textView);
            listItemView.setTag(R.id.itemCheckBox, cuisineHolder.checkbox);
        } else {
            cuisineHolder = (CuisineHolder) convertView.getTag();
        }

        cuisineHolder.checkbox.setTag(position);
        cuisineHolder.textView.setText(cuisineData.get(position).getCuisineName());
        cuisineHolder.checkbox.setChecked(cuisineData.get(position).isSelected());

        return listItemView;

    }

    @NonNull
    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new NameFilter();
        }
        return filter;
    }

    static class CuisineHolder {
        protected TextView textView;
        protected CheckBox checkbox;
    }

    private class NameFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            constraint = constraint.toString().toLowerCase();
            FilterResults result = new FilterResults();
            if (constraint != null && constraint.toString().length() > 0) {
                ArrayList<Cuisine> filteredItems = new ArrayList<>();

                for (int i = 0, l = originalList.size(); i < l; i++) {
                    Cuisine Cuisine = originalList.get(i);
                    if (Cuisine.getCuisineName().toLowerCase().contains(constraint))
                        filteredItems.add(Cuisine);
                }
                result.count = filteredItems.size();
                result.values = filteredItems;
            } else {
                synchronized (this) {
                    result.values = originalList;
                    result.count = originalList.size();
                }
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {

            cuisineData = (ArrayList<Cuisine>) results.values;
            notifyDataSetChanged();
            clear();
            Log.d("numbering", "cuisineData.size(): " + cuisineData.size());
            for (int i = 0, l = cuisineData.size(); i < l; i++)
                add(cuisineData.get(i));
            notifyDataSetInvalidated();
        }
    }
}
