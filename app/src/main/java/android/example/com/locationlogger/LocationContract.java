package android.example.com.locationlogger;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by smark on 17-01-2015.
 */
public class LocationContract {

    public static final String CONTENT_AUTHORITY = "android.example.com.locationlogger";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_LOCATION = "location";

    public static final class LocationEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();

        public static final String TABLE_NAME = "locations";

        public static final String COLUMN_LOCATION_ADDRESS = "address";

        public static final String COLUMN_LOCATION_COMMENT = "comment";

        public static final String COLUMN_LOCATION_LATITUDE = "latitude";

        public static final String COLUMN_LOCATION_LONGITUDE = "longitude";

        public static final String COLUMN_LOCATION_TIMESTAMP = "timestamp";
    }
}
