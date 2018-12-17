package com.example.abhishekbansal.rockpaperscissors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.squareup.seismic.ShakeDetector;

public class ShakeLogic extends AppCompatActivity implements ShakeDetector.Listener {

    // global variable to track if light is on/off
    boolean lightOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shake_logic);

        // 1. create a bunch of nonsense variables
        // that are used to detect when the phone is shaking
        // (setup your phone to deal with shakes)
        SensorManager manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        ShakeDetector detector = new ShakeDetector(this);
        detector.start(manager);
    }


    // this a mandatory function
    // it's required by the ShakeDetector.Listener class
    @Override public void hearShake() {

        // 2. this function AUTOMAGICALLY gets called
        // every time you shake the phone

        // print out a message when the phone shakes
        Log.d("JENELLE", "phone is shaking!!!!");
        Toast.makeText(this, "PHONE IS SHAKING!!!!", Toast.LENGTH_SHORT).show();

        // turn on flashlight when phone shakes!
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        // TURN ON THE FLASH on your phone!

        try {
            // 1. which camera do you want (Front or back camera?!)
            String cameraID = cameraManager.getCameraIdList()[0];   // BACK CAMERA?

            // 2. turn the flash on
            // turn on

            if (lightOn == false) {
                cameraManager.setTorchMode(cameraID, true);
                Log.d("JENELLE", "Turning flash ON!");
                Toast.makeText(this, "Turning flash ON!", Toast.LENGTH_SHORT).show();

                lightOn = true;
            }
            else {
                cameraManager.setTorchMode(cameraID, false);

                Log.d("JENELLE", "Turning flash OFF!");
                Toast.makeText(this, "Turning flash OFF!", Toast.LENGTH_SHORT).show();

                lightOn = false;
            }

            // turn off
            //cameraManager.setTorchMode(cameraID, false);
        }
        catch (Exception e) {
            Log.d("JENELLE", "Error while turning on camera flash");
            Toast.makeText(this, "Error while turning on camera flash", Toast.LENGTH_SHORT).show();
        }
    }




}
