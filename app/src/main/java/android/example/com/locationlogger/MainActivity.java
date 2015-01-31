package android.example.com.locationlogger;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.example.com.locationlogger.preferences.SettingsActivity;
import android.example.com.locationlogger.service.LocationLoggingService;
import android.location.Location;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.example.com.locationlogger.LocationContract.LocationEntry;

import java.util.Date;


public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    public static final String LOCATION_UPDATED_EVENT = "android.example.com.locationlogger.locationUpdated";
    public static final String SHOW_DETAIL_EXTRA = "android.example.com.locationlogger.showdetail_extra";
    public static final String LOCATION_EXTRA = "android.example.com.locationlogger.location_extra";
    public static final String ADDRESS_EXTRA = "android.example.com.locationlogger.address_extra";

    public static final int IS_ALIVE = Activity.RESULT_FIRST_USER; // first available user result code.

    public static final int NEW_LOCATION_MIN_DISTANCE = 30; // minimum required distance in meters from last location

    private static final int LOCATION_UPDATE_INTERVAL = 10000; // desired location update interval in milliseconds
    private static final int LOCATION_UPDATE_FASTEST_INTERVAL = 5000; //

    private static final long LOCATION_MAX_AGE = 60000L; // max allowed location age in milliseconds
    private static final double LOCATION_MAX_RADIUS = 1000D; // max allowed location uncertainty (accuracy) in meters

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private ShareActionProvider mShareActionProvider;

    private BroadcastReceiver mLocationReceiver;

    private ResultReceiver mResultReceiver;
    private String mCurrentAddress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(LOG_TAG, "onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                if(locationIsFresh(mCurrentLocation))
                {
                    Toast.makeText(MainActivity.this, getString(R.string.fething_address), Toast.LENGTH_SHORT).show();

                    AddressIntentService.startActionFetchAddress(MainActivity.this, mCurrentLocation, mResultReceiver);

                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.no_valid_location), Toast.LENGTH_SHORT).show();
                }
            }
        });

        buildGoogleApiClient();

        mResultReceiver = new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);
                if (resultCode == Globals.RESULT_OK) {
                    handleNewAddress( resultData.getString(Globals.ADDRESS_RESULT) );
                }
                // store location and address:
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Cursor existingLog = getContentResolver().query(
                                LocationEntry.CONTENT_URI,
                                new String[]{LocationEntry.COLUMN_LOCATION_TIMESTAMP},
                                LocationEntry.COLUMN_LOCATION_ADDRESS + " = '" + mCurrentAddress + "'",null,null);

                        if(existingLog != null && existingLog.moveToFirst())
                        {
                            // this address is already in the database!
                            return;
                        }

                        // otherwise: insert the new address:
                        ContentValues values = new ContentValues();
                        values.put(LocationEntry.COLUMN_LOCATION_ADDRESS, mCurrentAddress != "" ? mCurrentAddress : mCurrentLocation.getLatitude()+","+mCurrentLocation.getLongitude());
                        values.put(LocationEntry.COLUMN_LOCATION_LATITUDE, mCurrentLocation.getLatitude());
                        values.put(LocationEntry.COLUMN_LOCATION_LONGITUDE, mCurrentLocation.getLongitude());
                        values.put(LocationEntry.COLUMN_LOCATION_TIMESTAMP, mCurrentLocation.getTime());

                        getContentResolver().insert(LocationContract.LocationEntry.CONTENT_URI, values);
                    }
                }).start();
            }
        };

        mLocationReceiver = new BroadcastReceiver() {
            // receive location update events:
            @Override
            public void onReceive(Context context, Intent intent) {

                if(isOrderedBroadcast())
                {
                    // tell the next receiver that we were here!
                    setResultCode(MainActivity.IS_ALIVE);
                }

                if( intent.hasExtra(MainActivity.LOCATION_EXTRA))
                {
                    Location location = intent.getParcelableExtra(MainActivity.LOCATION_EXTRA);
                    handleNewLocation(location);
                }

                if( intent.hasExtra(MainActivity.ADDRESS_EXTRA))
                {
                    String address = intent.getStringExtra(MainActivity.ADDRESS_EXTRA);
                    handleNewAddress(address);
                }
            }
        };

        if( savedInstanceState != null) {
            Location location = savedInstanceState.getParcelable(MainActivity.LOCATION_EXTRA);
            handleNewLocation(location);
            handleNewAddress( savedInstanceState.getString(MainActivity.ADDRESS_EXTRA) );
        }
    }

    // -- Overrides for logging ---------------------------------------------------------------------------
    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_TAG, "onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume()");

        // subscribe to location update events:
        registerReceiver(mLocationReceiver, new IntentFilter(LOCATION_UPDATED_EVENT));

        // tell service to start the location updates by passing the ACTION_START string to the service intent:
        Intent serviceIntent = new Intent(this, LocationLoggingService.class);
        startService(serviceIntent);

        Intent receivedIntent = getIntent();
        if(receivedIntent.hasExtra(MainActivity.SHOW_DETAIL_EXTRA)) {
            // activity was started with a request to launch the detail activity:
            Intent intent = new Intent(this, ListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(MainActivity.SHOW_DETAIL_EXTRA, "x");
            intent.putExtra(ListActivity.LOCATION_EXTRA, receivedIntent.getParcelableExtra(MainActivity.LOCATION_EXTRA));
            intent.putExtra(MainActivity.ADDRESS_EXTRA, receivedIntent.getStringExtra(MainActivity.ADDRESS_EXTRA));
            startActivity(intent);
            getIntent().removeExtra(MainActivity.SHOW_DETAIL_EXTRA);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "onPause()");

        // unsubscribe from location update events:
        unregisterReceiver(mLocationReceiver);

        boolean enableBackgroundUpdates = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_background_updates", false);
        if(! enableBackgroundUpdates)
        {
            // tell service to stop the location updates by passing the ACTION_STOP string to the service intent:
            Intent serviceIntent = new Intent(this, LocationLoggingService.class);
            //serviceIntent.putExtra(LocationLoggerService.SERVICE_ACTION, LocationLoggerService.ACTION_STOP);
            stopService(serviceIntent);
        }

    }

    @Override
    protected void onStop() {
        Log.i(LOG_TAG,"onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i(LOG_TAG, "onDestroy()");


        boolean enableBackgroundUpdates = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_background_updates", false);
        if(enableBackgroundUpdates)
        {
            // background updates enabled, so service was not stopped in onPause. Tell service to stop the location updates by passing the ACTION_STOP string to the service intent:
            Intent serviceIntent = new Intent(this, LocationLoggingService.class);
            //serviceIntent.putExtra(LocationLoggerService.SERVICE_ACTION, LocationLoggerService.ACTION_STOP);
            stopService(serviceIntent);
        }

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(mCurrentLocation != null && locationIsFresh(mCurrentLocation))
          outState.putParcelable(MainActivity.LOCATION_EXTRA, mCurrentLocation);
        if(mCurrentAddress != null)
            outState.putString(MainActivity.ADDRESS_EXTRA, mCurrentAddress);
        super.onSaveInstanceState(outState);
    }

    // ----------------------------------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem shareMenuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareMenuItem);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }

        if (id == R.id.action_archive) {

            Intent intent = new Intent(this, ListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(ListActivity.LOCATION_EXTRA, mCurrentLocation);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // -- GoogleApiClient.ConnectionCallbacks ------------------------------------------------------------
    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest request = new LocationRequest()
                .setInterval(LOCATION_UPDATE_INTERVAL)
                .setFastestInterval(LOCATION_UPDATE_FASTEST_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, request, this);
    }

    @Override
    public void onConnectionSuspended(int code) {
        Log.i(LOG_TAG, "onConnectionSuspended, code="+code);
        mGoogleApiClient.connect();
    }

    // -- GoogleApiClient.OnConnectionFailedListener ----------------------------------------------------
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    // -- LocationListener ------------------------------------------------------------------------------
    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }


    // -- Location helpers ------------------------------------------------------------------------------------

    private void handleNewAddress(String address)
    {
        if(address == null)
            return;

        mCurrentAddress = address;
        ((TextView) findViewById(R.id.location_info_textview)).setText( mCurrentAddress != "" ? mCurrentAddress : getString(R.string.no_address_found));
        SharingHelper.createShareLocationIntent(mCurrentLocation, mCurrentAddress);

    }

    private void handleNewLocation(Location location)
    {
        mCurrentLocation = location;
        if(locationIsFresh(mCurrentLocation))
        {
            LatLng locLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            float zoom = mMap.getCameraPosition().zoom;
            MarkerOptions options = new MarkerOptions()
                    .position(locLatLng)
                    .title(getString(R.string.my_location));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locLatLng, zoom > 2 ? zoom : 15));
            mMap.addMarker(options);


            // there is a new valid location at min. distance from the last one, so invalidate the address.
            mCurrentAddress = null;
            ((TextView)findViewById(R.id.location_info_textview)).setText("new location found, long tap on the map to get address.");

            //TODO: also update Share intent in result receiver?
            if(mShareActionProvider != null)
            {
                mShareActionProvider.setShareIntent(
                        SharingHelper.createShareLocationIntent(mCurrentLocation, mCurrentAddress)
                );
            } else {
                Log.d(LOG_TAG, "ShareActionProvider is null");
            }
        }

    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private boolean locationIsFresh(Location location)
    {
        return location != null &&
                ((new Date().getTime() - location.getTime() <= LOCATION_MAX_AGE) &&
                (location.getAccuracy() <= LOCATION_MAX_RADIUS));

    }


}
