package com.example.abhishekbansal.rockpaperscissors;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.abhishekbansal.rockpaperscissors.Entities.Player;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<Player> playersOnlineList;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    MenuItem btnPlayersList, btnSendMessage;
    Context context;
    LocationManager manager;
    LocationListener userLocationListener;
    double userLatitude;
    double userLongitude;
    final static String TAG = "JoaoLog";
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        context = this;
        Log.d(TAG, "user: " + user.getPhoneNumber());
        this.manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location knownLocation = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        userLatitude = knownLocation.getLatitude();
        userLongitude = knownLocation.getLongitude();
        //updateUserLocationOnFirebase();

        //Setup LocationListeners
        setupLocationListener();

        //Setup permissions
        setupPermissions();


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        //TODO get from firebase all users online
        playersOnlineList = new ArrayList<>();


        mapFragment.getMapAsync(this);
        Log.d(TAG, "userLatitude: " + " => " + userLatitude);
        Log.d(TAG, "userLongitude: " + " => " + userLongitude);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.players_list_menu, menu);
        btnPlayersList = menu.findItem(R.id.action_players_list);
        btnSendMessage = menu.findItem(R.id.action_send_message);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.action_players_list) {
            Log.d(TAG, "*****userLatitude: " + " => " + userLatitude);
            Log.d(TAG, "*****userLongitude: " + " => " + userLongitude);
            Intent i = new Intent(this, NearbyPlayersActivity.class);
            i.putExtra("userLatitude", userLatitude);
            i.putExtra("userLongitude", userLongitude);
            startActivity(i);
            return true;
        }else if(id == R.id.action_send_message){
            Intent i = new Intent(this, Invite.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        //TODO: reorganize the code

        db.collection("players")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (!document.getId().equals(user.getPhoneNumber())){
                                    Log.d("--test", document.getId() + " => " + document.getData());
                                    GeoPoint geo = (GeoPoint) document.getData().get("location");
                                    Player p1 = new Player(document.getId(), geo.getLatitude(), geo.getLongitude(), true);


                                    LatLng coordinate = new LatLng(p1.getLat(), p1.getLng());
                                    mMap.addMarker(new MarkerOptions()
                                            .position(coordinate)
                                            .title(p1.getPhoneNumber().toString())
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                                    playersOnlineList.add(p1);
                                }
                            }
                        } else {
                            Log.w("--test2", "Error getting documents.", task.getException());
                        }
                    }
                });

        LatLng coordinate = new LatLng(43.772796, -79.335661);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 13));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

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

    public void setupPermissions() {
        if (Build.VERSION.SDK_INT < 23) {
            this.manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this.userLocationListener);
        }
        // 5b.  This is for phones AFTER Marshmallow
        else {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Show the popup box! ask for permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            } else {
                // Do this code if the user PREVIOUSLY gave us permission to get location.
                // (ie: You already have permission!)
                this.manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this.userLocationListener);

            }

        }
    }

    public void updateUserLocationOnFirebase() {
        DocumentReference washingtonRef = db.collection("players").document(user.getPhoneNumber());

// Set the "isCapital" field of the city 'DC'
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
        LatLng coordinate = new LatLng(userLatitude, userLongitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 13));
    }
}
