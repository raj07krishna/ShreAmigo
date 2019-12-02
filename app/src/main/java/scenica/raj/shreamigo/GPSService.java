package scenica.raj.shreamigo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by DELL on 1/11/2017.
 */


public class GPSService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    private static final String LOGSERVICE = "GPSService";
    ArrayList<String> oldCuisineList;
    Intent intent;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        buildGoogleApiClient();
        Log.i(LOGSERVICE, "onCreate");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOGSERVICE, "onStartCommand");
        this.intent = new Intent(this, RestaurantGeofenceService.class);

        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
            Log.i(LOGSERVICE, "inside if onStartCommand");
        }

        return START_STICKY;
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.i(LOGSERVICE, "onConnected" + bundle);

        startLocationUpdate();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient = null;
        Log.i(LOGSERVICE, "onConnectionSuspended " + i);

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(LOGSERVICE, "onLocationChanged");
        if (location != null) {
            Log.i(LOGSERVICE, " inside onLocationChanged");
            removeRestaurantList(location);
        }
//        LatLng mLocation = (new LatLng(location.getLatitude(), location.getLongitude()));
//        EventBus.getDefault().post(mLocation);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnect();
        Log.i(LOGSERVICE, "onDestroy - Estou sendo destruido ");

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(LOGSERVICE, "onBind");
        return null;
    }

    @Override
    public void onConnectionFailed(@Nullable ConnectionResult connectionResult) {
        Log.i(LOGSERVICE, "onConnectionFailed ");

    }

    private void initLocationRequest() {
        Log.i(LOGSERVICE, "initLocationRequest");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    private void startLocationUpdate() {
        Log.i(LOGSERVICE, "startLocationUpdate");
        initLocationRequest();

        if (checkPermission()) {
            Log.i(LOGSERVICE, "inside if of startLocationUpdate");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    private void stopLocationUpdate() {
        Log.i(LOGSERVICE, "stopLocationUpdate");

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

    }

    protected synchronized void buildGoogleApiClient() {
        Log.i(LOGSERVICE, "buildGoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }

    private boolean checkPermission() {
        Log.i(LOGSERVICE, "checkPermission");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    public void disconnect() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    public void removeRestaurantList(final Location location) {
        Log.d(LOGSERVICE, " removeRestaurantList: ");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        final SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
        oldCuisineList = new ArrayList<>();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(LOGSERVICE, " onDataChange: ");

                DataSnapshot newDataSnapshot = dataSnapshot.child("Restaurants List").child(pref.getString("user_uid", null));
                for (DataSnapshot restaurantSnapshot : newDataSnapshot.getChildren()) {
                    Log.d(LOGSERVICE, " node names: " + restaurantSnapshot.getKey());
                    oldCuisineList.add(restaurantSnapshot.getKey());

                }

                DataSnapshot geofenceSnapshot = dataSnapshot.child("Geofence Details").child(pref.getString("user_uid", null));
                ArrayList<GeofenceDetails> removeGeofenceDetails = new ArrayList<>();

                for (DataSnapshot restaurantSnapshot : geofenceSnapshot.getChildren()) {
                    Log.d(LOGSERVICE, "removeGeofenceId node names: " + restaurantSnapshot.getValue());
                    removeGeofenceDetails.add(restaurantSnapshot.getValue(GeofenceDetails.class));
                }

                intent.putExtra("oldCuisineList", oldCuisineList);
                Log.d(LOGSERVICE, "oldCuisineList: " + oldCuisineList.size());
                intent.putExtra("location", location);
                if (!(geofenceSnapshot.hasChildren()))
                    removeGeofenceDetails = null;
                intent.putExtra("remove_geofenceID", removeGeofenceDetails);
                onDestroy();
                startService(intent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(LOGSERVICE, "onCancelled", databaseError.toException());
            }
        });
    }

}


