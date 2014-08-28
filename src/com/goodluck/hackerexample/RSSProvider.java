package com.goodluck.hackerexample;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class RSSProvider extends ContentProvider {

    private static final String DATABASE_NAME = "rss.db";
    private static final int DATABASE_VERSION = 1;

    // Handle to a new DatabaseHelper.
    private RSSDBHelper mOpenHelper;

    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return RSSApp.RssItems.CONTENT_TYPE;
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // Opens the database object in "write" mode.
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(RSSApp.RssItems.TABLE_NAME, null, initialValues);
        if (rowId > 0){
            Uri itemUri = ContentUris.withAppendedId(RSSApp.RssItems.CONTENT_ID_URI_BASE,rowId);
            getContext().getContentResolver().notifyChange(itemUri, null);
            return itemUri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    static class RSSDBHelper extends SQLiteOpenHelper {

        private static final String TAG = "RSSDBHelper";

        public RSSDBHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + RSSApp.RssItems.TABLE_NAME + " ("
                    + RSSApp.RssItems._ID + " INTEGER PRIMARY KEY,"
                    + RSSApp.RssItems.COLUMN_NAME_TITLE + " TEXT,"
                    + RSSApp.RssItems.COLUMN_NAME_DESCRIPTION + " TEXT,"
                    + RSSApp.RssItems.COLUMN_NAME_PUBDATE + " TEXT" 
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");

            // Kills the table and existing data
            db.execSQL("DROP TABLE IF EXISTS notes");

            // Recreates the database with a new version
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new RSSDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)){
            orderBy = RSSApp.RssItems.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        Cursor cursor = db.query(RSSApp.RssItems.TABLE_NAME, projection, selection, selectionArgs, null, null,
                orderBy);
        return cursor;
    }

    @Override
    public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
        return 0;
    }

}
