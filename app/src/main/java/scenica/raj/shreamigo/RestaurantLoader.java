package scenica.raj.shreamigo;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * Created by DELL on 12/4/2016.
 */

public class RestaurantLoader extends AsyncTaskLoader<List<Restaurant>> {

    private String mUrl;
    private List<Restaurant> finalRestaurantsList;

    public RestaurantLoader(Context context, String mUrl) {
        super(context);
        this.mUrl = mUrl;
        Log.d("this", "1");
        Log.d("main activity", " RestaurantLoader: ");
    }

    @Override
    protected void onStartLoading() {
        Log.d("main activity", " onStartLoading: ");
        if (finalRestaurantsList == null)
            forceLoad();
        Log.d("this", " 2");
    }


    @Override
    public List<Restaurant> loadInBackground() {
        Log.d("main activity", " loadInBackground first line: ");
        if (mUrl == null) {
            return null;
        } else {
            Log.d("this", " 3");
            List<Restaurant> restaurants = QueryUtils.fetchRestaurantData(mUrl);
            Log.d("main activity", " loadInBackground after fetchRestaurantData: ");
            Log.d("loadInBackground", "countOfApiCalls: " + MainActivity.countOfApiCalls);
            Log.d("RestaurantLoader", "restaurant url :" + mUrl);
            Log.d("RestaurantLoader", "restaurant size :" + restaurants.size());
            finalRestaurantsList = restaurants;
            return restaurants;
        }
    }


}
