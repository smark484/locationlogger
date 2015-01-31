package android.example.com.locationlogger;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import android.example.com.locationlogger.LocationContract.LocationEntry;

/**
 * Created by smark on 18-01-2015.
 */
class LocationLogAdapter extends CursorAdapter {

    private Location mCurrentLocation;

    public static class ViewHolder {
        public final TextView distanceView;
        public final TextView dateView;
        public final TextView addressView;

        public ViewHolder(View view){
            distanceView = (TextView)view.findViewById(R.id.list_item_distance_textview);
            dateView = (TextView)view.findViewById(R.id.list_item_date_textview);
            addressView = (TextView)view.findViewById(R.id.list_item_address_textview);
        }
    }

    public LocationLogAdapter(Context context) {
        super(context, null, 0);
    }

    public void setCurrentLocation(Location location){ mCurrentLocation = location; }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.list_item_location, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder)view.getTag();

        // location date:
        String dateString = Formatter.LocalizedTimestamp( cursor.getLong(cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_TIMESTAMP) ));

        // location latitude:
        double lat = cursor.getFloat(cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_LATITUDE));

        // location longitude:
        double lon = cursor.getFloat(cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_LONGITUDE));

        // location address:
        String address = cursor.getString(cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_ADDRESS));

        if(mCurrentLocation != null)
        {
            Location loc = new Location(mCurrentLocation);
            loc.setLatitude(lat);
            loc.setLongitude(lon);
            float dist = mCurrentLocation.distanceTo(loc);
            viewHolder.distanceView.setText(Formatter.FormattedDistance(dist));
        }

        viewHolder.dateView.setText( dateString );
        viewHolder.addressView.setText( address );
    }
}
