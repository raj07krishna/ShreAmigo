package scenica.raj.shreamigo;

import android.text.TextUtils;
import android.util.Log;

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
 * Created by DELL on 12/3/2016.
 */

public final class QueryUtils {

    //private static final String SAMPLE_JASON_RESPONSE = "{\n  \"results_found\": 2,\n  \"results_start\": 0,\n  \"results_shown\": 2,\n  \"restaurants\": [\n    {\n      \"restaurant\": {\n        \"R\": {\n          \"res_id\": 16761628\n        },\n        \"apikey\": \"b0fa7064fb74f713bc335eda8392efae\",\n        \"id\": \"16761628\",\n        \"name\": \"Cafe Angelique\",\n        \"url\": \"https://www.zomato.com/new-york-city/cafe-angelique-greenwich-village?utm_source=api_basic_user&utm_medium=api&utm_campaign=v2.1\",\n        \"locationForService\": {\n          \"address\": \"68 Bleecker Street, New York, NY 10012\",\n          \"locality\": \"Greenwich Village\",\n          \"city\": \"New York City\",\n          \"city_id\": 280,\n          \"latitude\": \"40.7262080000\",\n          \"longitude\": \"-73.9955880000\",\n          \"zipcode\": \"10012\",\n          \"country_id\": 216\n        },\n        \"cuisines\": \"Cafe, Desserts\",\n        \"average_cost_for_two\": 40,\n        \"price_range\": 3,\n        \"currency\": \"$\",\n        \"offers\": [],\n        \"thumb\": \"https://b.zmtcdn.com/data/pictures/8/16761628/021179cf7b3d03c17f22a6d8f37b1050_featured_v2.png\",\n        \"user_rating\": {\n          \"aggregate_rating\": \"3.5\",\n          \"rating_text\": \"Good\",\n          \"rating_color\": \"9ACD32\",\n          \"votes\": \"44\"\n        },\n        \"photos_url\": \"https://www.zomato.com/new-york-city/cafe-angelique-greenwich-village/photos?utm_source=api_basic_user&utm_medium=api&utm_campaign=v2.1#tabtop\",\n        \"menu_url\": \"https://www.zomato.com/new-york-city/cafe-angelique-greenwich-village/menu?utm_source=api_basic_user&utm_medium=api&utm_campaign=v2.1&openSwipeBox=menu&showMinimal=1#tabtop\",\n        \"featured_image\": \"https://b.zmtcdn.com/data/pictures/8/16761628/021179cf7b3d03c17f22a6d8f37b1050_featured_v2.png\",\n        \"has_online_delivery\": 0,\n        \"is_delivering_now\": 0,\n        \"deeplink\": \"zomato://restaurant/16761628\",\n        \"has_table_booking\": 0,\n        \"events_url\": \"https://www.zomato.com/new-york-city/cafe-angelique-greenwich-village/events#tabtop?utm_source=api_basic_user&utm_medium=api&utm_campaign=v2.1\",\n        \"establishment_types\": []\n      }\n    },\n    {\n      \"restaurant\": {\n        \"R\": {\n          \"res_id\": 18076250\n        },\n        \"apikey\": \"b0fa7064fb74f713bc335eda8392efae\",\n        \"id\": \"18076250\",\n        \"name\": \"Cafe Angelique Express\",\n        \"url\": \"https://www.zomato.com/new-york-city/cafe-angelique-express-soho?utm_source=api_basic_user&utm_medium=api&utm_campaign=v2.1\",\n        \"locationForService\": {\n          \"address\": \"575 Broadway, New York, NY 10012\",\n          \"locality\": \"Soho\",\n          \"city\": \"New York City\",\n          \"city_id\": 280,\n          \"latitude\": \"40.7244400000\",\n          \"longitude\": \"-73.9976770000\",\n          \"zipcode\": \"10012\",\n          \"country_id\": 216\n        },\n        \"cuisines\": \"Cafe\",\n        \"average_cost_for_two\": 15,\n        \"price_range\": 1,\n        \"currency\": \"$\",\n        \"offers\": [],\n        \"thumb\": \"\",\n        \"user_rating\": {\n          \"aggregate_rating\": \"0\",\n          \"rating_text\": \"Not rated\",\n          \"rating_color\": \"CBCBC8\",\n          \"votes\": \"0\"\n        },\n        \"photos_url\": \"https://www.zomato.com/new-york-city/cafe-angelique-express-soho/photos?utm_source=api_basic_user&utm_medium=api&utm_campaign=v2.1#tabtop\",\n        \"menu_url\": \"https://www.zomato.com/new-york-city/cafe-angelique-express-soho/menu?utm_source=api_basic_user&utm_medium=api&utm_campaign=v2.1&openSwipeBox=menu&showMinimal=1#tabtop\",\n        \"featured_image\": \"\",\n        \"has_online_delivery\": 0,\n        \"is_delivering_now\": 0,\n        \"deeplink\": \"zomato://restaurant/18076250\",\n        \"has_table_booking\": 0,\n        \"events_url\": \"https://www.zomato.com/new-york-city/cafe-angelique-express-soho/events#tabtop?utm_source=api_basic_user&utm_medium=api&utm_campaign=v2.1\",\n        \"establishment_types\": []\n      }\n    }\n  ]\n}";
    private static final String LOG_TAG = "statement";

    private QueryUtils() {
        /* constructor*/
    }

    public static List<Restaurant> fetchRestaurantData(String requestUrl) {
        Log.d("main activity", " fetchRestaurantData: ");
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }
        Log.d("main activity", " fetchRestaurantData before extractRestaurant : ");
        List<Restaurant> restaurants = extractRestaurant(jsonResponse);
        return restaurants;
    }

    private static URL createUrl(String stringUrl) {
        Log.d("main activity", " createUrl of query utils: ");
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        Log.d("main activity", " makeHttpRequest: ");
        MainActivity.countOfApiCalls++;
        Log.d("loadInBackground", "countOfApiCalls: " + MainActivity.countOfApiCalls);
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
        Log.d("main activity", " readFromStream: ");
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


    public static ArrayList<Restaurant> extractRestaurant(String restaurantJason) {
        Log.d("main activity", " extractRestaurant: ");
        Log.d("jason", "jason: " + restaurantJason);

        if (TextUtils.isEmpty(restaurantJason)) {
            return null;
        }

        ArrayList<Restaurant> restaurantArrayList = new ArrayList<>();

        try {
            JSONObject baseJasonResponse = new JSONObject(restaurantJason);
            JSONArray restaurantArray = baseJasonResponse.getJSONArray("restaurants");

            for (int i = 0; i < restaurantArray.length(); i++) {
                JSONObject newRestaurant = restaurantArray.getJSONObject(i);
                JSONObject restaurant = newRestaurant.getJSONObject("restaurant");
                JSONObject locationResults = restaurant.getJSONObject("locationForService");

                if (!(locationResults.getString("latitude").contains("0.00"))) {
                    String results_found = baseJasonResponse.getString("results_found");
                    String id = restaurant.getString("id");
                    String name = restaurant.getString("name");
                    String resUrl = restaurant.getString("url");
                    String cuisines = restaurant.getString("cuisines");
                    String phoneNumber = null;
                    if (!restaurant.isNull("phone_numbers")) {
                        phoneNumber = restaurant.getString("phone_numbers");
                    }
                    String averageCostForTwo = restaurant.getString("average_cost_for_two");
                    String priceRange = restaurant.getString("price_range");
                    String currency = restaurant.getString("currency");
                    String image_url = restaurant.getString("photos_url");
                    String photos_url = restaurant.getString("photos_url");
                    JSONObject userRating = restaurant.getJSONObject("user_rating");
                    String rating = userRating.getString("aggregate_rating");
                    String rating_color = userRating.getString("rating_color");
                    String ratingText = userRating.getString("rating_text");
                    String votes = userRating.getString("votes");

                    String address = locationResults.getString("address");
                    String addressLocality = locationResults.getString("locality");
                    String addressCity = locationResults.getString("city");
                    String latitude = locationResults.getString("latitude");
                    String longitude = locationResults.getString("longitude");

                    Log.d("activity", "results_found" + results_found);

                    Restaurant restaurantClass = new Restaurant(results_found, id, name, resUrl, address, addressLocality, addressCity, averageCostForTwo, priceRange, currency, image_url, rating, rating_color, ratingText, cuisines, phoneNumber, latitude, longitude, photos_url, votes);
                    restaurantArrayList.add(restaurantClass);
                }
            }
        } catch (JSONException e) {
            Log.d("QueryUtils", "Jason: " + e);
        }

        return restaurantArrayList;
    }
}