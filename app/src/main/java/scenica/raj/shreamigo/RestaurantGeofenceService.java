package scenica.raj.shreamigo;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by DELL on 1/10/2017.
 */

public class RestaurantGeofenceService extends IntentService implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    protected static final String TAG = "Geofence activity";
    protected static final String LOG_TAG = "Geofence activity";
    HashMap<String, LatLng> geoFenceHashList = new HashMap<>();
    ArrayList<Geofence> geofenceList;
    List<Restaurant> restaurantsList;
    ArrayList<GeofenceDetails> geofenceDetailsArrayList = new ArrayList<>();

    GoogleApiClient googleApiClient;
    Location locationForService;
    LocationRequest locationRequest;

    List<Restaurant> restaurants;
    ArrayList<String> cuisineName;
    ArrayList<String> removeCuisineList;


    SharedPreferences pref;
    DatabaseReference databaseReference;


    public RestaurantGeofenceService() {
        super(TAG);
        Log.d("Geofence activity", "RestaurantGeofenceService started " +
                ": ");

    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        Log.d("Geofence activity", " readFromStream: ");
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    @Override
    public void onDestroy() {
        Log.d("Geofence activity", "onDestroy: ");
        super.onDestroy();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {


        googleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
        Log.d("Geofence activity", " googleApiClient: " + googleApiClient.isConnected());

        locationForService = intent.getParcelableExtra("location");
        ArrayList<String> oldCuisineList = intent.getStringArrayListExtra("oldCuisineList");
        Log.d("Geofence activity", "location: " + locationForService.getLatitude());

        pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
        if (pref.getBoolean("city_geofence_triggered", false)) {
            Log.d("Geofence activity", "checkCuisinePresence: ");
            checkCuisinePresence(locationForService);
            SharedPreferences.Editor ed = pref.edit();
            ed.putBoolean("city_geofence_triggered", false);
            ed.apply();
        }

        Log.d("Geofence activity", "SharedPreferences: activity_executed: " + pref.getBoolean("activity_executed", false));

        databaseReference = FirebaseDatabase.getInstance().getReference();
        restaurantsList = new ArrayList<>();
        geofenceList = new ArrayList<>();
        ArrayList<String> URLlist = createURLFromCuisine(locationForService);

        for (int i = 0; i < URLlist.size(); i++) {

            SharedPreferences.Editor ed = pref.edit();
            ed.putBoolean("call_api_again", true);
            ed.apply();
            Uri baseUri = Uri.parse(URLlist.get(i));
            Uri.Builder uriBuilder = baseUri.buildUpon();
            restaurants = new ArrayList<>();
            restaurants = fetchRestaurantData(uriBuilder.toString());
            Log.d("Geofence activity", "restaurants: " + restaurants);
            restaurantsList.addAll(restaurants);


            Log.d("Geofence activity", "getref:" + databaseReference.child("Restaurants List").child(pref.getString("user_uid", null)).getRef());
            Log.d("Geofence activity", "getkey:" + databaseReference.child("Restaurants List").child(pref.getString("user_uid", null)).getKey());
            Log.d("Geofence activity", " before adding: ");
            databaseReference.child("Restaurants List").child(pref.getString("user_uid", null)).child(cuisineName.get(i)).child(pref.getString("user_uid", null)).setValue(restaurants);
            Log.d("Geofence activity", " after adding: ");
        }
        deleteRestaurantGeofenceList(intent, googleApiClient);
        Log.d("Geofence activity", "cuisineName: " + cuisineName.size());
        Log.d("Geofence activity", "oldCuisineList: " + oldCuisineList.size());
        removeCuisineList = new ArrayList<>();
        removeCuisineList.addAll(oldCuisineList);

        for (int i = 0; i < cuisineName.size(); i++) {
            for (int j = 0; j < oldCuisineList.size(); j++) {

                if (oldCuisineList.get(j).equals(cuisineName.get(i))) {
                    removeCuisineList.remove(oldCuisineList.get(j));
                    break;
                }

            }

        }

        for (int i = 0; i < restaurantsList.size(); i++) {
            geoFenceHashList.put(restaurantsList.get(i).getId(), new LatLng(Double.parseDouble(restaurantsList.get(i).getLatitude()), Double.parseDouble(restaurantsList.get(i).getLongitude())));
            Log.d("Geofence activity", "restaurantData.get(i).getName(): " + restaurantsList.get(i).getName());
            Log.d("Geofence activity", "restaurantData.get(i).getLatitude(): " + restaurantsList.get(i).getLatitude());

        }
        removeRestaurantList();
        createCityGeofence(locationForService);

        if (restaurantsList.size() > 0) {
            createRestaurantGeofenceList();
            printGeoFenceList();
            insertGeofenceIDinFirebase();
        }


    }

    public ArrayList<Restaurant> fetchRestaurantData(String requestUrl) {
        Log.d("Geofence activity", " fetchRestaurantData: ");
        ArrayList<Restaurant> finalList = new ArrayList<>();

        for (int i = 0; i < 81; i = i + 20) {
            Log.d("Geofence activity", " call_api_again: " + pref.getBoolean("call_api_again", true));
            if (pref.getBoolean("call_api_again", true)) {

                requestUrl = requestUrl.concat("&start=" + i + "&count=20");
                URL url = createUrl(requestUrl);

                // Perform HTTP request to the URL and receive a JSON response back
                String jsonResponse = null;
                try {
                    jsonResponse = makeHttpRequest(url);
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Problem making the HTTP request.", e);
                }
                Log.d("Geofence activity", " fetchRestaurantData before extractRestaurant : ");
                finalList.addAll(extractRestaurant(jsonResponse));
            } else {
                Log.d("Geofence activity", " fetchRestaurantData in else: ");
                break;
            }
        }
        return finalList;

    }

    private URL createUrl(String stringUrl) {
        Log.d("Geofence activity", " createUrl of query utils: ");
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    private String makeHttpRequest(URL url) throws IOException {

        Log.d("Geofence activity", " makeHttpRequest: ");
//        GeofenceActivity.countOfApiCalls++;
//        Log.d("loadInBackground", "countOfApiCalls: " + GeofenceActivity.countOfApiCalls);
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("user-key", "b0fa7064fb74f713bc335eda8392efae");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            DatabaseReference counterDatabaseReference = databaseReference.child("Number of API calls").getRef();
            counterDatabaseReference.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    if (mutableData.getValue() == null) {
                        mutableData.setValue(1);
                    } else {
                        mutableData.setValue((Long) mutableData.getValue() + 1);
                    }
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                    if (databaseError != null) {
                        System.out.println("Firebase counter increment failed.");
                    } else {
                        System.out.println("Firebase counter increment succeeded.");
                    }
                }
            });
            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the Restaurant JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    public ArrayList<Restaurant> extractRestaurant(String restaurantJason) {
        Log.d("Geofence activity", " extractRestaurant: ");
        Log.d("jason", "jason: " + restaurantJason);

        if (TextUtils.isEmpty(restaurantJason)) {
            return null;
        }

        ArrayList<Restaurant> restaurantArrayList = new ArrayList<>();

        try {
            JSONObject baseJasonResponse = new JSONObject(restaurantJason);
            JSONArray restaurantArray = baseJasonResponse.getJSONArray("restaurants");
            String results_found = baseJasonResponse.getString("results_found");
            String results_start = baseJasonResponse.getString("results_start");

            for (int i = 0; i < restaurantArray.length(); i++) {
                JSONObject newRestaurant = restaurantArray.getJSONObject(i);
                JSONObject restaurant = newRestaurant.getJSONObject("restaurant");
                JSONObject locationResults = restaurant.getJSONObject("location");

                if (!(locationResults.getString("latitude").contains("0.00"))) {

                    String id = restaurant.getString("id");
                    String name = restaurant.getString("name");
                    String resUrl = restaurant.getString("url");
                    String cuisines = restaurant.getString("cuisines");
                    String phoneNumber = null;
                    if (!(restaurant.isNull("phone_numbers"))) {
                        phoneNumber = restaurant.getString("phone_numbers");
                    } else
                        phoneNumber = null;
                    String averageCostForTwo = restaurant.getString("average_cost_for_two");
                    String priceRange = restaurant.getString("price_range");
                    String currency = restaurant.getString("currency");
                    String image_url = restaurant.getString("featured_image");
                    String photos_url = restaurant.getString("photos_url");
                    JSONObject userRating = restaurant.getJSONObject("user_rating");
                    String rating = userRating.getString("aggregate_rating");
                    String rating_color = userRating.getString("rating_color");
                    String ratingText = userRating.getString("rating_text");
                    String votes = userRating.getString("votes");

                    String address = locationResults.getString("address");
                    String addressLocality = locationResults.getString("locality");
                    if (addressLocality.contains(",")) {
                        if (addressLocality.contains(", "))
                            break;
                        else if (addressLocality.contains(","))
                            addressLocality = addressLocality.replace(",", ", ");
                    }
                    String addressCity = locationResults.getString("city");
                    String latitude = locationResults.getString("latitude");
                    String longitude = locationResults.getString("longitude");

                    Log.d("activity", "results_found" + results_found);

                    Restaurant restaurantClass = new Restaurant(results_found, id, name, resUrl, address, addressLocality, addressCity, averageCostForTwo, priceRange, currency, image_url, rating, rating_color, ratingText, cuisines, phoneNumber, latitude, longitude, photos_url, votes);
                    restaurantArrayList.add(restaurantClass);
                }
            }
            if (Integer.parseInt(results_found) > (Integer.parseInt(results_start) + 20)) {
                SharedPreferences.Editor ed = pref.edit();
                ed.putBoolean("call_api_again", true);
                ed.apply();
            } else {
                SharedPreferences.Editor ed = pref.edit();
                ed.putBoolean("call_api_again", false);
                ed.apply();
            }
        } catch (JSONException e) {
            Log.d("QueryUtils", "Jason: " + e);
        }

        return restaurantArrayList;
    }

    public ArrayList<String> createURLFromCuisine(Location location) {
//        Log.d("onLocationChanged", "countOfApiCalls: " + countOfApiCalls);
        Log.d("Geofence activity", " createURLFromCuisine: ");
        String finalSearchURL = "https://developers.zomato.com/api/v2.1/search?";
        String SEARCH_REQUEST_URL_RADIUS = "&radius=4000";
        String SEARCH_REQUEST_URL_CUISINE = "&cuisines=";
        String urlForRestaurants;
        ArrayList<String> apiCallURLs = new ArrayList<>();
        SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
        String tempCuisineData = pref.getString("cuisineId", null);
        String tempCuisineName = pref.getString("cuisineName", null);
        ArrayList<String> cuisineId = new ArrayList<>();
        cuisineName = new ArrayList<>();


        finalSearchURL = finalSearchURL.concat("lat=" + String.valueOf(location.getLatitude()));
        finalSearchURL = finalSearchURL.concat("&lon=" + String.valueOf(location.getLongitude()));
        finalSearchURL = finalSearchURL.concat(SEARCH_REQUEST_URL_RADIUS);
        if (tempCuisineData == null) {
            urlForRestaurants = finalSearchURL.concat("&start=00&count=100");
            apiCallURLs.add(0, urlForRestaurants);
            return apiCallURLs;

        } else {
            try {
                JSONArray jsonArray = new JSONArray(tempCuisineData);
                for (int i = 0; i < jsonArray.length(); i++) {
                    cuisineId.add(jsonArray.optString(i));
                }
                JSONArray jsonArray1 = new JSONArray(tempCuisineName);
                for (int i = 0; i < jsonArray1.length(); i++) {
                    cuisineName.add(jsonArray1.optString(i));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            finalSearchURL = finalSearchURL.concat(SEARCH_REQUEST_URL_CUISINE);

            for (int i = 0; i < cuisineId.size(); i++) {


                urlForRestaurants = finalSearchURL.concat(cuisineId.get(i));
                apiCallURLs.add(i, urlForRestaurants);
                Log.d("Geofence activity", "url: " + urlForRestaurants);
                Log.d("Geofence activity", "cuisineId.size()" + cuisineId.size());


            }
//            index=0;
//            urlForRestaurants = apiCallURLs.get(index);
            Log.d("Geofence activity", " createURLFromCuisine: ");
        }
        return apiCallURLs;

//        Log.d("onLocationChanged", "countOfApiCalls: " + countOfApiCalls);
//        createCityGeofence(locationForService);
    }

    public void createRestaurantGeofenceList() {
        Log.d("Geofence activity", " createRestaurantGeofenceList: ");
        String uniqueID;
        double latitude;
        double longitude;
        final int GEOFENCE_RADIUS_IN_METERS = 100;
        long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
                12 * 60 * 60 * 1000;
        for (Map.Entry<String, LatLng> entry : geoFenceHashList.entrySet()) {

            uniqueID = entry.getKey();
            latitude = entry.getValue().latitude;
            longitude = entry.getValue().longitude;

            GeofenceDetails geofenceDetails = new GeofenceDetails(latitude, longitude, uniqueID);
            geofenceDetailsArrayList.add(geofenceDetails);

            geofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(entry.getKey())

                    // Set the circular region of this geofence.
                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            GEOFENCE_RADIUS_IN_METERS
                    )

                    // Set the expiration duration of the geofence. This geofence gets automatically
                    // removed after this period of time.
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)

                    // Set the transition types of interest. Alerts are only generated for these
                    // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)

                    // Create the geofence.
                    .build());
        }


        geoFenceHashList = new HashMap<>();
        try {

//            if (!(googleApiClient.isConnected())) {
//                setGoogleApiClient();
//            }

            LocationServices.GeofencingApi.addGeofences(
                    googleApiClient,
                    // The GeofenceRequest object.
                    getGeofencingRequest(),
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    getGeofencePendingIntent()
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if (status.isSuccess()) {
                        Log.d("Geofence activity", " onResult from restaurant: " + status.isSuccess());

                    } else {
                        Log.d("Geofence activity", " onResult from restaurant: " + status.toString());
                    }
                }
            }); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Log.d("Geofence activity", " securityException from restaurant: " + securityException.toString());
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        Log.d("Geofence activity", " getGeofencingRequest: ");
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        Log.d("Geofence activity", " getGeofencePendingIntent: ");
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling addgeoFences()
        return PendingIntent.getService(this, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void createCityGeofence(Location location) {
        Log.d("Geofence activity", " createCityGeofence: ");
        Log.d("Geofence activity", " location.getLatitude(): " + location.getLatitude());
        Log.d("Geofence activity", " location.getLongitude: " + location.getLongitude());
        String CITY_GEOFENCE_ID = "city";
        Geofence cityGeofence = new Geofence.Builder()
                .setRequestId(CITY_GEOFENCE_ID)
                .setCircularRegion(location.getLatitude(), location.getLongitude(), 4000)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        GeofencingRequest cityGeofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT)
                .addGeofence(cityGeofence)
                .build();

        try {
//            if (!(googleApiClient.isConnected())) {
//                setGoogleApiClient();
//            }

            LocationServices.GeofencingApi.addGeofences(
                    googleApiClient,
                    // The GeofenceRequest object.
                    cityGeofencingRequest,
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    getCityGeofencePendingIntent()
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if (status.isSuccess()) {
                        // remove drawing
                        Log.d("Geofence activity", " creating city geofence successful ");
                    } else {
                        Log.e("Geofence activity", "creating city geofence failed: " + status.getStatusMessage());
                    }
                }
            }); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Log.d("Geofence activity", " securityException from city: " + securityException.toString());
        }

    }

    private PendingIntent getCityGeofencePendingIntent() {
        Log.d("Geofence activity", " getCityGeofencePendingIntent: ");
        Intent intent = new Intent(this, CityGeofenceTransitionsIntentService.class);
        SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = pref.edit();
        ed.putBoolean("city_geofence", true);
        ed.putBoolean("request_location", false);
        ed.apply();
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling addgeoFences()
        return PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void printGeoFenceList() {
        for (int i = 0; i < geofenceList.size(); i++) {
            Log.d("main activity ", "printGeoFenceList :" + geofenceList.get(i).toString());
        }

    }

    public void removeRestaurantList() {
        Log.d("Geofence activity", " removeRestaurantList: ");
        if (removeCuisineList.size() != 0) {
            databaseReference.child("Restaurants List").child(pref.getString("user_uid", null)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d("Geofence activity", " onDataChange: ");
                    Log.d("Geofence activity", " cuisineName: " + cuisineName.get(0));
                    Log.d("Geofence activity", " child: " + dataSnapshot.hasChild(cuisineName.get(0)));
                    Log.d("Geofence activity", " child count: " + dataSnapshot.child(cuisineName.get(0)).child(pref.getString("user_uid", null)).getChildrenCount());

// dataSnapshot.child(cuisineName.get(0)).getRef().removeValue();
                    for (int i = 0; i < removeCuisineList.size(); i++) {
                        for (DataSnapshot restaurantSnapshot : dataSnapshot.getChildren()) {

                            if (removeCuisineList.get(i).equals(restaurantSnapshot.getKey())) {
                                Log.d("Geofence activity", "removeCuisineList node names: " + removeCuisineList.get(i));
                                Log.d("Geofence activity", "restaurantSnapshot node names: " + restaurantSnapshot.getKey());
                                restaurantSnapshot.getRef().removeValue();

                            }
                        }
//                        Log.d("Geofence activity", " node names: " + restaurantSnapshot.getKey());
                        //                    restaurantSnapshot.getRef().removeValue();

                    }

                    Intent intent = new Intent("scenica.intent.action.FIREBASE_DATA_UPDATED");
                    Log.d("Geofence activity", " broadcast sent: ");
                    sendBroadcast(intent);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "onCancelled", databaseError.toException());
                }
            });
        } else {
            if (pref.getBoolean("activity_executed", false)) {
                Intent intent = new Intent("scenica.intent.action.FIREBASE_DATA_UPDATED");
                Log.d("Geofence activity", " broadcast sent from else: ");
                sendBroadcast(intent);
            }
        }
    }

    public void insertGeofenceIDinFirebase() {
        Log.d("Geofence activity", " insertGeofenceIDinFirebase: ");
        databaseReference.child("Geofence Details").child(pref.getString("user_uid", null)).setValue(geofenceDetailsArrayList);
    }

    public void deleteRestaurantGeofenceList(Intent intent, GoogleApiClient mGoogleApiClient) {
        Log.d("Geofence activity", " deleteRestaurantGeofenceList: ");
        ArrayList<String> removeGeofenceIDs = new ArrayList<>();
        ArrayList<GeofenceDetails> removeGeofenceDetails = intent.getParcelableArrayListExtra("remove_geofenceID");
        if (removeGeofenceDetails == null) {
            Log.d("Geofence activity", " error in removing geofence or first login: ");
        } else {
//            if (!(googleApiClient.isConnected())) {
//                setGoogleApiClient();
//            }
            for (int i = 0; i < removeGeofenceDetails.size(); i++) {
                removeGeofenceIDs.add(removeGeofenceDetails.get(i).getUniqueID());
            }
            Log.d("Geofence activity", " googleApiClient: " + mGoogleApiClient.isConnected());
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, removeGeofenceIDs).setResultCallback(new ResultCallback<Status>() {
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
        }


    }


    public void checkCuisinePresence(Location location) {
        ArrayList<String> cuisineNametemp = new ArrayList<>();
        Log.d("Geofence activity", "inside checkCuisinePresence: ");
        String CUISINE_URL = createURLforCuisineList(location);

        Uri baseUri = Uri.parse(CUISINE_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        ArrayList<Cuisine> cuisinesList = fetchCuisineData(uriBuilder.toString());
        ArrayList<String> cuisinesFetchedFromAPI = new ArrayList<>();
        for (int i = 0; i < cuisinesList.size(); i++) {
            cuisinesFetchedFromAPI.add(cuisinesList.get(i).getCuisineName());
        }

        SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
        String tempCuisineName = pref.getString("cuisineName", null);
        try {
            JSONArray jsonArray1 = new JSONArray(tempCuisineName);
            for (int i = 0; i < jsonArray1.length(); i++) {
                cuisineNametemp.add(jsonArray1.optString(i));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < cuisineNametemp.size(); i++) {
            boolean temp = false;
            for (int j = 0; j < cuisinesFetchedFromAPI.size(); j++) {
                if (cuisinesFetchedFromAPI.contains(cuisineNametemp.get(i)))
                    temp = true;
            }

            if (temp)
                break;
            else {
                sendNotification("Selected Cuisine not present in current City. Please select another Cuisine.");
            }
        }

    }


    public String createURLforCuisineList(Location location) {
        String cuisineSearchUrl = "https://developers.zomato.com/api/v2.1/cuisines?";
        cuisineSearchUrl = cuisineSearchUrl.concat("lat=" + location.getLatitude());
        cuisineSearchUrl = cuisineSearchUrl.concat("&");
        cuisineSearchUrl = cuisineSearchUrl.concat("lon=" + location.getLongitude());
        return cuisineSearchUrl;
    }


    private ArrayList<Cuisine> fetchCuisineData(String requestUrl) {
        URL url = createCuisineUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequestCuisine(url);
        } catch (IOException e) {
            Log.e(TAG, "Problem making the HTTP request.", e);
        }
        return extractCuisines(jsonResponse);

    }

    private URL createCuisineUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Problem building the URL ", e);
        }
        return url;
    }

    private String makeHttpRequestCuisine(URL url) throws IOException {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("user-key", "b0fa7064fb74f713bc335eda8392efae");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            Log.d("CuisineQuery activity", " apicall: ");

            DatabaseReference counterDatabaseReference = databaseReference.child("Number of API calls").getRef();
            counterDatabaseReference.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    if (mutableData.getValue() == null) {
                        mutableData.setValue(1);
                    } else {
                        mutableData.setValue((Long) mutableData.getValue() + 1);
                    }
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                    if (databaseError != null) {
                        System.out.println("Firebase counter increment failed.");
                    } else {
                        System.out.println("Firebase counter increment succeeded.");
                    }
                }
            });
            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStreamCuisine(inputStream);
            } else {
                Log.e(TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(TAG, "Problem retrieving the Restaurant JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private String readFromStreamCuisine(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private ArrayList<Cuisine> extractCuisines(String cuisineJason) {
        if (TextUtils.isEmpty(cuisineJason)) {
            return null;
        }

        ArrayList<Cuisine> cuisineList = new ArrayList<>();

        try {
            JSONObject baseJasonResponse = new JSONObject(cuisineJason);
            JSONArray cuisineArray = baseJasonResponse.getJSONArray("cuisines");


            for (int i = 0; i < cuisineArray.length(); i++) {
                JSONObject newCuisine = cuisineArray.getJSONObject(i);
                JSONObject cuisine = newCuisine.getJSONObject("cuisine");

                String cuisine_name = cuisine.getString("cuisine_name");
                String cuisineId = cuisine.getString("cuisine_id");

                Log.d("cuisine", "cuisine_name: " + cuisine_name);
                Log.d("cuisine", "cuisine_id: " + cuisineId);


                Cuisine newCuisineDetails = new Cuisine(cuisine_name, cuisineId);

                cuisineList.add(newCuisineDetails);

            }
        } catch (JSONException e) {
            Log.d("QueryUtils", "Jason: " + e);
        }

        return cuisineList;
    }

    private void sendNotification(String notificationDetails) {
        // Create an explicit content Intent that starts the main Activity.
        Log.d("main activity Geofence", " sendNotification: ");
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(GoogleSignInActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.firebase_lockup_400)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.drawable.firebase_lockup_400))
                .setColor(Color.RED)
                .setContentTitle(notificationDetails)
                .setContentText(getString(R.string.geofence_transition_notification_text))
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }


}

