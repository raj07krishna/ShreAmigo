package scenica.raj.shreamigo;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.firebase.client.Firebase;
import com.google.android.gms.common.api.GoogleApiClient;


/**
 * Created by DELL on 1/10/2017.
 */

public class ShreAmigoApplication extends Application {

    public static SharedPreferences preferences;
    public static GoogleApiClient mGoogleApiClient;
    public Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = getApplicationContext();

//        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
//                .addConnectionCallbacks(mContext)
//                .addOnConnectionFailedListener(mContext)
//                .addApi(LocationServices.API)
//                .build();
        preferences = getSharedPreferences("ActivityPREF", MODE_PRIVATE);
        Firebase.setAndroidContext(this);
    }
}
