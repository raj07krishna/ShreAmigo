package scenica.raj.shreamigo;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Splash extends AppCompatActivity {

    public final static String TAG = "Splash";
    private static final int REQ_PERMISSION = 940;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ArrayList<ImageView> imageViewArrayList = new ArrayList<>();

        final FrameLayout main = (FrameLayout) findViewById(R.id.activity_spash);
        int images[] = {R.drawable.ic_local_dining_black_48dp, R.drawable.ic_local_bar_black_48dp, R.drawable.ic_local_cafe_black_48dp, R.drawable.ic_restaurant_black_48dp, R.drawable.ic_room_service_black_48dp, R.drawable.ic_cake_black_48dp
                , R.drawable.ic_local_bar_black_48dp, R.drawable.ic_restaurant_black_48dp, R.drawable.ic_cake_black_48dp, R.drawable.ic_local_dining_black_48dp, R.drawable.ic_local_cafe_black_48dp, R.drawable.ic_room_service_black_48dp};

        int numViews = 12;
        float alpha = 0;
        for (int i = 0; i < numViews; i++) {
            // Create some quick TextViews that can be placed.
            ImageView v = new ImageView(this);
            // Set a text and center it in each view.
            v.setImageResource(images[i]);
            imageViewArrayList.add(v);
//            v.setForegroundGravity(Gravity.CENTER);
//            v.setBackgroundColor(0xffff0000);
            // Force the views to a nice size (150x100 px) that fits my display.
            // This should of course be done in a display size independent way.
            int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 75, getResources().getDisplayMetrics());
            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width, height);
            // Place all views in the center of the layout. We'll transform them
            // away from there in the code below.
            lp.gravity = Gravity.CENTER;
            // Set layout params on view.
            v.setLayoutParams(lp);

            // Calculate the angle of the current view. Adjust by 90 degrees to
            // get View 0 at the top. We need the angle in degrees and radians.
            float angleDeg = i * 360.0f / numViews - 90.0f;
            float angleRad = (float) (angleDeg * Math.PI / 180.0f);
            // Calculate the position of the view, offset from center (300 px from
            // center). Again, this should be done in a display size independent way.
            int offset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 130, getResources().getDisplayMetrics());
            v.setTranslationX(offset * (float) Math.cos(angleRad));
            v.setTranslationY(offset * (float) Math.sin(angleRad));
            // Set the rotation of the view.
//            v.setRotation(angleDeg + 90.0f);
//            alpha = ((float) 0.08*i);
//            v.setAlpha(alpha);
            main.addView(v);

        }

//        animate(imageViewArrayList, 0, true);
//        animate(imageViewArrayList, 11, true);

        if (!(isAirplaneModeOn(this))) {
            if (checkGPS(this)) {
                if (checkPermission())
                    new HTTPAsyncTask().execute("");
                else
                    askPermission();
            } else {
                enableGPSDialog(this);
            }
        } else {
            airplaneModeDialog();
        }
    }

    private boolean checkPermission() {
        Log.d("numbering", "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    private void askPermission() {
        Log.d("numbering", "askPermission()");
        ActivityCompat.requestPermissions(
                this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
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

                    new HTTPAsyncTask().execute("");

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
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                showMessageOKCancel("You need to allow locationForService permissions to run the application ",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
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

    private void animate(final ArrayList<ImageView> imageViewArrayList, final int imageIndex, final boolean forever) {

        //imageView <-- The View which displays the images
        //images[] <-- Holds R references to the images to display
        //imageIndex <-- index of the first image to show in images[]
        //forever <-- If equals true then after the last image it starts all over again with the first image resulting in an infinite loop. You have been warned.

        int fadeInDuration = 200; // Configure time values here
        int timeBetween = 10;
        int fadeOutDuration = 200;

//        imageView.setVisibility(View.INVISIBLE);    //Visible or invisible by default - this will apply when the animation ends
//        imageView.setImageResource(images[imageIndex]);

        Animation fadeIn = new AlphaAnimation(0.08f * imageIndex, 0.08f * (imageViewArrayList.size() - imageIndex));
        fadeIn.setInterpolator(new DecelerateInterpolator()); // add this
        fadeIn.setDuration(fadeInDuration);

        Animation fadeOut = new AlphaAnimation(0.08f * (imageViewArrayList.size() - imageIndex), 0.08f * imageIndex);
        fadeOut.setInterpolator(new AccelerateInterpolator()); // and this
        fadeOut.setStartOffset(fadeInDuration + timeBetween);
        fadeOut.setDuration(fadeOutDuration);

        AnimationSet animation = new AnimationSet(false); // change to false
        animation.addAnimation(fadeIn);
        animation.addAnimation(fadeOut);
        animation.setRepeatCount(1);
        imageViewArrayList.get(imageIndex).setAnimation(animation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                if (imageViewArrayList.size() - 1 > imageIndex) {
                    animate(imageViewArrayList, imageIndex + 1, forever); //Calls itself until it gets to the end of the array
                } else {
                    if (forever == true) {
                        animate(imageViewArrayList, 0, forever);  //Calls itself to start the animation all over again in a loop if forever = true
                    }
                }
            }

            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
            }

            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
            }
        });
    }

    public boolean checkGPS(Context context) {
        LocationManager locationManager = null;
        boolean gps_enabled = false, network_enabled = false;

        if (locationManager == null)
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            Log.d(TAG, "GPS enabled : " + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
        } catch (Exception ex) {
            Log.d(TAG, "GPS enabled exception: " + ex.getMessage());
        }

//        try {
//            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//        } catch (Exception ex) {
//            Log.d(TAG, "GPS enabled exception: " + ex.getMessage());
//        }

        return gps_enabled;

    }


    public void enableGPSDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Splash.this);
        builder.setTitle("GPS is not enabled.");

        builder.setMessage("Click OK to go to settings.");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(myIntent);
                dialog.dismiss();
                finish();

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                finish();
            }
        });
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        // display dialog
        dialog.show();
    }


    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public boolean isAirplaneModeOn(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }


    }


    public void airplaneModeDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(Splash.this);
        builder.setTitle("Airplane Mode is on");

        builder.setMessage("Please disable Airplane mode.");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        builder.setCancelable(false);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        // display dialog
        dialog.show();
    }

    public String hasInternetAccess(Context context) {
        if (isNetworkAvailable(context)) {
            Log.d(TAG, "No network available!" + isNetworkAvailable(context));
            try

            {
                Log.e(TAG, "run");
                HttpURLConnection urlc = (HttpURLConnection)
                        (new URL("http://clients3.google.com/generate_204")
                                .openConnection());
                urlc.setRequestProperty("User-Agent", "Android");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                if ((urlc.getResponseCode() == 204 && urlc.getContentLength() == 0)) {
                    Log.e(TAG, "Internet Working: " + "true");
                } else
                    Log.e(TAG, "Internet Working: " + "false");
//                        return (urlc.getResponseCode() == 204 &&
//                                urlc.getContentLength() == 0);
                return ("A");
            } catch (IOException e) {
                Log.e(TAG, "Error checking internet connection", e);
//                internetConnectionErrorDialog();
                return ("B");
            }
        } else {
            Log.d(TAG, "No network available!");
//            noNetworkDialog();
            return ("C");
        }

    }

    private boolean isNetworkAvailable(Context context) {
        Log.e(TAG, "isNetworkAvailable");
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void internetConnectionErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Splash.this);
        builder.setTitle("Error while connecting to internet");

        builder.setMessage("Please try again later");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        builder.setCancelable(false);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        // display dialog
        dialog.show();
    }

    public void noNetworkDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Splash.this);
        builder.setTitle("No working Internet connection found.");

        builder.setMessage("Please try again later");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        builder.setCancelable(false);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        // display dialog
        dialog.show();
    }

    public void errorDialog(String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Splash.this);
        builder.setTitle(text);

        builder.setMessage("Please try again later");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        builder.setCancelable(false);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        // display dialog
        dialog.show();
    }

    private void delay() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

            }
        }, 30000);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
//        dialog.dismiss();
        finishAffinity();
    }

    private class HTTPAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            return hasInternetAccess(Splash.this);
        }

        @Override
        protected void onPostExecute(String s) {
            switch (s) {
                case "A":
//                    delay();
                    Intent intent = new Intent(Splash.this, GoogleSignInActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case "B":
                    errorDialog("Error checking internet connection");
                    break;
                case "C":
                    errorDialog("No network available!");
                    break;
            }
        }
    }
}

