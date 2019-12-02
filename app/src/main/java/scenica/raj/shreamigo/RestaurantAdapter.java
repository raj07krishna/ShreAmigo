package scenica.raj.shreamigo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by DELL on 12/3/2016.
 */

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {

    private static final String TAG = "RestaurantAdapter";
    public List<Restaurant> restaurantList;
    private Context context;
    private int from;

    public RestaurantAdapter(List<Restaurant> restaurantList, Context context, int from) {
        Log.d(TAG, " RestaurantAdapter");
        this.restaurantList = restaurantList;
        this.context = context;
        this.from = from;
    }

    @Override
    public void onBindViewHolder(RestaurantViewHolder holder, int position) {
        Log.d(TAG, "inside of onBindViewHolder");
        final Restaurant restaurant = restaurantList.get(position);
        try {
            Picasso.with(context).load(restaurant.getImage_url()).into(holder.restaurant_image);

        } catch (IllegalArgumentException e) {
            holder.restaurant_image.setImageResource(R.drawable.image_not_available);
        }

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.parseColor(restaurant.getRating_color()));
        gd.setCornerRadius(10);
        gd.setStroke(2, Color.WHITE);
//        rating.setText(restaurant.getRating());
//        rating.setBackground(gd);
        holder.restaurant_name.setText(restaurant.getName());
        holder.locality.setText(restaurant.getAddressLocality() + ", " + restaurant.getAddressCity());
        holder.cuisines.setText(restaurant.getCuisines());
//        int height_in_pixels = cuisines.getLineCount() * cuisines.getLineHeight(); //approx height text
//        cuisines.setHeight(height_in_pixels);

        holder.rating_new.setText(restaurant.getRating());
        holder.rating_new.setBackground(gd);

        holder.ratingBar.setRating(Float.parseFloat(restaurant.getRating()));
//        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
//        stars.getDrawable(2).setColorFilter(Color.parseColor(restaurant.getRating_color()), PorterDuff.Mode.SRC_ATOP);

        String average_cost = restaurant.getCurrency().concat(" ").concat(restaurant.getAverageCostForTwo());
        holder.avg_cost.setText(average_cost);
        if (restaurant.getPhoneNumber() == null) {
            holder.phone.setText("N/A");
        } else
            holder.phone.setText(restaurant.getPhoneNumber());
        holder.address.setText(restaurant.getAddress());

        if (restaurant.getPhoneNumber() == null) {
            holder.call_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "Phone Number not available",
                            Toast.LENGTH_LONG).show();
                }
            });

        } else {
            holder.call_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse(restaurant.getPhoneNumber()));
                        v.getContext().startActivity(callIntent);
                    } catch (Exception e) {
                        Log.d(TAG, "tablet exception: " + e.toString());
                        Toast.makeText(context, "No CALLING facility on device",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        holder.website.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent();
                browserIntent.setAction(Intent.ACTION_VIEW);
                browserIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                browserIntent.setData(Uri.parse(restaurant.getResUrl()));
                v.getContext().startActivity(browserIntent);
            }
        });

        holder.images_url.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent();
                browserIntent.setAction(Intent.ACTION_VIEW);
                browserIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                browserIntent.setData(Uri.parse(restaurant.getPhotos_url()));
                v.getContext().startActivity(browserIntent);
            }
        });

        holder.direction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent directionIntent = new Intent(Intent.ACTION_VIEW);
                directionIntent.setData(Uri.parse("http://maps.google.com/maps?daddr=" + restaurant.getLatitude() + "," + restaurant.getLongitude()));
                v.getContext().startActivity(directionIntent);
            }
        });


    }

    @Override
    public int getItemCount() {
        Log.d(TAG, " getItemCount: " + restaurantList.size());
        return restaurantList.size();
    }

    @Override
    public RestaurantViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, " onCreateViewHolder: ");
        View itemView;
        if (from == 1) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.res_details_card_view, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent, false);
        }

        return new RestaurantViewHolder(itemView);
    }

    public void clearData() {
        int size = this.restaurantList.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                this.restaurantList.remove(0);
            }

            this.notifyItemRangeRemoved(0, size);
        }
    }

    public static class RestaurantViewHolder extends RecyclerView.ViewHolder {

        protected ImageView restaurant_image;
        protected TextView rating_new;
        protected TextView restaurant_name;
        protected TextView locality;
        protected TextView phone;
        protected TextView cuisines;
        protected TextView avg_cost;
        protected TextView address;
        protected LinearLayout call_button;
        protected LinearLayout website;
        protected LinearLayout direction;
        protected LinearLayout images_url;
        protected RatingBar ratingBar;


        public RestaurantViewHolder(View view) {
            super(view);
            restaurant_image = (ImageView) view.findViewById(R.id.restaurant_images);
            rating_new = (TextView) view.findViewById(R.id.rating_new);
            restaurant_name = (TextView) view.findViewById(R.id.res_name_restaurant_details);
            locality = (TextView) view.findViewById(R.id.locality_restaurant_details);
            phone = (TextView) view.findViewById(R.id.phone_restaurant_details);
            cuisines = (TextView) view.findViewById(R.id.cuisine_restaurant_details);
            avg_cost = (TextView) view.findViewById(R.id.average_cost_restaurant_details);
            address = (TextView) view.findViewById(R.id.address);
            call_button = (LinearLayout) view.findViewById(R.id.call_button);
            website = (LinearLayout) view.findViewById(R.id.website);
            direction = (LinearLayout) view.findViewById(R.id.direction_restaurant_details);
            images_url = (LinearLayout) view.findViewById(R.id.images_restaurant_details);
            ratingBar = (RatingBar) view.findViewById(R.id.rating_bar);

        }
    }
}


