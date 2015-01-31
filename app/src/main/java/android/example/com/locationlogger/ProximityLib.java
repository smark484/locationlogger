package android.example.com.locationlogger;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by smark on 25-01-2015.
 */
public class ProximityLib {

    public static LatLngBounds getBounds(Location location, float range)
    {
        double earthRadius = 6374447D; // earth's radius in meters.

        // calculate latitude bounds
        double angularDistance = range / earthRadius ;
        double latitudeOffset = Math.toDegrees(angularDistance) ;
        double north = location.getLatitude() + latitudeOffset ;
        double south = location.getLatitude() - latitudeOffset ;
        boolean wrapNorth = north > 90 ;
        boolean wrapSouth = south < -90 ;
        double northBound = wrapNorth ? 90 : wrapSouth ? Math.max(north, -180 - south) : north;
        double southBound = wrapSouth ? -90 : wrapNorth ? Math.min(south, 180 - north) : south;

        // calculate longitude bounds
        double longitudeOffset = Math.toDegrees( Math.acos(1 - (1 - Math.cos(angularDistance)) / Math.pow(Math.cos(Math.toRadians(location.getLatitude() )), 2) ));


        double eastBound = ( wrapNorth || wrapSouth) ? 180 : location.getLongitude() + longitudeOffset;

        eastBound = eastBound > 180 ? eastBound - 360 : eastBound;

        double westBound = (wrapNorth || wrapSouth) ? -180 : location.getLongitude() - longitudeOffset;

        westBound = westBound < -180 ? westBound + 360 : westBound;

        return LatLngBounds.builder()
                .include(new LatLng(northBound,westBound))
                .include(new LatLng(southBound, eastBound))
                .build();
    }

}
