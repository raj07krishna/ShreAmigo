package scenica.raj.shreamigo;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

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

/**
 * Created by DELL on 1/15/2017.
 */

public class CuisineIntentService extends IntentService implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    protected static final String TAG = "CuisineIntent activity";

    GoogleApiClient googleApiClient;
    Location locationFromService;


    public CuisineIntentService() {
        super(TAG);
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

        locationFromService = intent.getParcelableExtra("location");
        String CUISINE_URL = createURLforCuisineList(locationFromService);

        Uri baseUri = Uri.parse(CUISINE_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        ArrayList<Cuisine> cuisines = fetchCuisineData(uriBuilder.toString());

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onDestroy() {

        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
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
}
