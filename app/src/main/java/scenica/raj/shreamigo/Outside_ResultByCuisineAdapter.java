package scenica.raj.shreamigo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by DELL on 12/29/2016.
 */

public class Outside_ResultByCuisineAdapter extends RecyclerView.Adapter<Outside_ResultByCuisineAdapter.ResultByCuisineAdapterViewHolder> {

    public final static String TAG = "ResultByCuisineAdapter";
    Inside_resultByCuisine_Adapter adapter;
    private Context context;
    private ArrayList<String> cuisineList;
    private ArrayList<ArrayList<Restaurant>> restaurants;


    public Outside_ResultByCuisineAdapter(Context context, ArrayList<String> cuisineList, ArrayList<ArrayList<Restaurant>> restaurants) {
        this.context = context;
        this.cuisineList = cuisineList;
        this.restaurants = restaurants;
        Log.d(TAG, "Outside_ResultByCuisineAdapter: ");
    }

    @Override
    public ResultByCuisineAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "Outside_ResultByCuisineAdapter: ");
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.outside_resultbycuisine_card, parent, false);
        return new ResultByCuisineAdapterViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ResultByCuisineAdapterViewHolder holder, final int position) {


        Log.d(TAG, "onBindViewHolder: ");
        Log.d(TAG, "restaurants.size: " + restaurants.size());
        holder.cuisineTextview.setText(cuisineList.get(position));
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                String cuisine_name = cuisineList.get(position);
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("cuisineName_fromList", cuisine_name);
                view.getContext().startActivity(intent);
            }
        });

        adapter = new Inside_resultByCuisine_Adapter(context, restaurants.get(position));
        holder.recyclerView.setAdapter(adapter);

    }

    @Override
    public int getItemCount() {
//        Log.d(TAG, "getItemCount: " + cuisineList.size());
        return cuisineList.size();
    }

    public static class ResultByCuisineAdapterViewHolder extends RecyclerView.ViewHolder {

        protected TextView cuisineTextview;
        RecyclerView recyclerView;
        RelativeLayout relativeLayout;

        public ResultByCuisineAdapterViewHolder(View itemView) {
            super(itemView);
            Log.d(TAG, "ResultByCuisineAdapterViewHolder: ");
            cuisineTextview = (TextView) itemView.findViewById(R.id.cuisine_name_resultByCuisine);
            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.upper_border);

            recyclerView = (RecyclerView) itemView.findViewById((R.id.inside_cardList));
            CustomLayoutManager layoutManager = new CustomLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false, 2);
            layoutManager.setReverseLayout(true);
            layoutManager.setStackFromEnd(true);
            recyclerView.setLayoutManager(layoutManager);

        }
    }
}
