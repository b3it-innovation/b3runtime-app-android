package com.b3.development.b3runtime.ui.signin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.ui.home.HomeActivity;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class SignInActivity extends AppCompatActivity {

    public static final String TAG = SignInActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 123;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mFirebaseAuth = FirebaseAuth.getInstance();

        mAuthStateListener = firebaseAuth -> {
            //Try getting logged in user
            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user != null) {
                //logged in
                startHomeActivity();
            } else {
                //not logged in, create sign in intent
                createSignInIntent();
            }
        };
    }

    private void createSignInIntent() {
        //creates login providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        //create and launch intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false)
                        .setLogo(R.drawable.b3logo_yellow_signin)
                        .setTheme(R.style.SignInTheme)
                        .build(),
                RC_SIGN_IN);
    }

    private void startHomeActivity() {
        //send intent to transfer to HomeActivity
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                //successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null && user.getDisplayName() != null) {
                    Toast.makeText(SignInActivity.this, "Welcome " + user.getDisplayName()
                            + ".", Toast.LENGTH_SHORT).show();
                }
            } else {
                //sign in failed
                if (response == null) {
                    //user canceled login
                    Log.d(TAG, "User canceled login");
                    finish();
                } else {
                    //some error happened
                    if (response.getError() != null) {
                        Log.d(TAG, "Error code: " + response.getError().getErrorCode());
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

}
