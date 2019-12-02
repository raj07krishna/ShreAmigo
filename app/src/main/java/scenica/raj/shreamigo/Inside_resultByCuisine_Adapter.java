package scenica.raj.shreamigo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


/**
 * Created by DELL on 2/1/2017.
 */
public class Inside_resultByCuisine_Adapter extends RecyclerView.Adapter<Inside_resultByCuisine_Adapter.OutsideRecylerViewViewHolder> {

    public final static String TAG = "Inside_resultByCuisin";

    Context context;
    ArrayList<Restaurant> restaurants;

    public Inside_resultByCuisine_Adapter(Context context, ArrayList<Restaurant> arrayList) {
        this.restaurants = arrayList;
        this.context = context;
        Log.d(TAG, "cuisineList : " + arrayList.toString());
        Log.d(TAG, "cuisineList : " + arrayList.size());
    }

    @Override
    public OutsideRecylerViewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder : ");
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.inside_resultbycuisine_card, parent, false);
        return new OutsideRecylerViewViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final OutsideRecylerViewViewHolder viewHolder, int position) {
        Log.d(TAG, "onBindViewHolder : ");
        final Restaurant model = restaurants.get(position);
        final Intent restaurantDetailsIntent = new Intent(context, RestaurantDetails.class);
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.parseColor(model.getRating_color()));
        gd.setCornerRadius(10);
        gd.setStroke(2, Color.WHITE);
        try {
            Picasso.with(context).load(model.getImage_url()).into(viewHolder.resImageView);

        } catch (IllegalArgumentException e) {
            viewHolder.resImageView.setImageResource(R.drawable.image_not_available);
        }


        viewHolder.rating.setText(model.getRating());
        viewHolder.rating.setBackground(gd);
        viewHolder.ratingBar.setRating(Float.parseFloat(model.getRating()));
        viewHolder.votes.setText(model.getVotes());
        Log.d(TAG, "rating color: " + model.getRating());


        viewHolder.resName.setText(model.getName());
//                            viewHolder.locality.setText(model.getAddressLocality() + ", " + model.getAddressCity());


        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//
                String restaurant_id = model.getId();
                restaurantDetailsIntent.putExtra("restaurant_id", restaurant_id);
                v.getContext().startActivity(restaurantDetailsIntent);
            }
        });

    }

    @Override
    public int getItemCount() {
//        Log.d(TAG, "getItemCount : " );
        return restaurants.size();

    }


    public static class OutsideRecylerViewViewHolder extends RecyclerView.ViewHolder {


        protected ImageView resImageView;
        protected TextView rating;
        protected TextView resName;
        protected TextView locality;
        protected RatingBar ratingBar;
        protected TextView votes;
        View mView;


        public OutsideRecylerViewViewHolder(View itemView) {
            super(itemView);
//            imageView = (ImageView) itemView.findViewById(R.id.user_cuisine_imageview);
            Log.d(TAG, "OutsideRecylerViewViewHolder : ");
            resImageView = (ImageView) itemView.findViewById(R.id.res_image);
            rating = (TextView) itemView.findViewById(R.id.rating_new_main);
            resName = (TextView) itemView.findViewById(R.id.res_name);
            locality = (TextView) itemView.findViewById(R.id.locality);
            ratingBar = (RatingBar) itemView.findViewById(R.id.rating_bar_main);
            votes = (TextView) itemView.findViewById(R.id.cuisine_votes);
            mView = itemView;


        }
    }
}

