package android.example.com.locationlogger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.example.com.locationlogger.LocationContract.LocationEntry;

/**
 * Created by smark on 17-01-2015.
 */
class LocationDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "locationlogger.db";

    public LocationDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String logTableCreate = "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
                LocationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + // autoinc. to ensure the _ID stays unique!
                LocationEntry.COLUMN_LOCATION_ADDRESS + " TEXT, " +
                LocationEntry.COLUMN_LOCATION_COMMENT + " TEXT, " +
                LocationEntry.COLUMN_LOCATION_LATITUDE + " REAL NOT NULL, " +
                LocationEntry.COLUMN_LOCATION_LONGITUDE + " REAL NOT NULL, " +
                LocationEntry.COLUMN_LOCATION_TIMESTAMP + " INTEGER NOT NULL);";

        db.execSQL(logTableCreate);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // no plans changing version yet!
    }
}
