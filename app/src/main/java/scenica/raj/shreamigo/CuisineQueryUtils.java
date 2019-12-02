package scenica.raj.shreamigo;

import android.text.TextUtils;
import android.util.Log;

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
import java.util.List;

/**
 * Created by DELL on 12/4/2016.
 */

public final class CuisineQueryUtils {

    private static final String LOG_TAG = "cuisine statement";


    public CuisineQueryUtils() {
    }

    public static List<Cuisine> fetchCuisineData(String requestUrl) {
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }
        List<Cuisine> cuisines = extractCuisines(jsonResponse);
        return cuisines;
    }

    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
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

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
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


    public static ArrayList<Cuisine> extractCuisines(String cuisineJason) {
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
