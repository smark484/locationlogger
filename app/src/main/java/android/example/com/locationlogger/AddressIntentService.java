package android.example.com.locationlogger;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddressIntentService extends IntentService {

    private static final String LOG_TAG = AddressIntentService.class.getSimpleName();

    private static final String ACTION_FETCH_ADDRESS = "android.example.com.locationlogger.action.FETCH_ADDRESS";

    private static final String EXTRA_LOCATION = "android.example.com.locationlogger.extra.location";

    private static final String EXTRA_RECEIVER = "android.example.com.locationlogger.extra.receiver";

    public static void startActionFetchAddress(Context context, Location location, ResultReceiver receiver) {

        Intent intent = new Intent(context, AddressIntentService.class);
        intent.setAction(ACTION_FETCH_ADDRESS);
        intent.putExtra(EXTRA_LOCATION, location);
        intent.putExtra(EXTRA_RECEIVER, receiver);
        context.startService(intent);
    }

    public AddressIntentService() {
        super("AddressIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.i(LOG_TAG, "onHandleIntent");

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FETCH_ADDRESS.equals(action)) {
                final Location location = intent.getParcelableExtra(EXTRA_LOCATION);
                final ResultReceiver receiver = intent.getParcelableExtra(EXTRA_RECEIVER);
                handleActionFetchAddress(location, receiver);
            }
        }
    }

    private void handleActionFetchAddress(Location location, ResultReceiver receiver) {
        // TODO: Handle action Foo

        int resultCode = Globals.RESULT_OK;

        String address = "";

        try {
            List<Address> addrs = (new Geocoder(this, Locale.getDefault()))
                    .getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if(addrs != null && addrs.size() > 0)
            {
                ArrayList<String> addrParts = new ArrayList<String>();
                for(int i = 0; i < addrs.get(0).getMaxAddressLineIndex(); i++) {
                    addrParts.add(addrs.get(0).getAddressLine(i));
                }

                address = TextUtils.join(", ", addrParts);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "handleActionFetchAddress", e);
            resultCode = Globals.RESULT_ERROR;
        }

        Bundle bundle = new Bundle();
        bundle.putString(Globals.ADDRESS_RESULT, address);
        receiver.send(resultCode, bundle);
    }
}
