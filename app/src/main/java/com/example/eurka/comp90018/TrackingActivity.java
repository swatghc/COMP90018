package com.example.eurka.comp90018;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;


public class TrackingActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    //A LatLng is a point in geographical coordinates: latitude and longitude.
    private LatLng DEFAULT_LATandLNG;
    private static final String TAG = "TrackingActivity";
    private ArrayList locationArray = new ArrayList();
    public String title = "";
    private Intent service;
    private LinearLayout layout;
    private Button submitButton;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        //Sets a callback object which will be triggered when the GoogleMap instance is ready to be used.
        mapFragment.getMapAsync(this);

    }


    //Callback method implement the interface, be used when the map is ready to be used.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getCurrentLocation();

        //Returns a CameraUpdate that moves the center of the screen to a latitude and longitude specified by a LatLng object,
        // and moves to the given zoom level.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LATandLNG, 17.0f));

        try {
            // My Location button appears in the top right corner of the map.
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }

    }

    public void getCurrentLocation(){
        double Default_Lat = 0;
        double Default_Lng = 0;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location location;
        for(String provider : providers){
            try{
                location = locationManager.getLastKnownLocation(provider);
                if(location!= null){
                    Default_Lat = location.getLatitude();
                    Default_Lng = location.getLongitude();
                    break;
                }

            }catch (SecurityException e){
                e.printStackTrace();
            }
        }
        DEFAULT_LATandLNG = new LatLng(Default_Lat,Default_Lng);

    }



    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
