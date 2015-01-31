package android.example.com.locationlogger;

import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.example.com.locationlogger.LocationContract.LocationEntry;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOCATION_LOG_LOADER = 1;

    private String mAddress;

    private Location mCurrentLocation;

    private android.support.v7.widget.ShareActionProvider mShareActionProvider;

    public DetailFragment() {
        // Required empty public constructor
    }

    String getAddress() {return mAddress; }
    public void setAddress(String address){
        mAddress = address;
    }

    public void setLocation(Location location) { mCurrentLocation = location; }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        getLoaderManager().initLoader(LOCATION_LOG_LOADER, null, this);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_detail, menu);

        MenuItem shareMenuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (android.support.v7.widget.ShareActionProvider) MenuItemCompat.getActionProvider(shareMenuItem);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(
          getActivity(),
                LocationEntry.CONTENT_URI,
                new String[]{
                        LocationEntry.COLUMN_LOCATION_ADDRESS,
                        LocationEntry.COLUMN_LOCATION_LATITUDE,
                        LocationEntry.COLUMN_LOCATION_LONGITUDE,
                        LocationEntry.COLUMN_LOCATION_TIMESTAMP
                },
                getAddress() != null ? LocationEntry.COLUMN_LOCATION_ADDRESS + " = '"+getAddress()+"'" : null,
                null,
                null
        );

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data != null && data.moveToFirst())
        {

            String date = Formatter.LocalizedTimestamp( data.getLong(data.getColumnIndex(LocationEntry.COLUMN_LOCATION_TIMESTAMP)) );
            float lat = data.getFloat(data.getColumnIndex(LocationEntry.COLUMN_LOCATION_LATITUDE));
            float lon = data.getFloat(data.getColumnIndex(LocationEntry.COLUMN_LOCATION_LONGITUDE));
            String address = data.getString(data.getColumnIndex(LocationEntry.COLUMN_LOCATION_ADDRESS));
            setAddress(address);
            LatLng latLng = new LatLng(lat,lon);

            ((TextView)getActivity().findViewById(R.id.location_date_textview)).setText(date);
            ((TextView)getActivity().findViewById(R.id.location_address_textview)).setText(mAddress);

            if(mCurrentLocation != null)
            {
                Location location = new Location("dummy");
                location.setLatitude(lat);
                location.setLongitude(lon);

                double dist = mCurrentLocation.distanceTo(location);

                ((TextView)getActivity().findViewById(R.id.location_distance_textview)).setText(Formatter.FormattedDistance(dist));
            }

            GoogleMap mMap = ((SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.detail_map)).getMap();

            float zoom = mMap.getCameraPosition().zoom;
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.my_location));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom > 2 ? zoom : 15));
            mMap.addMarker(options);

            if(mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(
                        SharingHelper.createShareLocationIntent(mCurrentLocation, mAddress)
                );
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    // -- LoadManager.LoaderCallbacks ----------------------------------------------------------------------------------

}
