package android.example.com.locationlogger;

import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import android.example.com.locationlogger.LocationContract.LocationEntry;

/**
 * Created by smark on 18-01-2015.
 */
public class ListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ListFragment.class.getSimpleName();

    private static final int LOCATION_LOG_LOADER = 0;

    private LocationLogAdapter mLocationLogAdapter;


    public interface OnListItemSelectedListener {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(String address);
    }

    public ListFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.i(LOG_TAG, "onCreateView");
        mLocationLogAdapter = new LocationLogAdapter(getActivity());

        if( getActivity().getIntent().hasExtra(ListActivity.LOCATION_EXTRA) )
        {
            mLocationLogAdapter.setCurrentLocation(getActivity().getIntent().<Location>getParcelableExtra(ListActivity.LOCATION_EXTRA));
        }


        View view = inflater.inflate(R.layout.fragment_list, container, false);

        ListView mListView = (ListView) view.findViewById(R.id.location_log_listview);
        mListView.setAdapter(mLocationLogAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor cursor = mLocationLogAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    ((OnListItemSelectedListener) getActivity())
                            .onItemSelected(cursor.getString(cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_ADDRESS)));
                }
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return false;
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onActivityCreated");

        getLoaderManager().initLoader(LOCATION_LOG_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        Log.i(LOG_TAG, "onResume");
        super.onResume();

        Intent receivedIntent = getActivity().getIntent();


        if(receivedIntent.hasExtra(MainActivity.SHOW_DETAIL_EXTRA))
        {
            // SHOW_DETAIL_EXTRA is set if the MainActivity was launched via a notification.
            // we then want to show the detail activity/fragment
            ((OnListItemSelectedListener)getActivity())
                    .onItemSelected((receivedIntent.getStringExtra(MainActivity.ADDRESS_EXTRA)));
            getActivity().getIntent().removeExtra(MainActivity.SHOW_DETAIL_EXTRA);
        }
    }

    // -- loader callbacks ---------------------------------------------------------------------------------------

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
          getActivity(),
                LocationEntry.CONTENT_URI,
                new String[]{},
                null,
                null,
                LocationEntry.COLUMN_LOCATION_TIMESTAMP+" DESC" // newest first
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mLocationLogAdapter.swapCursor(data);
        //TODO: store position and smooth scroll?
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mLocationLogAdapter.swapCursor(null);
    }
}
