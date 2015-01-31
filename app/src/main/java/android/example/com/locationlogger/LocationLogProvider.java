package android.example.com.locationlogger;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;


import android.example.com.locationlogger.LocationContract.LocationEntry;
/**
 * Created by smark on 17-01-2015.
 */
public class LocationLogProvider extends ContentProvider {

    private static final int LOCATION_LOG_DIR = 0;
    private static final int LOCATION_LOG_ITEM = 1;

    private LocationDbHelper mDbHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    @Override
    public boolean onCreate() {

        mDbHelper = new LocationDbHelper(getContext());

        return (mDbHelper != null);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(LocationContract.LocationEntry.TABLE_NAME);

        Cursor logCursor = null;

        switch ( sUriMatcher.match(uri) ) {

            case LOCATION_LOG_DIR:
                logCursor = qb.query(mDbHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
                  break;
            case  LOCATION_LOG_ITEM:
                logCursor = qb.query(mDbHelper.getReadableDatabase(), projection, LocationEntry._ID + " = '"+ ContentUris.parseId(uri)+"'", null, null, null, sortOrder);
                break;
        }

        if( logCursor != null)
        {
            logCursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

            return logCursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri insertUri;

        if(match == LOCATION_LOG_DIR) {
            long _id = db.insert(LocationEntry.TABLE_NAME, null, values);
            if(_id > 0)
                insertUri = ContentUris.withAppendedId( LocationEntry.CONTENT_URI, _id);
            else
                throw new SQLException("insert failed: "+uri);
       } else {
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return insertUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        if(sUriMatcher.match(uri) == LOCATION_LOG_DIR) {
            return mDbHelper.getWritableDatabase().delete(LocationEntry.TABLE_NAME, selection,selectionArgs);
        } else {
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if(sUriMatcher.match(uri) == LOCATION_LOG_DIR) {
            return  mDbHelper.getWritableDatabase().update(LocationEntry.TABLE_NAME, values, selection, selectionArgs);
        } else {
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    private static UriMatcher buildUriMatcher() {

        // if root -> return NO_MATCH:
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // create response codes for the following URIs:
        uriMatcher.addURI( LocationContract.CONTENT_AUTHORITY, LocationContract.PATH_LOCATION, LOCATION_LOG_DIR);
        uriMatcher.addURI( LocationContract.CONTENT_AUTHORITY, LocationContract.PATH_LOCATION +"/#", LOCATION_LOG_ITEM);

        return uriMatcher;
    }
}
