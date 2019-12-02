package scenica.raj.shreamigo;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CuisinesList extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, LoaderManager.LoaderCallbacks<List<Cuisine>> {

    public final static String TAG = "CuisinesList";
    private static final int RESTAURANT_LOADER_ID = 1;
    private static final int REQ_PERMISSION = 940;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static String CUISINE_URL;
    CuisineAdapter cuisineAdapter;
    ArrayList<String> cuisineFromFirebase;
    ArrayList<String> cuisineIDfromFirebase;
    CheckBox checkBox;
    Intent SettingsIntent;
    UserPreferenceData userPreferenceData;
    GridView gridView;
    TextView emptyStateTextView;
    View loadingIndicator;
    Button save, cancel;
    LinearLayout linearLayout;
    LoaderManager loaderManager;
    private ArrayList<Cuisine> data = new ArrayList<>();
    private BroadcastReceiver broadcastReceiver;
    private String cuisineSearchUrl = "https://developers.zomato.com/api/v2.1/cuisines?";

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuisines_list);

        toolbar = (Toolbar) findViewById(R.id.tool_bar_cuisine_list);
        setSupportActionBar(toolbar);

        startGoogleApiClient();
        save = (Button) findViewById(R.id.selectBtn);
//        cancel =(Button) findViewById(R.id.cancelBtn);
        linearLayout = (LinearLayout) findViewById(R.id.linear_layout);
        checkBox = (CheckBox) findViewById(R.id.itemCheckBox);

        SettingsIntent = getIntent();
        if (SettingsIntent.getBooleanExtra("from_settings", false)) {
            SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
            SharedPreferences.Editor ed = pref.edit();
            ed.putBoolean("activity_executed", false);
            ed.apply();
            Log.d("numbering", "SettingsIntent: " + SettingsIntent.getAction());

        }
        Log.d("numbering", "onCreate");

        final SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = pref.edit();
        ed.putBoolean("request_location", true);
        ed.putBoolean("city_geofence", true);

        ed.apply();
        if (pref.getBoolean("activity_executed", false)) {
            ed.putBoolean("cuisine_updated", false);
            startActivity(new Intent(this, ResultByCuisine.class));
            ed.apply();
            finish();
        }
        extractCuisineFromFirebase();

        EditText editText = (EditText) findViewById(R.id.search_text);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cuisineAdapter.getFilter().filter(s.toString().toLowerCase(Locale.US));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        gridView = (GridView) findViewById(R.id.gridview);
        emptyStateTextView = (TextView) findViewById(R.id.cuisine_empty_view);
        final Intent fromCuisineListIntent = new Intent(this, ResultByCuisine.class);


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("numbering", "inside setOnClickListener");
                String name;
                String ID;
                JSONArray jsonArrayId = new JSONArray();
                JSONArray jsonArrayName = new JSONArray();
                int count = 0;
                ArrayList<UserPreferenceData> userPreferenceDataArray = new ArrayList<>();

                for (int i = 0; i < data.size(); i++) {
                    if (data.get(i).isSelected())

                    {
                        Log.d("numbering", "name from for loop: " + data.get(i).getCuisineName());
                        name = (data.get(i).getCuisineName());
                        ID = (data.get(i).getCuisineID());
                        jsonArrayId.put(data.get(i).getCuisineID());
                        jsonArrayName.put(data.get(i).getCuisineName());
                        count++;

                        userPreferenceData = new UserPreferenceData(name, ID);
                        userPreferenceDataArray.add(userPreferenceData);

                    }
                }

                if (userPreferenceDataArray.size() == 0) {
                    noCuisineSelectedDialog();
                } else {

                    if (userPreferenceDataArray.size() > 1) {
                        checkOnlyOneCuisineNotification();
                    } else {
//                userPreferenceData = new UserPreferenceData(name, ID);
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                        if (pref.getString("user_uid", null) != null) {
                            databaseReference.child("Cuisine Choices").child(pref.getString("user_uid", null)).setValue(userPreferenceDataArray);
                        } else
                            Toast.makeText(getBaseContext(), "firebase sync error", Toast.LENGTH_SHORT).show();

                        SharedPreferences.Editor ed = pref.edit();
                        ed.putBoolean("activity_executed", true);
                        Log.d("numbering", "count: " + count);
                        Log.d("numbering", "data.size(): " + data.size());
                        if (count == data.size()) {
                            Log.d("numbering", "inside if of SharedPreferences");
                            ed.putString("cuisineId", null);
                            ed.putString("cuisineName", null);
                        } else {
                            Log.d("numbering", "inside else of SharedPreferences");
                            ed.putString("cuisineId", jsonArrayId.toString());
                            ed.putString("cuisineName", jsonArrayName.toString());
                        }
                        ed.putBoolean("cuisine_updated", true);
                        ed.putBoolean("call_api_again", true);
                        ed.apply();
                        startActivity(fromCuisineListIntent);
                        finish();
                    }
                }
            }


        });

        loadingIndicator = findViewById(R.id.cuisine_loading_indicator);

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            emptyStateTextView.setText("Loading Data");
            // Get a reference to the LoaderManager, in order to interact with loaders.
            loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(RESTAURANT_LOADER_ID, null, this);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            loadingIndicator = findViewById(R.id.cuisine_loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            // Update empty state with no connection error message
            emptyStateTextView.setText(R.string.no_internet_connection);
        }


    }

    @Override
    public Loader<List<Cuisine>> onCreateLoader(int id, Bundle args) {
        if (CUISINE_URL == null) {
            return new CuisineLoader(this, null);
        } else {
            Uri baseUri = Uri.parse(CUISINE_URL);
            Uri.Builder uriBuilder = baseUri.buildUpon();
            Log.d("numbering", "onCreateLoader");

            return new CuisineLoader(this, uriBuilder.toString());
        }
    }

    @Override
    public void onLoadFinished(Loader<List<Cuisine>> loader, List<Cuisine> cuisineData) {

        Log.d("numbering", "onLoadFinished");


//        if(cuisineAdapter != null) {
//            cuisineAdapter.clear();
//        }
        if (cuisineData != null && !cuisineData.isEmpty()) {

            Log.d("numbering", "inside if of onLoadFinished");
            data.addAll((ArrayList) cuisineData);
            Log.d("numbering", "data.size(): " + data.size());
            cuisineAdapter = new CuisineAdapter(this, cuisineData);
            Log.d("numbering", "before gridView");
            gridView.setAdapter(cuisineAdapter);
//            delay();

        } else {
            Log.d("numbering", "inside else of onLoadFinished");
        }

    }

    @Override
    public void onLoaderReset(Loader<List<Cuisine>> loader) {

        Log.d("numbering", "onLoaderReset");

    }

//    @Override
//    public void updateNewLocation(Location location, GoogleApiClient googleApiClient) {
//
//        if (CUISINE_URL == null) {
//            cuisineSearchUrl = cuisineSearchUrl.concat("lat=" + location.getLatitude());
//            cuisineSearchUrl = cuisineSearchUrl.concat("&");
//            cuisineSearchUrl = cuisineSearchUrl.concat("lon=" + location.getLongitude());
//            CUISINE_URL = cuisineSearchUrl;
//
//            Log.d("numbering", " inside if updateNewLocation");
//            Log.d("numbering", "cuisineSearchUrl :" + cuisineSearchUrl);
//            loaderManager.restartLoader(RESTAURANT_LOADER_ID, null, this);
//        } else {
//            Log.d("numbering", " inside else updateNewLocation");
//        }
//
//    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("numbering", "onStart");
        googleApiClient.connect();
        IntentFilter intent = new IntentFilter("scenica.intent.action.DATA_FROM_FIREBASE");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("numbering", "SettingsIntent: " + intent.getAction());
                Log.d("numbering", "onReceive: " + intent.getBooleanExtra("cuisine_present", false));
                if (showCuisineFromFirebase(intent)) {
                    showCuisineListDialog();
                } else {
                    showCuisineListForFirstTimeLogin();
                }
            }
        };

        this.registerReceiver(broadcastReceiver, intent);
    }

    @Override
    protected void onResume() {
        Log.d("numbering", "onResume");
        super.onResume();

    }

    @Override
    protected void onStop() {
        Log.d("numbering", "onPause");
        super.onStop();
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }

        this.unregisterReceiver(broadcastReceiver);
    }

    public void startGoogleApiClient() {
        Log.d("numbering", "startGoogleApiClient");
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


    }

    public void startLocationServices() {
        Log.d("numbering", "startLocationServices");
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000);     // 10 seconds, in milliseconds

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Log.d("numbering", "Location services connected.");
        Log.d("numbering", "googleApiClient: " + googleApiClient.toString());
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        if (connectionResult.hasResolution() && this instanceof Activity) {
            try {
                Activity activity = this;
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(activity, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            /*
             * Thrown if Google Play services canceled the original
             * PendingIntent
             */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.d("numbering", "Location services connection failed with code " + connectionResult.getErrorCode());
        }

    }

    @Override
    public void onLocationChanged(Location location) {

        Log.d("numbering", "onLocationChanged.");
        if (location != null) {
            if (CUISINE_URL == null) {
                cuisineSearchUrl = cuisineSearchUrl.concat("lat=" + location.getLatitude());
                cuisineSearchUrl = cuisineSearchUrl.concat("&");
                cuisineSearchUrl = cuisineSearchUrl.concat("lon=" + location.getLongitude());
                CUISINE_URL = cuisineSearchUrl;

                Log.d("numbering", " inside if onLocationChanged");
                Log.d("numbering", "cuisineSearchUrl :" + cuisineSearchUrl);
                loaderManager.restartLoader(RESTAURANT_LOADER_ID, null, this);
            } else {
                Log.d("numbering", " inside else onLocationChanged");
            }
        }

    }

    public void startLocationUpdates() {
        Log.d("numbering", "startLocationUpdates()");

        if (checkPermission()) {
            Log.d("numbering", "googleApiClient: " + googleApiClient.isConnected());
            startLocationServices();
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        } else {
            Log.d("numbering", "googleApiClient: " + googleApiClient.toString());
            askPermission();
        }

    }

    private boolean checkPermission() {
        Log.d("numbering", "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    private void askPermission() {
        Log.d("numbering", "askPermission()");
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQ_PERMISSION
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("numbering", "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted

                    startLocationUpdates();

                } else {
                    // Permission denied
                    permissionsDenied();
                }
                break;
            }
        }
    }

    private void permissionsDenied() {
        Log.d("numbering", "permissionsDenied()");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                showMessageOKCancel("You need to allow locationForService permissions to run the application ",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                            REQ_PERMISSION);
                                }
                            }
                        });
                Log.w("numbering", "permissionsDenied()");
            }
            return;
        }
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void showCuisineListDialog() {
        Log.d("numbering", "showCuisineListDialog");
        final Intent fromCuisineListIntent = new Intent(this, ResultByCuisine.class);
        Log.d("numbering", "cuisineFromFirebase: " + cuisineFromFirebase.get(0));
        loadingIndicator.setVisibility(View.GONE);
        emptyStateTextView.setVisibility(View.GONE);
        final SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
        AlertDialog.Builder builder = new AlertDialog.Builder(CuisinesList.this);
        builder.setTitle(getString(R.string.dialog_title));

        builder.setMessage("Cuisine: " + cuisineFromFirebase.get(0) + "\n" + "\n" + getString(R.string.dialog_message));
        String positiveText = getString(R.string.positive_text);
        builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor ed = pref.edit();
                ed.putBoolean("activity_executed", false);
                ed.apply();
                setVisibility();
                dialog.dismiss();
            }
        });

        String negativeText = getString(R.string.negative_text);
        builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                JSONArray jsonArrayId = new JSONArray();
                JSONArray jsonArrayName = new JSONArray();
                SharedPreferences.Editor ed = pref.edit();
                ed.putBoolean("activity_executed", true);
                ed.putBoolean("cuisine_updated", true);// to trigger GPSService
                ed.putBoolean("call_api_again", false);
                for (int i = 0; i < cuisineFromFirebase.size(); i++) {
                    jsonArrayId.put(cuisineIDfromFirebase.get(i));
                    jsonArrayName.put(cuisineFromFirebase.get(i));
                }
                ed.putString("cuisineId", jsonArrayId.toString());
                ed.putString("cuisineName", jsonArrayName.toString());
                ed.apply();
                startActivity(fromCuisineListIntent);
                finish();
            }
        });

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        // display dialog
        dialog.show();
    }

    private boolean showCuisineFromFirebase(Intent intent) {
        Log.d("numbering", "showCuisineFromFirebase");
        if (intent.getBooleanExtra("cuisine_present", false)) {
            String userPreferenceData = intent.getStringExtra("user_preference_cuisine");
            String userPreferenceDataID = intent.getStringExtra("user_preference_cuisine_ID");
            String[] userPreferenceDataArrayList = userPreferenceData.split(",");
            String[] userPreferenceDataArrayListID = userPreferenceDataID.split(",");
            cuisineFromFirebase = new ArrayList<String>(Arrays.asList(userPreferenceDataArrayList));
            cuisineIDfromFirebase = new ArrayList<String>(Arrays.asList(userPreferenceDataArrayListID));
            return true;
        } else {

            return false;
        }
    }

    private void delay() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

            }
        }, 3000);
    }

    public void setVisibility() {
        linearLayout.setVisibility(View.VISIBLE);
        save.setVisibility(View.VISIBLE);
        loadingIndicator.setVisibility(View.GONE);
        emptyStateTextView.setVisibility(View.GONE);

//        cancel.setVisibility(View.VISIBLE);
    }

    public void checkOnlyOneCuisineNotification() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CuisinesList.this);
        builder.setTitle("Please select only one cuisine.");

        builder.setMessage("To enjoy notifications for more than 1 cuisine please switch to Pro version.");
        String positiveText = getString(R.string.positive_text);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int i = 0; i < data.size(); i++) {
                    if (data.get(i).isSelected())
                        data.get(i).setSelected(false);
                    Log.d("cuisineList", "selected: " + data.get(i).getCuisineName() + " " + data.get(i).isSelected());
                }
                setVisibility();
                cuisineAdapter.notifyDataSetChanged();
//                cuisineAdapter = new CuisineAdapter(CuisinesList.this, (List<Cuisine>)data);
//                Log.d("numbering", "before gridView");
//                gridView.setAdapter(cuisineAdapter);
                dialog.dismiss();

            }
        });

        builder.setCancelable(false);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        // display dialog
        dialog.show();
    }

    public void noCuisineSelectedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CuisinesList.this);
        builder.setTitle("Please select atleast One cuisine.");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

            }
        });

        builder.setCancelable(false);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        // display dialog
        dialog.show();
    }


    private void extractCuisineFromFirebase() {
        Log.d(TAG, "extractCuisineFromFirebase: ");
        final SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Cuisine Choices").child(pref.getString("user_uid", null)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<UserPreferenceData> userPreferenceDataArrayList = new ArrayList<UserPreferenceData>();
                Intent intent = new Intent("scenica.intent.action.DATA_FROM_FIREBASE");
                for (DataSnapshot restaurantSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "Cuisine names: " + restaurantSnapshot.getValue());
                    userPreferenceDataArrayList.add(restaurantSnapshot.getValue(UserPreferenceData.class));
                }

                if (userPreferenceDataArrayList.size() == 0) {
                    Log.d(TAG, "extractCuisineFromFirebase inside if : ");
                    intent.putExtra("cuisine_present", false);
                    sendBroadcast(intent);
                } else {
                    Log.d(TAG, "extractCuisineFromFirebase inside else: ");
                    StringBuilder cuisineFromFirebase = new StringBuilder();
                    StringBuilder cuisineIDfromFirebase = new StringBuilder();
                    for (int i = 0; i < userPreferenceDataArrayList.size(); i++) {
                        cuisineFromFirebase.append(userPreferenceDataArrayList.get(i).getSelectedCuisineName()).append(",");
                        cuisineIDfromFirebase.append(userPreferenceDataArrayList.get(i).getSelectedCuisineID()).append(",");

                    }

                    intent.putExtra("user_preference_cuisine", cuisineFromFirebase.toString());
                    intent.putExtra("user_preference_cuisine_ID", cuisineIDfromFirebase.toString());
                    intent.putExtra("cuisine_present", true);
                    sendBroadcast(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CuisinesList.this);
        builder.setTitle("Are you sure want to exit?");
        final SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
        String positiveText = getString(R.string.positive_text);
        builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (SettingsIntent.getBooleanExtra("from_settings", false)) {
                    AlertDialog.Builder newBuilder = new AlertDialog.Builder(CuisinesList.this);
                    newBuilder.setTitle("Your preference is set to following: ");
                    newBuilder.setMessage("Cuisine: " + cuisineFromFirebase.get(0));
                    newBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor ed = pref.edit();
                            ed.putBoolean("activity_executed", true);
                            ed.apply();
                            dialog.dismiss();
                            CuisinesList.this.finish();
                        }
                    });
                    newBuilder.setCancelable(false);
                    Dialog newDialog = newBuilder.create();
                    newDialog.setCanceledOnTouchOutside(false);
                    // display dialog
                    newDialog.show();
                } else {
                    SharedPreferences.Editor ed = pref.edit();
                    ed.putBoolean("activity_executed", false);
                    ed.apply();
                    CuisinesList.this.finish();
                }

            }
        });

        String negativeText = getString(R.string.negative_text);
        builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor ed = pref.edit();
                ed.putBoolean("activity_executed", false);
                ed.apply();
                dialog.dismiss();
            }
        });

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        // display dialog
        dialog.show();
    }

    public void showCuisineListForFirstTimeLogin() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CuisinesList.this);
        builder.setTitle("No cuisine found.");

        builder.setMessage("Please select cuisine to start getting notifications.");
        String positiveText = getString(R.string.positive_text);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
                SharedPreferences.Editor ed = pref.edit();
                ed.putBoolean("activity_executed", false);
                ed.apply();
                setVisibility();
                dialog.dismiss();

            }
        });

        builder.setCancelable(false);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        // display dialog
        dialog.show();
    }


}
