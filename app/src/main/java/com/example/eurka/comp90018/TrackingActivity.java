package com.example.eurka.comp90018;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.telephony.SmsManager;
import android.widget.Toast;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class TrackingActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    //A LatLng is a point in geographical coordinates: latitude and longitude.
    private LatLng DEFAULT_LATandLNG;
    private static final String TAG = "TrackingActivity";
    private ArrayList locationArray = new ArrayList();
    public String title = "";
    private Intent service;
    private LinearLayout layout;
    private Button submitButton;
    private String username;
    private String emergencycontact;
    private double totalDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        //Sets a callback object which will be triggered when the GoogleMap instance is ready to be used.
        mapFragment.getMapAsync(this);

        //receive user id from main
        Bundle bundle = this.getIntent().getExtras();
        username = bundle.getString("username");
        emergencycontact = bundle.getString("emergencycontact");

        layout= (LinearLayout)findViewById(R.id.layout);
        submitButton= (Button) findViewById(R.id.submitButton);
        submitButton.setEnabled(false);

        //only receive intent when intentService finsh the task
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyIntentService.TRANSACTION_DONE);
        registerReceiver(locationReceiver, intentFilter);

    }

    @Override
    protected void onRestart(){
        super.onRestart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyIntentService.TRANSACTION_DONE);
        registerReceiver(locationReceiver, intentFilter);
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



    //start logging the track via MyIntentService
    public void startLogging(View view){
        layout.setBackgroundColor(Color.parseColor("#51b46d"));
        Log.i("hello",emergencycontact);
        service = new Intent(this, MyIntentService.class);

        startService(service);
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


    //stop logging track
    public void stopLogging(View view){
        layout.setBackgroundColor(Color.parseColor("#39add1"));
        submitButton.setEnabled(true);
        stopService(service);
    }

    public void emergencyFunction(View view){
        layout.setBackgroundColor(Color.parseColor("#38d145"));
        Log.d("hello","you are here!");
        Log.d("hello",emergencycontact);
        SmsManager smsManager = SmsManager.getDefault();
        getCurrentLocation();
        //error
        String location = LatLngtoAddress(DEFAULT_LATandLNG);
        String text = "you friend "+username+" encounters emergency,current location: "
                +location;
        Log.d("hello",text);
        Toast.makeText(TrackingActivity.this, text,
                Toast.LENGTH_SHORT).show();

        smsManager.sendTextMessage(emergencycontact,null,text,null,null);
        Toast.makeText(TrackingActivity.this, "发送完毕", Toast.LENGTH_SHORT).show();
        stopService(service);

    }

    public String LatLngtoAddress(LatLng latLng){
        Geocoder geocoder;
        List<android.location.Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try{
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

            return  address+" "+postalCode+" "+city;

        }catch (IOException e){
            e.printStackTrace();
        }
        return null;

    }

    private BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "map activity Reciever HIT");
            locationArray = intent.getParcelableArrayListExtra("locationData");
            LatLng[] array = new LatLng[locationArray.size()];
            int len = array.length;
            Log.i(TAG, "the array lenght is:"+String.valueOf(len));
            locationArray.toArray(array);
            for (int i=0; i < array.length; i++) {
                double lat = array[i].latitude;
                Log.i(TAG, String.valueOf(lat));
                mMap.addCircle(new CircleOptions()
                        .center(array[i])
                        .radius(8)
                        .fillColor(0x7f0000ff)
                        .strokeWidth(0));
            }

            double distance = 0;
            for (int i=0;i < array.length-1; i++) {
                distance = distance + CalculationByDistance(array[i],array[i+1]);
            }
            totalDistance = distance;

        }
    };

    @Override
    protected void onStop()
    {
        unregisterReceiver(locationReceiver);
        super.onStop();
    }

    //method be used to calculate the distance between two LatLng objects.
    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }


}
