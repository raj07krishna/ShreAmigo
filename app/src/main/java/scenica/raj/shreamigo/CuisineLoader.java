package scenica.raj.shreamigo;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * Created by DELL on 12/4/2016.
 */

public class CuisineLoader extends AsyncTaskLoader<List<Cuisine>> {

    private String cuisineUrl;
    private List<Cuisine> finalCuisineList;

    public CuisineLoader(Context context, String cuisineUrl) {
        super(context);
        this.cuisineUrl = cuisineUrl;
        Log.d("CuisineLoader activity", " RestaurantLoader: ");
    }

    @Override
    protected void onStartLoading() {

        if (finalCuisineList == null)
            forceLoad();
        Log.d("CuisineLoader activity", " onStartLoading: ");
    }

    @Override
    public List<Cuisine> loadInBackground() {

        if (cuisineUrl == null) {
            Log.d("CuisineLoader activity", " loadInBackground if: ");
            return null;
        } else {
            Log.d("CuisineLoader activity", " loadInBackground else: ");
            List<Cuisine> cuisines = CuisineQueryUtils.fetchCuisineData(cuisineUrl);
            finalCuisineList = cuisines;
            return cuisines;
        }
    }
}
