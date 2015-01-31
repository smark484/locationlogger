package android.example.com.locationlogger;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class ListActivity extends ActionBarActivity implements ListFragment.OnListItemSelectedListener {

    public static final String LOCATION_EXTRA = "location";

    private static final String LOG_TAG = ListActivity.class.getSimpleName();

    private boolean mHasTwoPanes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(LOG_TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        if(findViewById(R.id.location_detail_container) != null)
        {
            mHasTwoPanes = true;
            if (savedInstanceState == null) {
                DetailFragment fragment = new DetailFragment();
                if(getIntent().hasExtra(LOCATION_EXTRA))
                    fragment.setLocation((Location)getIntent().getParcelableExtra(ListActivity.LOCATION_EXTRA));
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.location_detail_container, fragment)
                        .commit();
            }

        } else {
            mHasTwoPanes = false;
        }
    }

    @Override
    protected void onStart() {
        Log.i(LOG_TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onRestart() {
        Log.i(LOG_TAG, "onRestart");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        Log.i(LOG_TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i(LOG_TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.i(LOG_TAG,"onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i(LOG_TAG,"onDestroy");
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // -- LocationLogFragment listener ---------------------------------------------------------------
    @Override
    public void onItemSelected(String address) {

        if(! mHasTwoPanes)
        {
            // launch detail activity
            Intent locationDetailIntent = new Intent(this, DetailActivity.class);
            locationDetailIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            locationDetailIntent.putExtra(DetailActivity.ADDRESS_EXTRA, address);
            locationDetailIntent.putExtra(DetailActivity.LOCATION_EXTRA, getIntent().getParcelableExtra(ListActivity.LOCATION_EXTRA));
            startActivity(locationDetailIntent);
        } else {

            DetailFragment fragment = new DetailFragment();
            Location location = getIntent().getParcelableExtra(ListActivity.LOCATION_EXTRA);
            fragment.setLocation(location);
            fragment.setAddress(address);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.location_detail_container, fragment)
                    .commit();
        }

    }
}
