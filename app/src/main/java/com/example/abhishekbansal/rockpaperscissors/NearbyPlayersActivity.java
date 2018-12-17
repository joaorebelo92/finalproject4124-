package com.example.abhishekbansal.rockpaperscissors;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.abhishekbansal.rockpaperscissors.Adapters.PlayersNearbyAdapter;
import com.example.abhishekbansal.rockpaperscissors.Entities.GameRoom;
import com.example.abhishekbansal.rockpaperscissors.Entities.Player;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NearbyPlayersActivity extends AppCompatActivity implements PlayersNearbyAdapter.ItemClickListener {

    private ArrayList<Player> playersNearBy;
    Context context;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    double userLatitude;
    double userLongitude;
    PlayersNearbyAdapter selectedPlayerAdapter;
    final static String TAG = "JoaoLog";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final double EARTH_RADIUS = 6371;
    String docNumber = "";

    LocationManager manager;
    LocationListener userLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_players);
        context = this;
        this.manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        Bundle b = getIntent().getExtras();
        userLatitude = b.getDouble("userLatitude");
        userLongitude = b.getDouble("userLongitude");

        //Setup LocationListeners
        setupLocationListener();

        //Setup permissions
        setupPermissions();

        //fill players array from firebase
        playersNearBy = new ArrayList<Player>();
        db.collection("players")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (!document.getId().equals(user.getPhoneNumber())) {
                                    GeoPoint geo = (GeoPoint) document.getData().get("location");
                                    double distance = getDistance(userLatitude, userLongitude, geo.getLatitude(), geo.getLongitude());
                                    //Toast.makeText(context, "This Player is " + String.format("%.0f", (distance)) + " km from you.", Toast.LENGTH_SHORT).show();
                                    if (distance <= 500.0){
                                        Player p1 = new Player(document.getId(), geo.getLatitude(), geo.getLongitude(), true);
                                        playersNearBy.add(p1);
                                    }
                                }
                            }
                            //Log.d(TAG, playersNearBy.size() + " => " + playersNearBy.size());
                            updateRecycleView();
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });


        //RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView_players);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        selectedPlayerAdapter = new PlayersNearbyAdapter(this, playersNearBy, userLatitude, userLongitude);
        selectedPlayerAdapter.setClickListener(this);
        recyclerView.setAdapter(selectedPlayerAdapter);

        //decoration divider
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());

        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    public void onItemClick(View view, int position) {
        //go to game room
        final int pos = position;
        docNumber = "";
        db.collection("GameRooms")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                if (document.getData().get("player1Number").equals(user.getPhoneNumber()) && document.getData().get("player2Number").equals(playersNearBy.get(pos).getPhoneNumber())){
                                    //user is player 1
                                    docNumber = document.getId();
                                    Log.d(TAG, "###" + document.getId());
                                    Log.d(TAG, "Error doc?: " + docNumber);
                                    //go to game
                                    double distance = getDistance(userLatitude,userLongitude, playersNearBy.get(pos).getLat(), playersNearBy.get(pos).getLng());
                                    double distanceInMeters = distance / 0.001;
                                    if (distanceInMeters <= 500.0){
                                        Intent i = new Intent(context, GameLogic.class);
                                        i.putExtra("docNumber", docNumber);
                                        startActivity(i);
                                    }else {
                                        Toast.makeText(context, "User is far from you", Toast.LENGTH_SHORT).show();
                                    }

                                }else if (document.getData().get("player1Number").equals(playersNearBy.get(pos).getPhoneNumber()) && document.getData().get("player2Number").equals(user.getPhoneNumber())){
                                    //user is player 2
                                    docNumber = document.getId();
                                    Log.d(TAG, "***" + document.getId());
                                    Log.d(TAG, "Error doc?: " + docNumber);
                                    //go to game
                                    double distance = getDistance(userLatitude,userLongitude, playersNearBy.get(pos).getLat(), playersNearBy.get(pos).getLng());
                                    double distanceInMeters = distance / 0.001;
                                    if (distanceInMeters <= 500.0){
                                        Intent i = new Intent(context, GameLogic.class);
                                        i.putExtra("docNumber", docNumber);
                                        startActivity(i);
                                    }else {
                                        Toast.makeText(context, "User is far from you", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                            if (docNumber.equals("")){
                                GameRoom game = new GameRoom(user.getPhoneNumber(),playersNearBy.get(pos).getPhoneNumber(),"","","");
                                db.collection("GameRooms")
                                        .add(game)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                                                docNumber = documentReference.getId();
                                                Log.d(TAG, "Error doc?: " + docNumber);
                                                //go to game
                                                double distance = getDistance(userLatitude,userLongitude, playersNearBy.get(pos).getLat(), playersNearBy.get(pos).getLng());
                                                double distanceInMeters = distance / 0.001;
                                                if (distanceInMeters <= 500.0){
                                                    Intent i = new Intent(context, GameLogic.class);
                                                    i.putExtra("docNumber", docNumber);
                                                    startActivity(i);
                                                }else {
                                                    Toast.makeText(context, "User is far from you", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error adding document", e);
                                            }
                                        });
                            }


                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

    }

    public void setupPermissions() {
        if (Build.VERSION.SDK_INT < 23) {

            this.manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this.userLocationListener);

        }
        // 5b.  This is for phones AFTER Marshmallow
        else {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Show the popup box! ask for permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                // Do this code if the user PREVIOUSLY gave us permission to get location.
                // (ie: You already have permission!)
                this.manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this.userLocationListener);

            }

        }
    }

    private void updateRecycleView() {

        //Log.d(TAG, "playersNearBy.size():  " + playersNearBy.size() + "\n");
        selectedPlayerAdapter.notifyDataSetChanged();
    }

    public void setupLocationListener() {
        this.userLocationListener = new LocationListener() {

            // This function gets run when the phone receives a new location
            @Override
            public void onLocationChanged(Location location) {
                if (String.format("%.3f", userLatitude).equals(String.format("%.3f", location.getLatitude())) && String.format("%.3f", userLongitude).equals(String.format("%.3f", location.getLongitude()))) {
                    //they are the same -- don't do anyting
                    Log.d(TAG, "Same location");
                } else {
                    userLatitude = location.getLatitude();
                    userLongitude = location.getLongitude();
                    Log.d(TAG, " new userLatitude: " + " => " + userLatitude);
                    Log.d(TAG, " new userLongitude: " + " => " + userLongitude);
                    updateUserLocationOnFirebase();
                }
            }

            // IGNORE THIS FUNCTION!
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            // IGNORE THIS FUNCTION!
            @Override
            public void onProviderEnabled(String provider) {

            }

            // IGNORE THIS FUNCTION!
            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }

    public void updateUserLocationOnFirebase() {
        DocumentReference washingtonRef = db.collection("players").document(user.getPhoneNumber());
        final GeoPoint geo = new GeoPoint(userLatitude, userLongitude);
        washingtonRef
                .update("location", geo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Log.w(TAG, "Error updating document", e);
                        Map<String, Object> docData = new HashMap<>();
                        docData.put("isLoggedIn", true);

                        docData.put("location", geo);
                        db.collection("players").document(user.getPhoneNumber())
                                .set(docData)
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error adding document", e);
                                    }
                                });
                    }
                });
    }

    //get distance
    public double getDistance(double startLat, double startLong,
                              double endLat, double endLong) {

        double dLat  = Math.toRadians((endLat - startLat));
        double dLong = Math.toRadians((endLong - startLong));

        startLat = Math.toRadians(startLat);
        endLat   = Math.toRadians(endLat);

        double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    public static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }
}
