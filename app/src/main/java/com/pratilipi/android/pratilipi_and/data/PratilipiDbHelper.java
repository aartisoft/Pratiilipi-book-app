package com.pratilipi.android.pratilipi_and.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Rahul Ranjan on 8/21/2015.
 */
public class PratilipiDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "pratilipi.db";

    public PratilipiDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE " + PratilipiContract.HomeScreenEntity.TABLE_NAME + " (" +

                PratilipiContract.HomeScreenEntity._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the location entry associated with this weather data
                PratilipiContract.HomeScreenEntity.COLUMN_PRATILIPI_ID + " INTEGER NOT NULL, " +
                PratilipiContract.HomeScreenEntity.COLUMN_PRATILIPI_TITLE + " TEXT NOT NULL, " +
                PratilipiContract.HomeScreenEntity.COLUMN_CONTENT_TYPE + " TEXT NOT NULL, " +
                PratilipiContract.HomeScreenEntity.COLUMN_COVER_URL + " TEXT NOT NULL, " +
                PratilipiContract.HomeScreenEntity.COLUMN_PRICE + " TEXT NOT NULL, " +

                PratilipiContract.HomeScreenEntity.COLUMN_LANGUAGE_ID + " INTEGER NOT NULL, " +

                PratilipiContract.HomeScreenEntity.COLUMN_AUTHOR_ID + " INTEGER NOT NULL, " +
                PratilipiContract.HomeScreenEntity.COLUMN_AUTHOR_NAME + " TEXT NOT NULL," +

                PratilipiContract.HomeScreenEntity.COLUMN_CATEGORY_ID + " INTEGER NOT NULL, " +
                PratilipiContract.HomeScreenEntity.COLUMN_CATEGORY_NAME + " TEXT NOT NULL, " +
                PratilipiContract.HomeScreenEntity.COLUMN_DATE + " INTEGER NOT NULL " + " )";

        sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PratilipiContract.HomeScreenEntity.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
