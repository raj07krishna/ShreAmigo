package scenica.raj.shreamigo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
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
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static ArrayList<Restaurant> restaurantList = new ArrayList<>();
    public static int countOfApiCalls;
    RecyclerView recyclerView;
    private TextView emptyStateTextView;
    private Toolbar toolbar;
    private GoogleApiClient mGoogleApiClient, googleApiClient;
    private ProgressDialog mProgressDialog;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.tool_bar_main);
        setSupportActionBar(toolbar);

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

        Intent intent = getIntent();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
//        if(databaseReference.child("Restaurants List").child(pref.getString("user_uid", null)).getKey() != null)
//        {
//            startService(new Intent(this,GPSService.class));
//        }
        String cuisineNameFromList = intent.getStringExtra("cuisineName_fromList");
        recyclerView = (RecyclerView) findViewById(R.id.cardList);
        emptyStateTextView = (TextView) findViewById(R.id.empty_view);
//        ArrayList<String> cuisineName = extractCuisineName();
        Log.d("this", "no data: " + databaseReference.child("Restaurants List").child(pref.getString("user_uid", null)).child(cuisineNameFromList).child(cuisineNameFromList).child(pref.getString("user_uid", null)).getKey());
        DatabaseReference ref = databaseReference.child("Restaurants List").child(pref.getString("user_uid", null)).child(cuisineNameFromList).child(pref.getString("user_uid", null)).getRef();
        Query query = ref.orderByChild("rating");
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        final Intent restaurantDetailsIntent = new Intent(this, RestaurantDetails.class);

        FirebaseRecyclerAdapter<Restaurant, RestaurantViewHolder> adapter = new FirebaseRecyclerAdapter<Restaurant, RestaurantViewHolder>(Restaurant.class, R.layout.card_view, RestaurantViewHolder.class, query) {
            @Override
            protected void populateViewHolder(RestaurantViewHolder viewHolder, final Restaurant model, int position) {

                try {
                    Picasso.with(getBaseContext()).load(model.getImage_url()).into(viewHolder.resImageView);

                } catch (IllegalArgumentException e) {
                    viewHolder.resImageView.setImageResource(R.drawable.image_not_available);
                }

                GradientDrawable gd = new GradientDrawable();
                gd.setColor(Color.parseColor(model.getRating_color()));
                gd.setCornerRadius(10);
                gd.setStroke(2, Color.WHITE);

                viewHolder.rating.setText(model.getRating());
                viewHolder.rating.setBackground(gd);
                viewHolder.ratingBar.setRating(Float.parseFloat(model.getRating()));
                Log.d("main activity", "rating color: " + model.getRating());
                viewHolder.votes.setText(model.getVotes());

                viewHolder.resName.setText(model.getName());
                viewHolder.locality.setText(model.getAddressLocality() + ", " + model.getAddressCity());


                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//
                        String restaurant_id = model.getId();

                        restaurantDetailsIntent.putExtra("restaurant_id", restaurant_id);
                        startActivity(restaurantDetailsIntent);
                    }
                });
            }
        };

        recyclerView.setAdapter(adapter);
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

//        startService(new Intent(this,GPSService.class));
//        if (restaurantAdapter.getItemCount()==0) {
//            recyclerView.setVisibility(View.GONE);
//            emptyStateTextView.setVisibility(View.VISIBLE);
//        } else {
//            recyclerView.setVisibility(View.VISIBLE);
//            emptyStateTextView.setVisibility(View.GONE);
//        }

//        Log.d("main activity", "countOfApiCalls: " + countOfApiCalls);
//        ConnectivityManager connMgr = (ConnectivityManager)
//                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
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
//        Log.d("main activity", "befefore geofencelist: ");
//        geofenceList = new ArrayList<Geofence>();
//        Log.d("before printGeofencelis", "countOfApiCalls: " + countOfApiCalls);
////        printGeoFenceList();
//        Log.d("after printGeofencelist", "countOfApiCalls: " + countOfApiCalls);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("Main Activity", "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

//    @Override
//    public Loader<List<Restaurant>> onCreateLoader(int id, Bundle args) {
//        Log.d("main activity", "url for loading: " + urlForRestaurants);
//
//        if (urlForRestaurants == null) {
//            return new RestaurantLoader(this, null);
//        } else {
//            Uri baseUri = Uri.parse(urlForRestaurants);
//            Uri.Builder uriBuilder = baseUri.buildUpon();
//            Log.d("numbering", "0");
//            Log.d("main activity", "onCreateLoader: ");
//
//            return new RestaurantLoader(this, uriBuilder.toString());
//        }
//    }
//
//    @Override
//    public void onLoadFinished(Loader<List<Restaurant>> loader, List<Restaurant> restaurantData) {
//
//        View loadingIndicator = findViewById(R.id.loading_indicator);
//        loadingIndicator.setVisibility(View.GONE);
//
////        emptyStateTextView.setText(R.string.no_restaurants);
//
////        restaurantAdapter.clearData();
//        Log.d("onLoadFinished", "countOfApiCalls: " + countOfApiCalls);
//        if (restaurantData != null && !restaurantData.isEmpty()) {
//            restaurantList.addAll(restaurantData);
//            showRestaurant(restaurantList);
//
//            for (int i = 0; i < restaurantData.size(); i++) {
//                geoFenceHashList.put(restaurantData.get(i).getId(), new LatLng(Double.parseDouble(restaurantData.get(i).getLatitude()), Double.parseDouble(restaurantData.get(i).getLongitude())));
//                Log.d("main activity", "restaurantData.get(i).getName(): " + restaurantData.get(i).getName());
//                Log.d("main activity", "restaurantData.get(i).getLatitude(): " + restaurantData.get(i).getLatitude());
//
//            }
//            Log.d("main activity", "restaurantData.size(): " + restaurantData.size());
//            Log.d("main activity", "geoFenceHashList: " + geoFenceHashList.size());
//            Log.d("main activity", "onLoadFinished: ");
//            createRestaurantGeofenceList();
//            index++;
//
//        } else {
//            emptyStateTextView.setText(R.string.no_restaurants);
//            index++;
//        }
//        Log.d("main activity", "after if in loadFinished");
//        printGeoFenceList();
//        Log.d("main activity", "index : " +index);
//        if(apiCallURLs != null && index < apiCallURLs.size())
//        {
//            urlForRestaurants = apiCallURLs.get(index);
//            loaderManager.restartLoader(RESTAURANT_LOADER_ID, null, this);
//        }
//
//        Log.d("outsideofifonLoadFinish", "countOfApiCalls: " + countOfApiCalls);
//    }
//
//    @Override
//    public void onLoaderReset(Loader<List<Restaurant>> loader) {
//
//        Log.d("main activity", "in onLoaderReset after restaurant adapter");
//        Log.d("main activity", "onLoaderReset: ");
//        restaurantAdapter.clearData();
//    }
//
//
//    @Override
//    public void updateNewLocation(Location locationForService, GoogleApiClient googleApiClient) {
//        Log.d("main activity", " updateNewLocation: ");
//        this.googleApiClient = googleApiClient;
//        Toast.makeText(
//                this,
//                String.valueOf(locationForService.getLatitude()),
//                Toast.LENGTH_SHORT
//        ).show();
//        createURL(locationForService);
//
//
//    }
//
//
//    public void createURL(Location locationForService) {
//        Log.d("onLocationChanged", "countOfApiCalls: " + countOfApiCalls);
//        Log.d("main activity", " createURLFromCuisine: ");
//
//        SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
//        String tempCuisineData = pref.getString("cuisineId", null);
//        ArrayList<String> cuisineId = new ArrayList<>();
//        finalSearchURL = finalSearchURL.concat("lat=" + String.valueOf(locationForService.getLatitude()));
//        finalSearchURL = finalSearchURL.concat("&lon=" + String.valueOf(locationForService.getLongitude()));
//        finalSearchURL = finalSearchURL.concat(SEARCH_REQUEST_URL_RADIUS);
//        if (tempCuisineData == null) {
//            urlForRestaurants = finalSearchURL.concat("&start=00&count=100");
//
//        } else {
//            try {
//                JSONArray jsonArray = new JSONArray(tempCuisineData);
//                for (int i = 0; i < jsonArray.length(); i++) {
//                    cuisineId.add(jsonArray.optString(i));
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            finalSearchURL = finalSearchURL.concat(SEARCH_REQUEST_URL_CUISINE);
//            apiCallURLs = new ArrayList<>();
//            for (int i = 0; i < cuisineId.size(); i++) {
//
//
//                urlForRestaurants = finalSearchURL.concat(cuisineId.get(i));
//                apiCallURLs.add(i, urlForRestaurants);
//                Log.d("main activity", "url: " + urlForRestaurants);
//                Log.d("main activity", "cuisineId.size()" + cuisineId.size());
//
//
//            }
//            index=0;
//            urlForRestaurants = apiCallURLs.get(index);
//            Log.d("main activity", " createURLFromCuisine: ");
//        }
//        loaderManager.restartLoader(RESTAURANT_LOADER_ID, null, this);
//        Log.d("onLocationChanged", "countOfApiCalls: " + countOfApiCalls);
//        createCityGeofence(locationForService);
//    }
//
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        locationActivity.connect();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        urlForRestaurants = null;
//        locationActivity.disconnect();
//    }
//
//
//    @Override
//    public void onResult(@NonNull Status status) {
//        Log.d("main activity", " onResult: ");
//        if (status.isSuccess()) {
//            Toast.makeText(
//                    this,
//                    "Geofences Added",
//                    Toast.LENGTH_SHORT
//            ).show();
//            printGeoFenceList();
//        } else {
//            // Get the status code for the error and log it using a user-friendly message.
//            String errorMessage = GeofenceErrorMessages.getErrorString(this,
//                    status.getStatusCode());
//            Log.d("main activity", " onResult: geofence error:  " + errorMessage);
//        }
//    }
//
//    public void createRestaurantGeofenceList() {
//        Log.d("main activity", " createRestaurantGeofenceList: ");
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
//
//        geoFenceHashList = new HashMap<>();
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
//    }
//
//    private GeofencingRequest getGeofencingRequest() {
//        Log.d("main activity", " getGeofencingRequest: ");
//        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
//        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
//        builder.addGeofences(geofenceList);
//        return builder.build();
//    }
//
//    private PendingIntent getGeofencePendingIntent() {
//        Log.d("main activity", " getGeofencePendingIntent: ");
//        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
//        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling addgeoFences()
//        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//    }
//
//
//    public void printGeoFenceList() {
//        for (int i = 0; i < geofenceList.size(); i++) {
//            Log.d("main activity ", "printGeoFenceList :" + geofenceList.get(i).toString());
//        }
//
//    }
//
//    public void showRestaurant(List<Restaurant> restaurantData) {
//
//        emptyStateTextView.setVisibility(View.GONE);
//
//        Log.d("main activity", "inside of if onLoadFinished");
//        restaurantAdapter = new RestaurantAdapter(restaurantData, getApplicationContext());
//        recyclerView.setHasFixedSize(true);
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
//        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//        recyclerView.setLayoutManager(linearLayoutManager);
//        recyclerView.setAdapter(restaurantAdapter);
//        Log.d("main activity", "in onLoadFinished after restaurant adapter");
//        restaurantAdapter.notifyItemInserted(restaurantData.size() - 1);
//    }
//
//    public void createCityGeofence(Location locationForService) {
//        Geofence cityGeofence = new Geofence.Builder()
//                .setRequestId(CITY_GEOFENCE_ID)
//                .setCircularRegion(locationForService.getLatitude(), locationForService.getLongitude(), 700)
//                .setExpirationDuration(Geofence.NEVER_EXPIRE)
//                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
//                .build();
//
//        GeofencingRequest cityGeofencingRequest = new GeofencingRequest.Builder()
//                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT)
//                .addGeofence(cityGeofence)
//                .build();
//
//        try {
//            LocationServices.GeofencingApi.addGeofences(
//                    googleApiClient,
//                    // The GeofenceRequest object.
//                    cityGeofencingRequest,
//                    // A pending intent that that is reused when calling removeGeofences(). This
//                    // pending intent is used to generate an intent when a matched geofence
//                    // transition is observed.
//                    getCityGeofencePendingIntent()
//            ).setResultCallback(this); // Result processed in onResult().
//        } catch (SecurityException securityException) {
//            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
//        }
//
//    }
//
//    private PendingIntent getCityGeofencePendingIntent() {
//        Log.d("main activity", " getCityGeofencePendingIntent: ");
//        Intent intent = new Intent(this, CityGeofenceTransitionsIntentService.class);
//        SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
//        SharedPreferences.Editor ed = pref.edit();
//        ed.putBoolean("city_geofence", true);
//        ed.putBoolean("request_location", false);
//        ed.apply();
//        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling addgeoFences()
//        return PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//    }
//
////    public static void cityGeofenceTriggered(Context context)
////    {
////
////        SharedPreferences.Editor ed = pref(context).edit();
////        ed.putBoolean("request_location", true);
////        ed.apply();
////
////
////    }
////
////    public void startCityGeofenceActivity()
////    {
////        SharedPreferences.Editor ed = pref.edit();
////        ed.putBoolean("request_location", true);
////        ed.apply();
////    }
////
////    private static SharedPreferences pref(Context context) {
////        return context.getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
////    }
//

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

    public ArrayList<String> extractCuisineName() {
        SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
        String tempCuisineName = pref.getString("cuisineName", null);
        ArrayList<String> cuisineName = new ArrayList<>();

        try {

            JSONArray jsonArray1 = new JSONArray(tempCuisineName);
            for (int i = 0; i < jsonArray1.length(); i++) {
                cuisineName.add(jsonArray1.optString(i));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return cuisineName;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        googleApiClient.connect();

    }

    @Override
    protected void onStop() {
        super.onStop();

        hideProgressDialog();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    private void signOut() {
        Log.d("main activity", "signOut");
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


        startActivity(new Intent(MainActivity.this, GoogleSignInActivity.class));
        Toast.makeText(MainActivity.this, "Sign out successful", Toast.LENGTH_SHORT).show();
        finish();
    }

    public static class RestaurantViewHolder extends RecyclerView.ViewHolder {

        View mView;
        private ImageView resImageView;
        private TextView rating;
        private TextView resName;
        private TextView locality;
        private RatingBar ratingBar;
        private TextView votes;


        public RestaurantViewHolder(View view) {
            super(view);
            resImageView = (ImageView) view.findViewById(R.id.res_image);
            rating = (TextView) view.findViewById(R.id.rating_new_main);
            resName = (TextView) view.findViewById(R.id.res_name);
            locality = (TextView) view.findViewById(R.id.locality);
            ratingBar = (RatingBar) view.findViewById(R.id.rating_bar_main);
            votes = (TextView) view.findViewById(R.id.cuisine_votes);
            mView = view;

        }
    }
}