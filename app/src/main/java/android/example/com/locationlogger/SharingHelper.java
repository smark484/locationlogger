package android.example.com.locationlogger;

import android.content.Intent;
import android.location.Location;

/**
 * Created by smark on 21-01-2015.
 */
class SharingHelper {

    public static Intent createShareLocationIntent(Location location, String address)
    {
        Intent shareLocationIntent = new Intent(Intent.ACTION_SEND);
        shareLocationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareLocationIntent.setType("text/plain");

        String text = "https://maps.google.com/maps?q=" +
                ((address != null) ? address.replace(" ","+") : location.getLatitude()+"+"+location.getLongitude());
        shareLocationIntent.putExtra(Intent.EXTRA_TEXT, text);

        return shareLocationIntent;
    }
}
