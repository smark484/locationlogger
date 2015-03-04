package android.example.com.locationlogger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by smark on 18-01-2015.
 */
public class Formatter {
    private static final DateFormat sDateFormat = DateFormat.getDateTimeInstance();
    private static final SimpleDateFormat sSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private static final SimpleDateFormat sDurationFormat = new SimpleDateFormat("HH:mm:ss");

    public static String FormattedDuaration(long duration)
    {
        return sDurationFormat.format(new Date(duration));
    }

    public static String FormattedDistance(double distance)
    {
        String metersFormat = "%5.1f m";
        String kmFormat = "%6.1f km";
        if(distance < 1000D)
            return String.format(metersFormat, distance);
        else
            return String.format(kmFormat, distance / 1000D);

    }

    public static String LocalizedTimestamp(long time)
    {
        return sDateFormat.format(new Date(time));
    }

    public static String XmlTimestamp(long time)
    {
        return sSimpleDateFormat.format(new Date(time));
    }
}
