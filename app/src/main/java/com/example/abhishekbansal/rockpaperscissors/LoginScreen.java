package com.example.abhishekbansal.rockpaperscissors;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class LoginScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
    }

    public void loginpressed(View view) {
        Log.d("Tejas", "Going to Signup Page");

        Intent i = new Intent(this, Signup.class);
        startActivity(i);
    }
}
