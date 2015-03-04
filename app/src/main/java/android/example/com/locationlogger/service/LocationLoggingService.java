package android.example.com.locationlogger.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.example.com.locationlogger.Formatter;
import android.example.com.locationlogger.MainActivity;
import android.example.com.locationlogger.ProximityLib;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLngBounds;

import android.example.com.locationlogger.LocationContract.LocationEntry;
import android.widget.Toast;

import java.util.Date;

public class LocationLoggingService extends Service   implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener  {

    private static final String LOG_TAG = LocationLoggingService.class.getSimpleName();

    private static final double LOCATION_PROX_DISTANCE = 30D;

    private static final long LOCATION_MAX_AGE = 60000L;

    private static final double LOCATION_MAX_RADIUS = 1000D;

    private static final int LOCATION_UPDATE_INTERVAL = 10000;
    private static final int LOCATION_UPDATE_FASTEST_INTERVAL = 5000;
    private static final float LOCATION_SMALLEST_DISPLACEMENT = 25F;
    private static final int NOTIFICATION_ID = 19116408;

    private GoogleApiClient mGoogleApiClient;

    private static boolean mIsRunning;
    private Location mCurrentLocation;
    private Location mLastProxLocation;

    public LocationLoggingService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(LOG_TAG, "onStartCommand");

//        if(mGoogleApiClient == null)
//            buildGoogleApiClient();
//
//        if(! mIsRunning)
//        {
//            startLocationUpdates();
//        }

        if(mGoogleApiClient != null) {
            stopLocationUpdates();
        }

        buildGoogleApiClient();
        startLocationUpdates();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "onDestroy");
        stopLocationUpdates();
        mIsRunning = false;
        super.onDestroy();
    }

    private void stopLocationUpdates() {
        if(mGoogleApiClient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    private void startLocationUpdates() {
        mGoogleApiClient.connect();
    }

    // --  GoogleApiClient methods ------------------------------------------------------------------------------------
    @Override
    public void onConnected(Bundle bundle) {

        Log.i(LOG_TAG, "onConnected()");

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(location != null && !locationIsStale(location))
        {
            Log.v(LOG_TAG, "Last location is good...");
            onLocationChanged(location);
        }

        LocationRequest request = new LocationRequest()
                .setInterval(LOCATION_UPDATE_INTERVAL)
                .setFastestInterval(LOCATION_UPDATE_FASTEST_INTERVAL)
                .setSmallestDisplacement(LOCATION_SMALLEST_DISPLACEMENT)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, request, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG,"onConnectionSuspended("+i+")");
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
            Log.v(LOG_TAG, "onLocationChanged(acc="+location.getAccuracy()+", time="+ Formatter.XmlTimestamp(location.getTime())+")");
            // search for logged locations nearby and eventually notify user.
            handleNewLocation(location);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(LOG_TAG, "onConnectionFailed()");
    }

    // -- Helper methods ----------------------------------------------------------------------------------
    private void proximityNotifiaction(Context context, Location location) {

       if(mLastProxLocation != null && ( location.distanceTo(mLastProxLocation) < MainActivity.NEW_LOCATION_MIN_DISTANCE ))
           // don't spam notifications!
           return;

        LocationRecord nearest = findNearestLocation(location, 100F);

        if(nearest != null){
            // close location found!
            Intent mainActivityIntent = new Intent( getApplicationContext(), MainActivity.class);

            mainActivityIntent.putExtra(MainActivity.SHOW_DETAIL_EXTRA, "x");
            mainActivityIntent.putExtra(MainActivity.LOCATION_EXTRA, location);
            mainActivityIntent.putExtra(MainActivity.ADDRESS_EXTRA, nearest.Address);

            mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent mainActivityPendingIntent = PendingIntent.getActivity( getApplicationContext(), 0, mainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                    .setSmallIcon(android.R.drawable.stat_sys_warning)
                    .setContentTitle("Location logger")
                    .setContentText("Close to "+ (!nearest.Address.equals("") ? nearest.Address : nearest.Latitude+","+nearest.Longitude))
                    .setContentIntent(mainActivityPendingIntent)
                    .setAutoCancel(true);

            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
            mLastProxLocation = location;
        }
    }


    private LocationRecord findNearestLocation(Location location, float range)
    {
        String nearestAddress = null;

        // get location bounds:
        LatLngBounds mLocationBounds = ProximityLib.getBounds(location, range);


        // cursor to load log entries that are candidates to be close to the current location:
        Cursor cursor = getContentResolver(

        ).query(
                LocationEntry.CONTENT_URI,
                new String[]{
                        LocationEntry.COLUMN_LOCATION_ADDRESS,
                        LocationEntry.COLUMN_LOCATION_COMMENT,
                        LocationEntry.COLUMN_LOCATION_LATITUDE,
                        LocationEntry.COLUMN_LOCATION_LONGITUDE,
                        LocationEntry.COLUMN_LOCATION_TIMESTAMP
                },
//                LocationContract.LocationEntry.COLUMN_LOCATION_LATITUDE + " >= " +mLocationBounds.southwest.latitude + " and " +
//                        LocationContract.LocationEntry.COLUMN_LOCATION_LATITUDE + " <= " +mLocationBounds.northeast.latitude + " and " +
//                        LocationContract.LocationEntry.COLUMN_LOCATION_LONGITUDE + " >= " +mLocationBounds.southwest.latitude + " and " +
//                        LocationContract.LocationEntry.COLUMN_LOCATION_LONGITUDE + " <= " +mLocationBounds.northeast.latitude,
                null,
                null,
                null
        );

        float nearestDist = -1F; // -1 = not found yet
        double nearestLat = 0D, nearestLon = 0D;

        while(cursor != null && cursor.moveToNext())
        {
            double lat = cursor.getDouble( cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_LATITUDE));
            double lon = cursor.getDouble( cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_LONGITUDE));

            float dist[] = new float[]{0};
            Location.distanceBetween(location.getLatitude(),location.getLongitude(), lat, lon, dist);

            if(dist[0] < LOCATION_PROX_DISTANCE){
                if(dist[0] < nearestDist || nearestDist < 0) {
                    nearestDist = dist[0];
                    nearestLat = lat;
                    nearestLon = lon;
                    nearestAddress = cursor.getString( cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_ADDRESS));
                    if(nearestAddress == null)
                        nearestAddress = "";
                }
            }
        }

        if(cursor != null && !cursor.isClosed())
            cursor.close();

        LocationRecord record = null;

        if(nearestDist >= 0)
        {
            record = new LocationRecord();
            record.Address = nearestAddress;
            record.Latitude = nearestLat;
            record.Longitude = nearestLon;
        }

        return record;
    }

    private class LocationRecord {
        double Latitude;
        double Longitude;
        String Address;
    }

    private void handleNewLocation(Location location) {

        if(locationIsStale(location))
        // location is stale - don't bother anyone with it.
            return;

        mCurrentLocation = location;

        LocationRecord nearest = findNearestLocation(location, 100F);

        //  broadcast location updated event:
        Intent locationIntent = new Intent(MainActivity.LOCATION_UPDATED_EVENT);
        locationIntent.putExtra(MainActivity.LOCATION_EXTRA, location);

        if(nearest != null && nearest.Address != null)
        {
            locationIntent.putExtra(MainActivity.ADDRESS_EXTRA, nearest.Address);
        }

        this.sendOrderedBroadcast(locationIntent, null, new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {

                        if(getResultCode() == MainActivity.IS_ALIVE)
                            // main activity is active and has received the event - no need for further actions.
                            return;

                        proximityNotifiaction(context, mCurrentLocation);
                    }
                }, null, 0, null, null
        );

    }




    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private boolean locationIsStale(Location location)
    {
        return location == null ||
                ((new Date().getTime() - location.getTime() > LOCATION_MAX_AGE) ||
                (location.getAccuracy() > LOCATION_MAX_RADIUS));

    }

}
