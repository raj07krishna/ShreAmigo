package scenica.raj.shreamigo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GoogleSignInActivity extends BaseActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;
    Intent intent;
    // [END declare_auth]
    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth_listener]
    // [START declare_auth_listener]
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;
    //    private TextView mStatusTextView;
//    private TextView mDetailTextView;
    private FrameLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        intent = new Intent(GoogleSignInActivity.this, CuisinesList.class);

        if (mAuth.getCurrentUser() != null) {
            Log.d(TAG, "getCurrentUser: " + mAuth.getCurrentUser());
            SharedPreferences pref = getSharedPreferences("ActivityPREF", MODE_PRIVATE);
            SharedPreferences.Editor ed = pref.edit();
            ed.putBoolean("signin_executed", true);
            ed.apply();
            startActivity(intent);
            finish();
        }
        // Views
//        mStatusTextView = (TextView) findViewById(R.id.status);
//        mDetailTextView = (TextView) findViewById(R.id.detail);
        linearLayout = (FrameLayout) findViewById(R.id.main_layout);
        linearLayout.setBackgroundResource(R.drawable.final_one);
        Drawable drawable = linearLayout.getBackground();
        drawable.setAlpha(90);

        // Button listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);
//        findViewById(R.id.sign_out_button).setOnClickListener(this);
//        findViewById(R.id.disconnect_button).setOnClickListener(this);
        SignInButton button = (SignInButton) findViewById(R.id.sign_in_button);
        button.setStyle(SignInButton.SIZE_WIDE, SignInButton.COLOR_DARK);

        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.requestIdToken))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        // [START auth_state_listener]
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    createUser(user);
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // [START_EXCLUDE]
                updateUI(user);
                // [END_EXCLUDE]
            }
        };
        // [END auth_state_listener]
    }

    // [START on_start_add_listener]
    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    // [END on_start_add_listener]

    // [START on_stop_remove_listener]
    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    // [END on_stop_remove_listener]

    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }
        }
    }
    // [END onactivityresult]

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(GoogleSignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END auth_with_google]

    // [START signin]
    private void signIn() {
        Log.d(TAG, "signIn");
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signin]

    private void signOut() {
        Log.d(TAG, "signOut");
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        updateUI(null);
                    }
                });
    }

    private void revokeAccess() {
        Log.d(TAG, "revokeAccess");
        // Firebase sign out
        mAuth.signOut();

        // Google revoke access
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        Log.d(TAG, "updateUI");
        hideProgressDialog();
        if (user != null) {
//            mStatusTextView.setText(getString(R.string.google_status_fmt, user.getEmail()));
//            mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));
//
//            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
//            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
            Toast.makeText(this, "Logged in as " + user.getEmail(), Toast.LENGTH_LONG).show();
            SharedPreferences pref = getSharedPreferences("ActivityPREF", MODE_PRIVATE);
            SharedPreferences.Editor ed = pref.edit();
            ed.putString("user_uid", user.getUid());
            ed.apply();
//            extractCuisineFromFirebase();
            startActivity(intent);
            finishAffinity();

        } else {
//            mStatusTextView.setText(R.string.signed_out);
//            mDetailTextView.setText(null);

            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
//            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick");
        int i = v.getId();
        if (i == R.id.sign_in_button) {
            signIn();
        }
// else if (i == R.id.sign_out_button) {
//            signOut();
//        } else if (i == R.id.disconnect_button) {
//            revokeAccess();
//        }
    }

    public void createUser(FirebaseUser user) {
//        extractCuisineFromFirebase(user);
        if (mAuth.getCurrentUser() != null) {
//            final Firebase ref = new Firebase("https://ShreAmigo-c3741.firebaseio.com/");
            Log.d(TAG, "createUser");
            UserData userData = new UserData();
            userData.setEmail(user.getEmail());
            userData.setName(user.getDisplayName());
//        userData.setPhotoUri(user.getPhotoUrl());
            userData.setTokenID(user.getToken(true).toString());
            userData.setUID(user.getUid());
            DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference();
            Log.d("GoogleSignIn activity", "mDatabaseReference: " + FirebaseDatabase.getInstance().getReference().toString());
            mDatabaseReference.child("Users").child(userData.getUID()).setValue(userData);
        }

    }

    private void extractCuisineFromFirebase(FirebaseUser user) {
        Log.d(TAG, "extractCuisineFromFirebase: ");
        final SharedPreferences pref = getSharedPreferences("ActivityPREF", MODE_PRIVATE);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Cuisine Choices").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
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
}
