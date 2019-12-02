package scenica.raj.shreamigo;

/**
 * Created by DELL on 12/17/2016.
 */

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lmoroney on 12/16/14.
 */
public class GeofenceTransitionsIntentService extends IntentService {
    protected static final String TAG = "geofence-transitions";
    List<Geofence> triggeringGeofences;
    ArrayList triggeringGeofencesIdsList;
    final static String notification_text = "Nearby Restaurants";
    public GeofenceTransitionsIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("main activity Geofence", " onHandleIntent: ");

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());
            Log.d(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        /// / Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            // Get the geofences that were triggered. A single event can trigger multiple geofences.
            triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );
            Log.d("main activity Geofence", " onHandleIntent: before sendNotification");
            // Send notification and log the transition details.

            Log.d("main activity Geofence", " onHandleIntent: after sendNotification");
//            Log.i(TAG, geofenceTransitionDetails);
        } else {
            // Log the error.
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
        }

    }

    private void getGeofenceTransitionDetails(
            Context context,
            int geofenceTransition,
            List<Geofence> triggeringGeofencesList) {
        Log.d("main activity Geofence", " getGeofenceTransitionDetails: ");
        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        triggeringGeofencesIdsList = new ArrayList();

        for (Geofence geofence : triggeringGeofencesList) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        extractRestaurantDetails(geofenceTransitionString);
//        triggeringGeofencesIdsList = findRestaurantName(triggeringGeofencesIdsList);
//        String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);
//
//        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

    private String getTransitionString(int transitionType) {
        Log.d("main activity Geofence", " getTransitionString: ");
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);
            default:
                return getString(R.string.unknown_geofence_transition);
        }
    }

    private void sendNotification(String notificationDetails) {
        // Create an explicit content Intent that starts the main Activity.
        Log.d("main activity Geofence", " sendNotification: ");
        Intent notificationIntent;

        notificationIntent = new Intent(getApplicationContext(), RestaurantDetails.class);
        String triggeringGeofencesIdsString = TextUtils.join(",", triggeringGeofencesIdsList);
        notificationIntent.putExtra("restaurant_id", triggeringGeofencesIdsString);


        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(ResultByCuisine.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Define the notification settings.
        builder.setSmallIcon(R.mipmap.ic_launcher)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher))
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

    public void extractRestaurantDetails(final String geofenceTransitionString) {

        final ArrayList<Restaurant> restaurantArrayList = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        final SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
        final DatabaseReference ref = databaseReference.child("Restaurants List").child(pref.getString("user_uid", null));

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshotChildren : dataSnapshot.getChildren()) {

                    dataSnapshotChildren.child(pref.getString("user_uid", null)).getRef().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d("res_details activity", "dataSnapshot: " + dataSnapshot.getValue());
                            for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                Restaurant restaurant = singleSnapshot.getValue(Restaurant.class);
                                Log.d("res_details activity", "restaurant: " + restaurant);
                                restaurantArrayList.add(restaurant);
                            }

                            Log.d("res_details activity", "restaurantArrayList: " + restaurantArrayList.size());
                            extractName(restaurantArrayList, geofenceTransitionString);


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

    public void extractName(ArrayList<Restaurant> restaurantArrayList, String geofenceTransitionString) {
        ArrayList<String> geofenceNameList = new ArrayList<>();
        for (int j = 0; j < triggeringGeofencesIdsList.size(); j++) {
            for (int i = 0; i < restaurantArrayList.size(); i++) {
                if (Integer.parseInt(restaurantArrayList.get(i).getId()) == Integer.parseInt(triggeringGeofencesIdsList.get(j).toString())) {
                    geofenceNameList.add(restaurantArrayList.get(i).getName());
                }
            }
        }
        String triggeringGeofencesNamesString = TextUtils.join(", ", geofenceNameList);
        sendNotification(notification_text + ": " + triggeringGeofencesNamesString);

    }

}

//    public ArrayList findRestaurantName(ArrayList triggeringGeofencesIdsList)
//    {
//
//        ArrayList geofenceList = new ArrayList();
//        for (int j = 0; j < triggeringGeofencesIdsList.size() ; j++) {
//            for (int i = 0; i < MainActivity.restaurantList.size(); i++) {
//                if (Integer.parseInt(MainActivity.restaurantList.get(i).getId()) == Integer.parseInt(triggeringGeofencesIdsList.get(j).toString())) {
//                    geofenceList.add(MainActivity.restaurantList.get(i).getName());
//                }
//            }
//        }
//        return  geofenceList;
//    }


