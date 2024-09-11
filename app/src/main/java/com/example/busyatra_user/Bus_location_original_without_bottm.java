package com.example.busyatra_user;

import static android.content.ContentValues.TAG;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bus_location_original_without_bottm extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Location userLocation; // The user's current location as a Location object
    private LatLng currentLocation;
    private Circle radiusCircle;
    private Marker userMarker;
    private static final int RADIUS_IN_METERS = 5000;

    private List<Bus> allBuses = new ArrayList<>();
    private Map<String, Marker> busMarkers = new HashMap<>();
    private BitmapDescriptor bitmapDescriptor;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_location);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Add a listener to the "buses" collection in Firestore
        CollectionReference busesRef = db.collection("messages");

        // Listen for changes to the documents in Firebase
        busesRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot snapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "listen:error", e);
                    return;
                }

                allBuses.clear();
                // Loop through the documents and add the buses to the list
                for (DocumentSnapshot dc : snapshots) {
                    Double latitude = dc.getDouble("latitude");
                    Double longitude = dc.getDouble("longitude");
                    String busName = dc.getString("text");
                    Toast.makeText(Bus_location_original_without_bottm.this, busName, Toast.LENGTH_SHORT).show();

                    LatLng location = new LatLng(latitude, longitude);
                    Bus bus = new Bus(busName, location);
                    allBuses.add(bus);
                }

                // Now that we have fetched the data, update the map with markers
                updateMapWithMarkers();
            }
        });
    }

    // Method to update the map with markers for buses within 5 kilometers from the user
    private void updateMapWithMarkers() {
        // Check if the map is ready
        if (mMap == null) {
            return;
        }

        // Check if the user location is available
        if (userLocation == null) {
            // Fetch the user location first
            fetchUserLocation();
            return;
        }

        // Filter buses based on distance from user's location
        for (Bus bus : allBuses) {
            Location busLocationObj = new Location("");
            busLocationObj.setLatitude(bus.getLocation().latitude);
            busLocationObj.setLongitude(bus.getLocation().longitude);
            float distanceToBus = userLocation.distanceTo(busLocationObj);

            // Check if the bus is within the radius
            if (distanceToBus <= RADIUS_IN_METERS) {
                // Check if the marker for this bus already exists
                Marker busMarker = busMarkers.get(bus.getBusName());
                if (busMarker == null) {
                    // Marker doesn't exist, create a new one

                    // Create a custom marker icon with the bus name
                    BitmapDescriptor customIcon = createCustomIcon(bus.getBusName());

                    busMarker = mMap.addMarker(new MarkerOptions()
                            .position(bus.getLocation())
                            .title(bus.getBusName())
                            .icon(customIcon));

                    busMarkers.put(bus.getBusName(), busMarker);
                } else {
                    // Marker already exists, just update its position
                    busMarker.setPosition(bus.getLocation());
                }
            } else {
                // Remove the marker for buses outside the radius
                removeMarkerForBus(bus);
            }
        }
    }

    private BitmapDescriptor createCustomIcon(String busName) {
        // Convert the vector drawable to a bitmap
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.busicon2);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        // Create a text paint
        Paint textPaint = new Paint();
        textPaint.setTextSize(36);
        textPaint.setColor(Color.BLACK);

        // Calculate the text position on the icon
        int x = (canvas.getWidth() - (int) textPaint.measureText(busName)) / 2;
        int y = canvas.getHeight() / 2;

        // Draw the bus name on the icon
        canvas.drawText(busName, x, y, textPaint);

        // Create a BitmapDescriptor from the updated bitmap
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void removeMarkerForBus(Bus bus) {
        // Remove the marker for the bus
        Marker busMarker = busMarkers.get(bus.getBusName());
        if (busMarker != null) {
            busMarker.remove();
            busMarkers.remove(bus.getBusName());
        }
    }

    private void fetchUserLocation() {
        // Check for location permission before accessing user's location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted, get current location
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    // Use the location to update the map
                    userLocation = location; // Store the user's current location as a Location object

                    // Update the user's location marker
                    if (userMarker == null) {
                        userMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(userLocation.getLatitude(), userLocation.getLongitude())).title("Current location"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userMarker.getPosition(), 13));
                    } else {
                        userMarker.setPosition(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()));
                    }

                    // Update the radius circle
                    if (radiusCircle != null) {
                        radiusCircle.remove(); // Remove old circle if any
                    }
                    radiusCircle = mMap.addCircle(new CircleOptions()
                            .center(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()))
                            .radius(RADIUS_IN_METERS)
                            .strokeColor(Color.BLUE)
                            .fillColor(Color.parseColor("#500084d3"))); // Semi-transparent blue color

                    // Update the bus markers
                    updateMapWithMarkers();
                }
            });
        }
    }

    // ... Your other methods ...

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Convert the vector drawable to a bitmap
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.busicon2);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
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
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Check if the marker clicked is a bus marker
                if (busMarkers.containsValue(marker)) {
                    // Open Google Maps with the route from the user's location to the bus marker's location
                    openGoogleMapsForDirections(marker.getPosition());
                    return true; // Return true to prevent the default behavior (showing the info window)
                }
                return false; // Return false to show the info window for non-bus markers
            }
        });
        mMap.setMyLocationEnabled(true);

        // Create a BitmapDescriptor from the bitmap
        bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);

        // Fetch the data from Firebase and update the map with markers
        updateMapWithMarkers();
    }

    private void openGoogleMapsForDirections(LatLng destination) {
        if (userLocation != null) {
            // Create a URI for the Google Maps intent
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + destination.latitude + "," + destination.longitude);

            // Create an intent to open Google Maps
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            // Check if there's an app to handle the intent (Google Maps is installed)
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                // Open Google Maps
                startActivity(mapIntent);
            } else {
                // Handle the case where Google Maps is not installed on the device
                // You can show a toast or alert the user to install Google Maps
                Toast.makeText(this, "Google Maps is not installed.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Handle the case where the user location is not available
            Toast.makeText(this, "User location not available.", Toast.LENGTH_SHORT).show();
        }
    }


    // Custom Bus class to hold bus information
    private static class Bus {
        private String busName;
        private LatLng location;

        public Bus(String busName, LatLng location) {
            this.busName = busName;
            this.location = location;
        }

        public String getBusName() {
            return busName;
        }

        public LatLng getLocation() {
            return location;
        }
    }
}