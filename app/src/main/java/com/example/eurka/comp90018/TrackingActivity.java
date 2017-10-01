package com.example.eurka.comp90018;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.NextServiceFilterCallback;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStoreException;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;
import com.squareup.okhttp.OkHttpClient;

import android.telephony.SmsManager;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


public class TrackingActivity extends FragmentActivity implements OnMapReadyCallback {

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
    private Date startTime;
    private Date endTime;
    private String duriation;
    private String currentLocation;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日   HH:mm:ss");
    volatile boolean shutdown = false;
    private DatabaseAdapter.DatabaseHelper dbHelper;


    /**
     * Mobile Service Client reference
     */
    private MobileServiceClient mClient;

    /**
     * Mobile Service Table used to access data
     */
    private MobileServiceTable<ToDoItem> mToDoTable;

    private ProgressDialog progressDialog;

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

        dbHelper = new DatabaseAdapter.DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("user", null, null, null, null, null, null);

        String emeContact = "";
        while (cursor.moveToNext()) {
            String username = cursor.getString(cursor.getColumnIndex("username"));
            emeContact = cursor.getString(cursor.getColumnIndex("emergencycontact"));
        }

        if (!emeContact.equals("")) {
            emergencycontact = emeContact;
        }
        Log.i("hello", "EMS  " + emergencycontact);


        layout = (LinearLayout) findViewById(R.id.layout);
//        submitButton= (Button) findViewById(R.id.submitButton);
//        submitButton.setEnabled(false);

        //only receive intent when intentService finsh the task
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyIntentService.TRANSACTION_DONE);
        registerReceiver(locationReceiver, intentFilter);


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("loading...");

        try {
            // Create the Mobile Service Client instance, using the provided

            // Mobile Service URL and key
            mClient = new MobileServiceClient(
                    "https://comp90018.azurewebsites.net",
                    this).withFilter(new ProgressFilter());

            // Extend timeout from default of 10s to 20s
            mClient.setAndroidHttpClientFactory(new OkHttpClientFactory() {
                @Override
                public OkHttpClient createOkHttpClient() {
                    OkHttpClient client = new OkHttpClient();
                    client.setReadTimeout(20, TimeUnit.SECONDS);
                    client.setWriteTimeout(20, TimeUnit.SECONDS);
                    return client;
                }
            });

            // Get the Mobile Service Table instance to use
            mToDoTable = mClient.getTable(ToDoItem.class);

            // Offline Sync
            //mToDoTable = mClient.getSyncTable("ToDoItem", ToDoItem.class);

            //Init local storage
            initLocalStore().get();

        } catch (MalformedURLException e) {
            createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e) {
            createAndShowDialog(e, "Error");
        }

    }

    @Override
    protected void onRestart() {
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
    public void startLogging(View view) {
        layout.setBackgroundColor(Color.parseColor("#51b46d"));
        Log.i("hello", emergencycontact);
        service = new Intent(this, MyIntentService.class);
        startService(service);
        startTime = new Date(System.currentTimeMillis());
        startDate = format.format(startTime);
        Log.i("hello", "begin   " + startDate);

    }

    private String startDate;
    private String endDate;

    public void getCurrentLocation() {
        double Default_Lat = 0;
        double Default_Lng = 0;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location location;
        for (String provider : providers) {
            try {
                location = locationManager.getLastKnownLocation(provider);
                if (location != null) {
                    Default_Lat = location.getLatitude();
                    Default_Lng = location.getLongitude();
                    break;
                }

            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        DEFAULT_LATandLNG = new LatLng(Default_Lat, Default_Lng);

    }


    //stop logging track
    public void stopLogging(View view) {
        layout.setBackgroundColor(Color.parseColor("#39add1"));
        //submitButton.setEnabled(true);
        stopService(service);

        endTime = new Date(System.currentTimeMillis());
        endDate = format.format(endTime);
        Log.i("hello", "stop   " + endDate);
        long dur = endTime.getTime() - startTime.getTime();
        duriation = formattingMs(dur);
        Log.i("hello", duriation);


        Toast.makeText(TrackingActivity.this, "Total Distance: " + totalDistance + "KM" + "  Spent Time: " + duriation, Toast.LENGTH_SHORT).show();

    }

    public String formattingMs(long period) {
        Integer seconds = (int) (period / 1000) % 60;
        Integer minutes = (int) ((period / (1000 * 60)) % 60);
        Integer hours = (int) ((period / (1000 * 60 * 60)) % 24);
        String s = seconds.toString();
        String m = minutes.toString();
        String h = hours.toString();
        return h + ":" + m + ":" + s;
    }

    public void submit(View view) {

        if (TextUtils.isEmpty(startDate) || TextUtils.isEmpty(endDate) || endDate.compareTo(startDate) <= 0) {
            Toast.makeText(TrackingActivity.this, "You haven't started or stopped yet", Toast.LENGTH_SHORT).show();
            return;
        }


        if (mClient == null) {
            return;
        }

        // Create a new item
        final ToDoItem item = new ToDoItem();
        item.setUsername(username);
        item.setDate(startDate);
        item.setDuriation("Spent Time: " + duriation);
        item.setTotalDistance("Total Distance: " + totalDistance + "KM");

        // Insert the new item
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    addItemInTable(item);
                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };

        runAsyncTask(task);
    }

    /**
     * Add an item to the Mobile Service Table
     *
     * @param item The item to Add
     */
    public ToDoItem addItemInTable(ToDoItem item) throws ExecutionException, InterruptedException {
        ToDoItem entity = mToDoTable.insert(item).get();
        return entity;
    }

    public void emergencyFunction(View view) {
        getCurrentLocation();
        layout.setBackgroundColor(Color.parseColor("#38d145"));

        new Thread(runnable).start();

        if (currentLocation != null) {
            SmsManager smsManager = SmsManager.getDefault();

            String text = "you friend " + username + " encounters emergency,current location: "
                    + currentLocation;
            Log.i("hello", text);

            Toast.makeText(TrackingActivity.this, text,
                    Toast.LENGTH_SHORT).show();

            smsManager.sendTextMessage(emergencycontact, null, text, null, null);
            Toast.makeText(TrackingActivity.this, "发送完毕", Toast.LENGTH_SHORT).show();
        }

//        stopService(service);

    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            currentLocation = getCurrentLocationViaJSON(DEFAULT_LATandLNG.latitude, DEFAULT_LATandLNG.longitude);
            Log.i("Hello", currentLocation);
        }
    };


    private BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "map activity Reciever HIT");
            locationArray = intent.getParcelableArrayListExtra("locationData");
            LatLng[] array = new LatLng[locationArray.size()];
            int len = array.length;
            //Log.i(TAG, "the array lenght is:"+String.valueOf(len));
            locationArray.toArray(array);
            for (int i = 0; i < array.length; i++) {
                double lat = array[i].latitude;
                Log.i(TAG, String.valueOf(lat));
                mMap.addCircle(new CircleOptions()
                        .center(array[i])
                        .radius(9)
                        .fillColor(0x7f0000ff)
                        .strokeWidth(0));
            }

            double distance = 0;
            for (int i = 0; i < array.length - 1; i++) {
                distance = distance + CalculationByDistance(array[i], array[i + 1]);
            }
            totalDistance = distance;

        }
    };

    @Override
    protected void onStop() {
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

    public static JSONObject getLocationInfo(double lat, double lng) {
        try {
            URL url = new URL("http://maps.googleapis.com/maps/api/geocode/json?latlng=" + lat + "," + lng + "&sensor=true");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            InputStream in = new BufferedInputStream(connection.getInputStream());

            StringBuilder stringBuilder = new StringBuilder();
            int b;
            while ((b = in.read()) != -1) {
                stringBuilder.append((char) b);
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject = new JSONObject(stringBuilder.toString());
            Log.i("hello", "GEO " + jsonObject.toString());

            return jsonObject;

        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;
    }

    public static String getCurrentLocationViaJSON(double lat, double lng) {

        JSONObject jsonObj = getLocationInfo(lat, lng);
        Log.i("JSON string =>", jsonObj.toString());

        String currentLocation = "testing";
        String street_address = null;
        String postal_code = null;

        try {
            String status = jsonObj.getString("status").toString();
            Log.i("status", status);

            if (status.equalsIgnoreCase("OK")) {
                JSONArray results = jsonObj.getJSONArray("results");
                int i = 0;
                Log.i("i", i + "," + results.length()); //TODO delete this
                do {

                    JSONObject r = results.getJSONObject(i);
                    JSONArray typesArray = r.getJSONArray("types");
                    String types = typesArray.getString(0);

                    if (types.equalsIgnoreCase("street_address")) {
                        street_address = r.getString("formatted_address").split(",")[0];
                        Log.i("street_address", street_address);
                    } else if (types.equalsIgnoreCase("postal_code")) {
                        postal_code = r.getString("formatted_address");
                        Log.i("postal_code", postal_code);
                    }

                    if (street_address != null && postal_code != null) {
                        currentLocation = street_address + "," + postal_code;
                        Log.i("Current Location =>", currentLocation); //Delete this
                        i = results.length();
                    }

                    i++;
                } while (i < results.length());

                Log.i("JSON Geo Locatoin =>", currentLocation);
                return currentLocation;
            }

        } catch (JSONException e) {
            Log.e("testing", "Failed to load JSON");
            e.printStackTrace();
        }
        return null;
    }


    private class ProgressFilter implements ServiceFilter {

        @Override
        public ListenableFuture<ServiceFilterResponse> handleRequest(ServiceFilterRequest request, NextServiceFilterCallback nextServiceFilterCallback) {

            final SettableFuture<ServiceFilterResponse> resultFuture = SettableFuture.create();


            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (progressDialog != null) progressDialog.show();
                }
            });

            ListenableFuture<ServiceFilterResponse> future = nextServiceFilterCallback.onNext(request);

            Futures.addCallback(future, new FutureCallback<ServiceFilterResponse>() {
                @Override
                public void onFailure(Throwable e) {
                    resultFuture.setException(e);
                }

                @Override
                public void onSuccess(ServiceFilterResponse response) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (progressDialog != null) progressDialog.dismiss();
                        }
                    });

                    resultFuture.set(response);
                }
            });

            return resultFuture;
        }
    }

    /**
     * Creates a dialog and shows it
     *
     * @param exception The exception to show in the dialog
     * @param title     The dialog title
     */
    private void createAndShowDialogFromTask(final Exception exception, String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createAndShowDialog(exception, "Error");
            }
        });
    }


    /**
     * Creates a dialog and shows it
     *
     * @param exception The exception to show in the dialog
     * @param title     The dialog title
     */
    private void createAndShowDialog(Exception exception, String title) {
        Throwable ex = exception;
        if (exception.getCause() != null) {
            ex = exception.getCause();
        }
        createAndShowDialog(ex.getMessage(), title);
    }

    /**
     * Creates a dialog and shows it
     *
     * @param message The dialog message
     * @param title   The dialog title
     */
    private void createAndShowDialog(final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }

    /**
     * Run an ASync task on the corresponding executor
     *
     * @param task
     * @return
     */
    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }

    /**
     * Initialize local storage
     *
     * @return
     * @throws MobileServiceLocalStoreException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private AsyncTask<Void, Void, Void> initLocalStore() throws MobileServiceLocalStoreException, ExecutionException, InterruptedException {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    MobileServiceSyncContext syncContext = mClient.getSyncContext();

                    if (syncContext.isInitialized())
                        return null;

                    SQLiteLocalStore localStore = new SQLiteLocalStore(mClient.getContext(), "OfflineStore", null, 1);

                    Map<String, ColumnDataType> tableDefinition = new HashMap<String, ColumnDataType>();
                    tableDefinition.put("id", ColumnDataType.String);
                    tableDefinition.put("text", ColumnDataType.String);
                    tableDefinition.put("complete", ColumnDataType.Boolean);

                    localStore.defineTable("ToDoItem", tableDefinition);

                    SimpleSyncHandler handler = new SimpleSyncHandler();

                    syncContext.initialize(localStore, handler).get();

                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }
        };

        return runAsyncTask(task);
    }


}
