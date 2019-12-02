package scenica.raj.shreamigo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RestaurantDetails extends AppCompatActivity {

    Intent intent;
    RecyclerView recyclerView;
    ArrayList<Restaurant> restaurantArrayList;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);

        toolbar = (Toolbar) findViewById(R.id.tool_bar_res_details);
        setSupportActionBar(toolbar);

        intent = getIntent();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
        restaurantArrayList = new ArrayList<>();

        databaseReference.child("Cuisine Choices").child(pref.getString("user_uid", null)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<UserPreferenceData> userPreferenceDataArrayList = new ArrayList<UserPreferenceData>();
                for (DataSnapshot restaurantSnapshot : dataSnapshot.getChildren()) {
                    Log.d("res_details activity", "Cuisine names: " + restaurantSnapshot.getValue());
                    userPreferenceDataArrayList.add(restaurantSnapshot.getValue(UserPreferenceData.class));
                }

                extractRestaurantDetails(userPreferenceDataArrayList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    public void setValue(final ArrayList<Restaurant> restaurantData) {

        RestaurantAdapter restaurantAdapter = new RestaurantAdapter(restaurantData, getApplicationContext(), 1);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(restaurantAdapter);
        restaurantAdapter.notifyItemInserted(restaurantData.size());
        View loadingIndicator = findViewById(R.id.loading_indicator_restaurant_details);
        loadingIndicator.setVisibility(View.GONE);
    }


    public void extractRestaurantDetails(final ArrayList<UserPreferenceData> userPreferenceData) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        final SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
        final String id = intent.getStringExtra("restaurant_id");
        Log.d("res_details activity", "restaurant_id: " + id);
        final String[] cuisineIdArray = id.split(",");
        final String cuisineName = intent.getStringExtra("cuisine_name");
        final DatabaseReference ref = databaseReference.child("Restaurants List").child(pref.getString("user_uid", null));


        recyclerView = (RecyclerView) findViewById(R.id.restaurant_details_cardList);
        Log.d("res_details activity", "stringArray: " + cuisineIdArray.toString());

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot dataSnapshotChildren : dataSnapshot.getChildren()) {
                    Log.d("res_details activity", "reference: " + dataSnapshotChildren.child(pref.getString("user_uid", null)).getRef());
                    dataSnapshotChildren.child(pref.getString("user_uid", null)).getRef().addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d("res_details activity", "dataSnapshot: " + dataSnapshot.getValue());
                            for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                Restaurant restaurant = singleSnapshot.getValue(Restaurant.class);
                                Log.d("res_details activity", "restaurant: " + restaurant);
                                restaurantArrayList.add(restaurant);
                            }

                            searchRestaurantWithID(restaurantArrayList, cuisineIdArray
                            );
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void searchRestaurantWithID(ArrayList<Restaurant> restaurantArrayList, String[] cuisineIdArray) {
        ArrayList<Restaurant> restaurants = new ArrayList<>();
        for (int i = 0; i < cuisineIdArray.length; i++) {
            for (int j = 0; j < restaurantArrayList.size(); j++) {
                if (Integer.parseInt(restaurantArrayList.get(j).getId()) == Integer.parseInt(cuisineIdArray[i]))
                    restaurants.add(restaurantArrayList.get(j));
            }

        }

        Log.d("res_details activity", "restaurantArrayList: " + restaurants.size());
        setValue(restaurants);
    }
}


//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot dataSnapshotChildren : dataSnapshot.getChildren()) {
//                    final DatabaseReference reference = dataSnapshotChildren.child(pref.getString("user_uid", null)).getRef();
//                    for (int j = 0; j < cuisineIdArray.length; j++) {
//                        Log.d("res_details activity", "reference: " + dataSnapshotChildren.getRef().toString());
//                        Log.d("res_details activity", "stringArray: " + cuisineIdArray[j]);
//                        reference.orderByChild("id").equalTo(cuisineIdArray[j]).addChildEventListener(new ChildEventListener() {
//                            @Override
//                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                                Log.d("res_details activity", "dataSnapshot: " + dataSnapshot.getValue());
////                                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
//                                Restaurant restaurant = dataSnapshot.getValue(Restaurant.class);
//                                Log.d("res_details activity", "restaurant: " + restaurant);
//                                restaurantArrayList.add(restaurant);
////                                }
//                                Log.d("res_details activity", "restaurantArrayList: " + restaurantArrayList.size());
//                                setValue(restaurantArrayList);
//                            }
//
//
//                            @Override
//                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//                            }
//
//                            @Override
//                            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//                            }
//
//                            @Override
//                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//                            }
//
//                            @Override
//                            public void onCancelled(DatabaseError databaseError) {
//
//                            }
//                        });
//
//
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }
//}

//        ref.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });


//        ref.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                ddListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                            }
//
//                            @Override
//                            public void onCancelled(DatabaseError databaseError) {
//
//                                Log.d("res_details activity", "databaseError: " + databaseError);
//
//                            }
//                        });
//
//                    }
//                }
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

//        }

//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                    int i =0;
//                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
////                    Log.d("res_details activity", "singleSnapshot: " + singleSnapshot.toString());
//                    final DatabaseReference reference = singleSnapshot.child(pref.getString("user_uid", null)).getRef();
//
//                    Log.d("res_details activity", "userPreferenceData: " + userPreferenceData.get(i).getSelectedCuisineName());
//                    for (int j = 0; j < stringArray.length ; j++) {
//                        Log.d("res_details activity", "stringArray: " + stringArray[j]);
//                        Log.d("res_details activity", "reference: " + reference.toString());
//                        reference.orderByChild("id").equalTo(stringArray[j]).addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                Log.d("res_details activity", "dataSnapshot: " + dataSnapshot.getValue());
//                                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
//                                    Restaurant restaurant = singleSnapshot.getValue(Restaurant.class);
//                                    Log.d("res_details activity", "restaurant: " + restaurant);
//                                    restaurantArrayList.add(restaurant);
//                                    Log.d("res_details activity", "restaurantArrayList: " + restaurantArrayList.size());
//                                    setValue(restaurantArrayList);
//                                }
//                            }
//
//                            @Override
//                            public void onCancelled(DatabaseError databaseError) {
//                                Log.d("res_details activity", "databaseError: " + databaseError);
//                            }
//                        });
//                    }
//                    i++;
//                }
//
//
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });


//    public static class RestaurantDetailsViewHolder extends RecyclerView.ViewHolder {
//
//        ImageView restaurant_image;
//        TextView rating_new;
//        TextView restaurant_name;
//        TextView locality;
//        TextView phone;
//        TextView cuisines;
//        TextView avg_cost;
//        TextView address;
//        LinearLayout call_button;
//        LinearLayout website;
//        LinearLayout direction;
//        RatingBar ratingBar;
//
//
//        public RestaurantDetailsViewHolder(View view) {
//            super(view);
//            restaurant_image = (ImageView) view.findViewById(R.id.restaurant_images);
//            rating_new = (TextView) view.findViewById(R.id.rating_new);
//            restaurant_name = (TextView) view.findViewById(R.id.res_name_restaurant_details);
//            locality = (TextView) view.findViewById(R.id.locality_restaurant_details);
//            phone = (TextView) view.findViewById(R.id.phone_restaurant_details);
//            cuisines = (TextView) view.findViewById(R.id.cuisine_restaurant_details);
//            avg_cost = (TextView) view.findViewById(R.id.average_cost_restaurant_details);
//            address = (TextView) view.findViewById(R.id.address);
//            call_button = (LinearLayout) view.findViewById(R.id.call_button);
//            website = (LinearLayout) view.findViewById(R.id.website);
//            direction = (LinearLayout) view.findViewById(R.id.direction_restaurant_details);
//            ratingBar = (RatingBar)view.findViewById(R.id.rating_bar);
//
//        }
//    }
//}
