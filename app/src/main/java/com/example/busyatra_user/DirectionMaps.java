package com.example.busyatra_user;

import static android.content.ContentValues.TAG;

import static androidx.core.content.ContentProviderCompat.requireContext;

import androidx.annotation.NonNull;
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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
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
import com.example.busyatra_user.databinding.ActivityDirectionMapsBinding;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;
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

public class DirectionMaps extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, RoutingListener  {

    private GoogleMap mMap;
    private List<Polyline> polylines = null;
    private BitmapDescriptor bitmapDescriptor;
    protected LatLng start = null;
    protected LatLng end = null;

    private FusedLocationProviderClient fusedLocationClient;
    private Location userLocation; // The user's current location as a Location object
    private LatLng currentLocation;
    //private Circle radiusCircle;
    //private static final int RADIUS_IN_METERS = 5000;
    private String busName;
    private Marker userMarker;

    private LatLng busLocation;
    private List<DirectionMaps.Bus> allBuses = new ArrayList<>();
    private Map<String, Marker> busMarkers = new HashMap<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ActivityDirectionMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction_maps);
        Intent intent = getIntent();
        busName = intent.getStringExtra("busName");
        busLocation = intent.getParcelableExtra("busLocation");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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
                    // Toast.makeText(getActivity(), busName, Toast.LENGTH_SHORT).show();
                    LatLng location = new LatLng(latitude, longitude);
                    DirectionMaps.Bus bus = new DirectionMaps.Bus(busName, location);
                    allBuses.add(bus);
                }

                // Now that we have fetched the data, update the map with markers
                updateMapWithMarkers();
            }
        });

    }
    private void updateMapWithMarkers() {
        if (mMap == null || busName == null) {
            return;
        }

        // Check if the user location is available
        if (userLocation == null) {
            // Fetch the user location first
            fetchUserLocation();
            return;
        }

        // Only add a marker for the bus whose name matches the busName
        for (DirectionMaps.Bus bus : allBuses) {
            if (busName.equals(bus.getBusName())) {
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
                // Remove the marker for buses with different names
                removeMarkerForBus(bus);
            }
        }
    }
    private BitmapDescriptor createCustomIcon(String busName) {
        // Convert the vector drawable to a bitmap
        Drawable drawable = ContextCompat.getDrawable(DirectionMaps.this, R.drawable.busicon2);
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

    private void removeMarkerForBus(DirectionMaps.Bus bus) {
        // Remove the marker for the bus
        Marker busMarker = busMarkers.get(bus.getBusName());
        if (busMarker != null) {
            busMarker.remove();
            busMarkers.remove(bus.getBusName());
        }
    }

    private void fetchUserLocation() {
        // Check for location permission before accessing user's location
        if (ActivityCompat.checkSelfPermission(DirectionMaps.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted, get current location
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    // Use the location to update the map
                    userLocation = location; // Store the user's current location as a Location object

                    // Update the user's location marker
                    if (userMarker == null) {
                        userMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(userLocation.getLatitude(), userLocation.getLongitude())).title("Current location"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userMarker.getPosition(), 13));
                        currentLocation=new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
                    } else {
                        userMarker.setPosition(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()));
                        currentLocation=new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
                    }

                    // Update the radius circle// Semi-transparent blue color

                    // Update the bus markers
                    updateMapWithMarkers();
                    Toast.makeText(this, ""+currentLocation, Toast.LENGTH_SHORT).show();
                   if (busLocation != null && currentLocation!=null) {
                        Findroutes(currentLocation, busLocation);
                       Toast.makeText(this, " Bus :"+busLocation, Toast.LENGTH_SHORT).show();

                   }
                   else {
                       Toast.makeText(this, "khali h ", Toast.LENGTH_SHORT).show();
                   }
                }
            });
        }
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

        mMap.setMyLocationEnabled(true);

        // Create a BitmapDescriptor from the bitmap
        bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);

        // Fetch the data from Firebase and update the map with markers
        updateMapWithMarkers();
      //  Toast.makeText(this, ""+currentLocation, Toast.LENGTH_SHORT).show();

//        if (busLocation != null) {
//                  Findroutes(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()), busLocation);
//                    }
//                    else {
//                        Toast.makeText(this, "khali h ", Toast.LENGTH_SHORT).show();
//                    }
    }
    public void Findroutes(LatLng Start, LatLng End)
    {
        if(Start==null || End==null) {
            Toast.makeText(DirectionMaps.this,"Unable to get location", Toast.LENGTH_LONG).show();
        }
        else
        {

            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(true)
                    .waypoints(Start, End)
                    .key("AIzaSyC7ZQ5LDflovXKny22vY_f7SLtTdfwPs_0")  //also define your api key here.
                    .build();
            routing.execute();
        }
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        View parentLayout = findViewById(android.R.id.content);
        Snackbar snackbar= Snackbar.make(parentLayout, e.toString(), Snackbar.LENGTH_LONG);
        snackbar.show();

    }

    @Override
    public void onRoutingStart() {
        Toast.makeText(DirectionMaps.this,"Finding Route...",Toast.LENGTH_LONG).show();

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        CameraUpdate center = CameraUpdateFactory.newLatLng(currentLocation);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
        if(polylines!=null) {
            polylines.clear();
        }
        PolylineOptions polyOptions = new PolylineOptions();
        LatLng polylineStartLatLng=null;
        LatLng polylineEndLatLng=null;


        polylines = new ArrayList<>();
        //add route(s) to the map using polyline
        for (int i = 0; i <route.size(); i++) {

            if(i==shortestRouteIndex)
            {
                polyOptions.color(getResources().getColor(R.color.black));
                polyOptions.width(7);
                polyOptions.addAll(route.get(shortestRouteIndex).getPoints());
                Polyline polyline = mMap.addPolyline(polyOptions);
                polylineStartLatLng=polyline.getPoints().get(0);
                int k=polyline.getPoints().size();
                polylineEndLatLng=polyline.getPoints().get(k-1);
                polylines.add(polyline);

            }
            else {

            }

        }

        //Add Marker on route starting position
        MarkerOptions startMarker = new MarkerOptions();
        startMarker.position(polylineStartLatLng);
        startMarker.title("My Location");
       // mMap.addMarker(startMarker);

        //Add Marker on route ending position
        MarkerOptions endMarker = new MarkerOptions();
        endMarker.position(polylineEndLatLng);
        endMarker.title("Destination");
      //  mMap.addMarker(endMarker);

    }

    @Override
    public void onRoutingCancelled() {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

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