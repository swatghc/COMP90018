package com.example.eurka.comp90018;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
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

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
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


    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
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
}
