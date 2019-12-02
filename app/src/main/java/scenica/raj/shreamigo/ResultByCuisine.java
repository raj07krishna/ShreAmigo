package scenica.raj.shreamigo;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;


public class ResultByCuisine extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public final static String TAG = "ResultByCuisin activity";
    ArrayList<String> cuisineId = new ArrayList<>();
    ArrayList<String> cuisineName = new ArrayList<>();
    ArrayList<UserPreferenceData> cuisineList;
    ArrayList<Restaurant> restaurantArrayList;
    LinearLayoutManager layoutManager;

    RecyclerView recyclerView;
    Toolbar toolbar;
    private BroadcastReceiver broadcastReceiver;
    private TextView emptyStateTextView;
    private ProgressDialog mProgressDialog;
    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient, googleApiClient;

    private Outside_ResultByCuisineAdapter insideResultByCuisineAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_by_cuisine);

        toolbar = (Toolbar) findViewById(R.id.tool_bar_resultbycuisine);
        setSupportActionBar(toolbar);

        emptyStateTextView = (TextView) findViewById(R.id.user_cuisine_empty_view);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.requestIdToken))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();

        SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
        if (pref.getBoolean("cuisine_updated", false)) {
            startService(new Intent(this, GPSService.class));
        } else {
            Log.d(TAG, " else showList: ");
            showList();
        }


//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//        String cuisine_name = dataSnapshotChildren.getKey();
//        intent.putExtra("cuisineName_fromList", cuisine_name);
//        startActivity(intent);


//        final FirebaseRecyclerAdapter<UserPreferenceData, ResultByCuisineAdapterViewHolder> adapter = new FirebaseRecyclerAdapter<UserPreferenceData, ResultByCuisineAdapterViewHolder>(UserPreferenceData.class, R.layout.inside_resultbycuisine_card, ResultByCuisineAdapterViewHolder.class, ref) {
//            @Override
//            protected void populateViewHolder(final ResultByCuisineAdapterViewHolder viewHolder, final UserPreferenceData model, int position) {
//                viewHolder.cuisineTextview.setText(model.getSelectedCuisineName());
//                Log.d("ResultByCuisine activty", "getSelectedCuisineName: " + model.getSelectedCuisineName());
//
//                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
////
//                        String cuisine_name = model.getSelectedCuisineName();
//                        intent.putExtra("cuisineName_fromList", cuisine_name);
//                        startActivity(intent);
//                    }
//                });
//                Log.d(TAG, "before setValue : " + model.getSelectedCuisineName());
//                setValue(model.getSelectedCuisineName());
//            }
//        };
//
////        cuisineList = new ArrayList<>();
////
////        insideResultByCuisineAdapter = new Outside_ResultByCuisineAdapter(this, cuisineList);
//        recyclerView.setAdapter(adapter);

//        setRecyclerView();

//        ConnectivityManager connMgr = (ConnectivityManager)
//                getSystemService(Context.CONNECTIVITY_SERVICE);
//
//        // Get details on the currently active default data network
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//
//        // If there is a network connection, fetch data
//        if (networkInfo != null && networkInfo.isConnected()) {
//            // Get a reference to the LoaderManager, in order to interact with loaders.
//            loaderManager = getLoaderManager();
//
//            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
//            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
//            // because this activity implements the LoaderCallbacks interface).
//            loaderManager.initLoader(RESTAURANT_LOADER_ID, null, this);
//        } else {
//            // Otherwise, display error
//            // First, hide loading indicator so error message will be visible
//            View loadingIndicator = findViewById(R.id.loading_indicator);
//            loadingIndicator.setVisibility(View.GONE);
//
//            // Update empty state with no connection error message
//            emptyStateTextView.setText(R.string.no_internet_connection);
//        }
//        ref.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                try{
////                   ArrayList<String> cuisineName = dataSnapshot.getValue(ArrayList<>);
////                    Log.d("ResultByCuisin activity", "getValue" +dataSnapshot.getValue().toString());
////
////                    cuisineList.add(cuisineName);
//                    insideResultByCuisineAdapter.notifyItemInserted(cuisineList.size()-1);
//                }
//                catch (Exception e)
//                {
//                    Log.e("ResultByCuisine","exception: "+ e);
//                }
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
//
//
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        googleApiClient.connect();

        IntentFilter intent = new IntentFilter("scenica.intent.action.FIREBASE_DATA_UPDATED");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, " onReceive: ");
                showList();
            }
        };
        this.registerReceiver(broadcastReceiver, intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.unregisterReceiver(broadcastReceiver);
        hideProgressDialog();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }
    //    public void setRecyclerView()
//    {
//        final SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
//        final DatabaseReference ref = databaseReference.child("Restaurants List").child(pref.getString("user_uid", null));
//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                String cuisineName;
//                for (DataSnapshot dataSnapshotChildren : dataSnapshot.getChildren()) {
////                    cuisineName = dataSnapshotChildren.getKey();
//                    textView.setText(dataSnapshotChildren.getKey());
//                    final DatabaseReference reference = dataSnapshotChildren.child(pref.getString("user_uid", null)).getRef();
//                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            Log.d(TAG, "onDataChange setRecyclerView : ");
//                            for (DataSnapshot dataSnapshotChildren : dataSnapshot.getChildren()) {
//                                Restaurant restaurant = dataSnapshotChildren.getValue(Restaurant.class);
//                                Log.d(TAG, "restaurant: " + restaurant.getId());
//                                restaurantArrayList.add(restaurant);
//
//                            }
//                            Log.d(TAG, "before setAdapter : ");
//                            setAdapter(restaurantArrayList,"cusiine");
//
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//                            Log.d(TAG, "onCancelled : " + databaseError);
//                        }
//                    });
//
//                }
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }

//    public void setValue(String cuisine) {
//        Log.d(TAG, "setValue : ");
//        final SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
//        final DatabaseReference ref = databaseReference.child("Restaurants List").child(pref.getString("user_uid", null)).child(cuisine).child(pref.getString("user_uid", null));
//        Query query = ref.orderByChild("rating");
//        query.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Log.d(TAG, "onDataChange setValue : ");
//                for (DataSnapshot dataSnapshotChildren : dataSnapshot.getChildren()) {
//                    Restaurant restaurant = dataSnapshotChildren.getValue(Restaurant.class);
//                    Log.d(TAG, "restaurant: " + restaurant);
//                    restaurantArrayList.add(restaurant);
//
//                }
//                Log.d(TAG, "before setAdapter : ");
//                setAdapter(restaurantArrayList, "q");
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }

//    public void setAdapter(ArrayList<Restaurant> restaurantData, final String cuisine) {
//
//        RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.main_relative_view);
////        textView.setText(cuisine);
//        final Intent intent = new Intent(this, MainActivity.class);
//        relativeLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                intent.putExtra("cuisineName_fromList", cuisine);
//                        startActivity(intent);
//            }
//        });
//
//        Log.d(TAG, "setAdapter : ");
//        CustomLayoutManager layoutManager
//                = new CustomLayoutManager(this, LinearLayoutManager.HORIZONTAL, false, 2);
//        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.user_cuisine_cardList_first);
//        recyclerView.setLayoutManager(layoutManager);
//        RestaurantAdapter adapter = new RestaurantAdapter(restaurantData, getApplicationContext(), 0);
//        recyclerView.setAdapter(adapter);
//        adapter.notifyItemInserted(restaurantData.size());
//    }

//    public static class RestaurantViewHolder extends RecyclerView.ViewHolder {
//
//        protected ImageView resImageView;
//        protected TextView rating;
//        protected TextView resName;
//        protected TextView locality;
//        protected RatingBar ratingBar;
//        View mView;
//
//
//        public RestaurantViewHolder(View view) {
//            super(view);
//            resImageView = (ImageView) view.findViewById(R.id.res_image);
//            rating = (TextView) view.findViewById(R.id.rating_new_main);
//            resName = (TextView) view.findViewById(R.id.res_name);
//            locality = (TextView) view.findViewById(R.id.locality);
//            ratingBar = (RatingBar) view.findViewById(R.id.rating_bar_main);
//            mView = view;
//
//        }
//    }

//    public static class ResultByCuisineAdapterViewHolder extends RecyclerView.ViewHolder {
//
//        protected TextView cuisineTextview;
//        View mView;
//
//        public ResultByCuisineAdapterViewHolder(View itemView) {
//            super(itemView);
//            mView = itemView.findViewById(R.id.main_relative_view);
//            cuisineTextview = (TextView) itemView.findViewById(R.id.user_cuisine_name);
//        }
//    }

//    @Override
//    public Loader<List<Restaurant>> onCreateLoader(int id, Bundle args) {
//
//        if(urlForRestaurants == null)
//        {
//            return new RestaurantLoader(this, null);
//        }
//        else {
//            Uri baseUri = Uri.parse(urlForRestaurants);
//            Uri.Builder uriBuilder = baseUri.buildUpon();
//            Log.d("numbering", "0");
//            Log.d("main activity", "onCreateLoader: ");
//
//            return new RestaurantLoader(this, uriBuilder.toString());
//        }
//
//    }
//
//    @Override
//    public void onLoadFinished(Loader<List<Restaurant>> loader, List<Restaurant> restaurantData) {
//        Log.d("main activity", "onLoadFinished: " );
//        if (restaurantData != null && !restaurantData.isEmpty())
//        {
//            MainActivity.restaurantList.addAll(restaurantData);
//        }
//    }
//
//
//    @Override
//    public void onLoaderReset(Loader<List<Restaurant>> loader)
//        {
//
//        Log.d("main activity", "in onLoaderReset after restaurant adapter");
//        Log.d("main activity", "onLoaderReset: " );
////        restaurantAdapter.clearData();
//    }
//
//
//
//    private GeofencingRequest getGeofencingRequest() {
//        Log.d("main activity", " getGeofencingRequest: " );
//        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
//        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
//        builder.addGeofences(geofenceList);
//        return builder.build();
//    }
//
//    private PendingIntent getGeofencePendingIntent() {
//        Log.d("main activity", " getGeofencePendingIntent: " );
//        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
//        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling addgeoFences()
//        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//    }
//
//    public void populateGeofenceList() {
//        Log.d("main activity", " createRestaurantGeofenceList: " );
//        for (Map.Entry<String, LatLng> entry : geoFenceHashList.entrySet()) {
//
//            geofenceList.add(new Geofence.Builder()
//                    // Set the request ID of the geofence. This is a string to identify this
//                    // geofence.
//                    .setRequestId(entry.getKey())
//
//                    // Set the circular region of this geofence.
//                    .setCircularRegion(
//                            entry.getValue().latitude,
//                            entry.getValue().longitude,
//                            GEOFENCE_RADIUS_IN_METERS
//                    )
//
//                    // Set the expiration duration of the geofence. This geofence gets automatically
//                    // removed after this period of time.
//                    .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
//
//                    // Set the transition types of interest. Alerts are only generated for these
//                    // transition. We track entry and exit transitions in this sample.
//                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
//                            Geofence.GEOFENCE_TRANSITION_EXIT)
//
//                    // Create the geofence.
//                    .build());
//        }
//    }
//
//    @Override
//    public void updateNewLocation(Location location, GoogleApiClient googleApiClient) {
//        Log.d("main activity", " updateNewLocation: " );
//        this.googleApiClient = googleApiClient;
//        recyclerView.setVisibility(View.GONE);
//        emptyStateTextView.setText(String.valueOf(location.getLatitude()));
//
//
//    }
//
//    @Override
//    public void onResult(@NonNull Status status) {
//        Log.d("main activity", " onResult: " );
//        if (status.isSuccess()) {
//            Toast.makeText(
//                    this,
//                    "Geofences Added",
//                    Toast.LENGTH_SHORT
//            ).show();
//        } else {
//            // Get the status code for the error and log it using a user-friendly message.
//            String errorMessage = GeofenceErrorMessages.getErrorString(this,
//                    status.getStatusCode());
//            Log.d("main activity", " onResult: geofence error:  " + errorMessage );
//        }
//    }
//
//
//
//
//    public  void createGeofence(List<Restaurant> restaurantData)
//    {
//        for (int i = 0; i < restaurantData.size(); i++) {
//            geoFenceHashList.put(restaurantData.get(i).getId(), new LatLng(Double.parseDouble(restaurantData.get(i).getLatitude()), Double.parseDouble(restaurantData.get(i).getLongitude())));
//            Log.d("main activity", "restaurantData.get(i).getName(): " + restaurantData.get(i).getName());
//            Log.d("main activity", "restaurantData.get(i).getLatitude(): " + restaurantData.get(i).getLatitude());
//
//        }
//        Log.d("main activity", "restaurantData.size(): " + restaurantData.size());
//        Log.d("main activity", "geoFenceHashList: " + geoFenceHashList.size());
//        Log.d("main activity", "onLoadFinished: ");
//        populateGeofenceList();
//
//        try {
//            LocationServices.GeofencingApi.addGeofences(
//                    googleApiClient,
//                    // The GeofenceRequest object.
//                    getGeofencingRequest(),
//                    // A pending intent that that is reused when calling removeGeofences(). This
//                    // pending intent is used to generate an intent when a matched geofence
//                    // transition is observed.
//                    getGeofencePendingIntent()
//            ).setResultCallback(this); // Result processed in onResult().
//        } catch (SecurityException securityException) {
//            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
//        }
//
//        Log.d("main activity", "after if in loadFinished");
//    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {

            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.signout) {
            signOut();
        }
        return super.onOptionsItemSelected(item);
    }

    public void sendValueToAdapter(ArrayList<ArrayList<Restaurant>> restaurants, ArrayList<String> cuisineName) {
        Outside_ResultByCuisineAdapter adapter = new Outside_ResultByCuisineAdapter(getApplicationContext(), cuisineName, restaurants);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.user_cuisine_cardList_first);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        View loadingIndicator = findViewById(R.id.user_cuisine_loading_indicator);
        loadingIndicator.setVisibility(View.GONE);
        emptyStateTextView.setVisibility(View.GONE);
    }

    public void showList() {
        Log.d(TAG, "showList: ");
        final SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
        restaurantArrayList = new ArrayList<>();
        String tempCuisineId = pref.getString("cuisineId", null);
        String tempCuisineName = pref.getString("cuisineName", null);


        if (tempCuisineId != null) {
            try {
                JSONArray jsonArrayId = new JSONArray(tempCuisineId);
                JSONArray jsonArrayName = new JSONArray(tempCuisineName);
                if (jsonArrayId.length() == jsonArrayName.length()) {

                    for (int i = 0; i < jsonArrayId.length(); i++) {
                        cuisineId.add(jsonArrayId.optString(i));
                        cuisineName.add(jsonArrayName.optString(i));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        recyclerView = (RecyclerView) findViewById(R.id.user_cuisine_cardList_first);
        emptyStateTextView = (TextView) findViewById(R.id.user_cuisine_empty_view);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference ref = databaseReference.child("Restaurants List").child(pref.getString("user_uid", null)).getRef();
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setAutoMeasureEnabled(true);
        final Intent restaurantDetailsIntent = new Intent(this, RestaurantDetails.class);
        final ArrayList<String> cuisineNameFromFirebase = new ArrayList<>();
        final ArrayList<ArrayList<Restaurant>> restaurantArrayList = new ArrayList<ArrayList<Restaurant>>();
        final int[] count = {0};
        Log.d(TAG, "ref.getKey(): " + ref.getKey());
        Log.d(TAG, "ref.toString(): " + ref.toString());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "dataSnapshot: " + dataSnapshot.hasChildren());
                if (dataSnapshot.hasChildren()) {
                    for (final DataSnapshot dataSnapshotChildren : dataSnapshot.getChildren()) {
                        Log.d(TAG, "onDataChange  dataSnapshotChildren: " + dataSnapshotChildren.getKey());
                        Log.d(TAG, "Restaurant Names: " + dataSnapshotChildren.getValue());
//                    textView.setText(dataSnapshotChildren.getKey());
//                    onclickFunction(dataSnapshotChildren.getKey());
                        cuisineNameFromFirebase.add(dataSnapshotChildren.getKey());
                        Log.d(TAG, "cuisineNameFromFirebase.size: " + cuisineNameFromFirebase.size());

                        DatabaseReference databaseReference1 = ref.child(dataSnapshotChildren.getKey()).child(pref.getString("user_uid", null)).getRef();
                        Query query = databaseReference1.orderByChild("rating").limitToLast(2);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final ArrayList<Restaurant> restaurants = new ArrayList<>();
                                Log.d(TAG, "restaurants.size: " + restaurants.size());
                                for (DataSnapshot dataSnapshotChildren : dataSnapshot.getChildren()) {
                                    restaurants.add(dataSnapshotChildren.getValue(Restaurant.class));
                                    Log.d(TAG, "dataSnapshotChildren: " + dataSnapshotChildren.hasChildren());
                                    Log.d(TAG, "dataSnapshot: " + dataSnapshot.hasChildren());
                                }
                                restaurantArrayList.add(restaurants);
                                count[0]++;

                                if (count[0] == cuisineNameFromFirebase.size()) {

                                    sendValueToAdapter(restaurantArrayList, cuisineNameFromFirebase);
                                } else {
                                    Toast.makeText(ResultByCuisine.this, "Database sync error, Please signout and sign in again.", Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                } else {
                    emptyStateTextView.setText("No Restaurants nearby");
                    View loadingIndicator = findViewById(R.id.user_cuisine_loading_indicator);
                    loadingIndicator.setVisibility(View.GONE);

                }
                Log.d(TAG, "cuisineNameFromFirebase: " + cuisineNameFromFirebase);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void signOut() {
        Log.d(TAG, "signOut");
        mAuth = FirebaseAuth.getInstance();

        showProgressDialog();
        deleteGeofence(mGoogleApiClient);
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();

    }

    public void deleteGeofence(final GoogleApiClient mGoogleApiClient) {
        final ArrayList<GeofenceDetails> geofenceDetailsArrayList = new ArrayList<>();
        SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference ref = databaseReference.child("Geofence Details").child(pref.getString("user_uid", null)).getRef();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GeofenceDetails geofenceDetails;

                for (DataSnapshot dataSnapshotChildren : dataSnapshot.getChildren()) {
                    geofenceDetails = dataSnapshotChildren.getValue(GeofenceDetails.class);
                    geofenceDetailsArrayList.add(geofenceDetails);
                }

                signOutProcess(geofenceDetailsArrayList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void signOutProcess(ArrayList<GeofenceDetails> geofenceDetailsArrayList) {
        String CITY_GEOFENCE_ID = "city";
        ArrayList<String> removeGeofenceIDs = new ArrayList<>();
        for (int i = 0; i < geofenceDetailsArrayList.size(); i++) {
            removeGeofenceIDs.add(geofenceDetailsArrayList.get(i).getUniqueID());
        }
        removeGeofenceIDs.add(CITY_GEOFENCE_ID);
        Log.d("Geofence activity", " googleApiClient: " + mGoogleApiClient.isConnected());
        LocationServices.GeofencingApi.removeGeofences(googleApiClient, removeGeofenceIDs).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    // remove drawing
                    Log.d("Geofence activity", " removing geofence successful ");
                } else {
                    Log.e("Geofence activity", "Removing geofence failed: " + status.getStatusMessage());
                }
            }
        });
        // Firebase sign out
        mAuth.signOut();
        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        hideProgressDialog();
                    }
                });

        SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = pref.edit();
        ed.putBoolean("activity_executed", false);
        ed.apply();

        startActivity(new Intent(ResultByCuisine.this, GoogleSignInActivity.class));
        Toast.makeText(ResultByCuisine.this, "Sign out successful", Toast.LENGTH_SHORT).show();
        finish();
    }
}

