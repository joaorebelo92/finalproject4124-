package com.example.abhishekbansal.rockpaperscissors;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class Signup extends AppCompatActivity {

    private final String TAG = "ABHISHEK";

    private static final int RC_SIGN_IN = 123;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {

            Log.d(TAG, "Already signed in!");
            Toast.makeText(this,"User already logged in!", Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(), auth.getCurrentUser().getPhoneNumber(), Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(), auth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            Intent i = new Intent(this, MapsActivity.class);
            startActivity(i);
            }
            else {
            Log.d(TAG, "Not signed in!");

            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(Arrays.asList(
                                    new AuthUI.IdpConfig.EmailBuilder().build(),
                                    new AuthUI.IdpConfig.PhoneBuilder().build()))
                            .build(),
                    RC_SIGN_IN);
            }
        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {

            IdpResponse response = IdpResponse.fromResultIntent(data);


            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Intent i = new Intent(this, MapsActivity.class);
                startActivity(i);
            }

            else {

                if (response == null) {
                    Log.d(TAG, "User cancelled the signin process");

                }
                else {
                    Log.d(TAG,"an error occurred during login");
                    Log.d(TAG, response.getError().getMessage());
                }
            }
        }
    }


    public void login(View v)
    {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(Arrays.asList(
                                new AuthUI.IdpConfig.EmailBuilder().build(),
                                new AuthUI.IdpConfig.PhoneBuilder().build()))
                        .build(),
                RC_SIGN_IN);
    }

    public void backpressed(View view) {
        Log.d("Tejas", "Going back to Login Page");

        Intent i = new Intent(this, LoginScreen.class);
        startActivity(i);
    }
}
