package com.example.eurka.comp90018;

import android.Manifest;
import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MyIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private LatLng DEFAULT_LATandLNG;
    public Timer mTimer;
    private ArrayList locationList = new ArrayList();
    private Location location;
    private static final String ACTION_FOO = "start";
    private static final String ACTION_BAZ = "stop";
    private static final String TAG = "TrackingActivity";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.example.eurka.comp90018.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.example.eurka.comp90018.extra.PARAM2";

    public MyIntentService() {
        super("MyIntentService");
    }


    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "starting!!");
        startLogging();
        return 0;
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getStringExtra("action");
            Log.i(TAG, action);
            if (ACTION_FOO.equals(action)) {
                startLogging();
            } else if (ACTION_BAZ.equals(action)) {
                stopLogging();
            }
        }
    }

    public void startLogging() {
        Log.i(TAG, "THIS WAS HIT IN THE BACKGROUND SERVICE");
        getCurrentLocation();
    }

    public TimerTask mTask = new TimerTask() {
        public void run() {
            getCurrentLocation();
            double lat = DEFAULT_LATandLNG.latitude;
            Log.i(TAG, "timer task latitiude is : " + String.valueOf(lat));
            locationList.add(DEFAULT_LATandLNG);
            int len = locationList.size();
            Log.i(TAG, "before it is sent the length is :" + String.valueOf(len));
        }
    };

    public void stopLogging() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mTask.cancel();
        int len = locationList.size();
        Log.i(TAG, "In STop Logging length is :" + String.valueOf(len));
        notifyFinished();
    }
    @Override
    public void onDestroy(){
        Log.i(TAG,"Destroyed!!!");
        stopLogging();
    }

    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

            useLocation(location);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
    };

    public void getCurrentLocation(){
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);
        for (int i = 0; i < providers.size(); i++) {
            Log.i(TAG, providers.get(i));

            try {
                location = lm.getLastKnownLocation(providers.get(i));
                lm.requestLocationUpdates(providers.get(i),5000,0,locationListener);

            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

    }

    int numberOfHits = 0;

    public void useLocation(Location l) {
        numberOfHits++;
        double Default_Lat = 0;
        double Default_Lng = 0;
        location= l;
        if (location != null) {
            Default_Lat = location.getLatitude();
            Default_Lng = location.getLongitude();
        }
        Log.i(TAG, String.valueOf(Default_Lat)+"  "+numberOfHits);
        DEFAULT_LATandLNG = new LatLng(Default_Lat, Default_Lng);
        locationList.add(DEFAULT_LATandLNG);
    }

    public static final String TRANSACTION_DONE = "done";

    private void notifyFinished(){
        int len = locationList.size();
        Log.i(TAG, "In notify Finished the length is :" +String.valueOf(len));
        Intent i = new Intent(TRANSACTION_DONE);
        i.putParcelableArrayListExtra("locationData", locationList);
        MyIntentService.this.sendBroadcast(i);
    }
}
