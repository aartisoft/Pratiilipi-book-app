package com.pratilipi.android.pratilipi_and.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Rahul Ranjan on 8/21/2015.
 */
public class PratilipiDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 6;

    static final String DATABASE_NAME = "pratilipi.db";

    public PratilipiDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_CATEGORY_TABLE = "CREATE TABLE " + PratilipiContract.CategoriesEntity.TABLE_NAME + " (" +

                PratilipiContract.CategoriesEntity._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the location entry associated with this weather data
                PratilipiContract.CategoriesEntity.COLUMN_CATEGORY_ID + " INTEGER NOT NULL, " +
                PratilipiContract.CategoriesEntity.COLUMN_CATEGORY_NAME + " TEXT NOT NULL, " +
                PratilipiContract.CategoriesEntity.COLUMN_LANGUAGE + " TEXT NOT NULL, " +
                PratilipiContract.CategoriesEntity.COLUMN_SORT_ORDER + " INTEGER NOT NULL, " +
                PratilipiContract.CategoriesEntity.COLUMN_CREATION_DATE + " INTEGER NOT NULL, " +

                " UNIQUE (" + PratilipiContract.CategoriesEntity.COLUMN_CATEGORY_ID  + ") ON CONFLICT REPLACE" +
                ")";

        final String SQL_CREATE_CATEGORY_PRATILIPI_TABLE = "CREATE TABLE " + PratilipiContract.CategoriesPratilipiEntity.TABLE_NAME + " (" +

                PratilipiContract.CategoriesEntity._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the location entry associated with this weather data
                PratilipiContract.CategoriesPratilipiEntity.COLUMN_CATEGORY_ID + " INTEGER NOT NULL, " +
                PratilipiContract.CategoriesPratilipiEntity.COLUMN_PRATILIPI_ID + " INTEGER NOT NULL, " +
                PratilipiContract.CategoriesPratilipiEntity.COLUMN_CREATION_DATE + " INTEGER NOT NULL, " +

                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + PratilipiContract.CategoriesPratilipiEntity.COLUMN_PRATILIPI_ID + ") REFERENCES " +
                PratilipiContract.PratilipiEntity.TABLE_NAME + " (" + PratilipiContract.PratilipiEntity.COLUMN_PRATILIPI_ID + "), " +
                " FOREIGN KEY (" + PratilipiContract.CategoriesPratilipiEntity.COLUMN_CATEGORY_ID + ") REFERENCES " +
                PratilipiContract.CategoriesEntity.TABLE_NAME + " (" + PratilipiContract.CategoriesEntity.COLUMN_CATEGORY_ID + ") " +

                ")";

        final String SQL_CREATE_HOMESCREEN_TABLE = "CREATE TABLE " + PratilipiContract.HomeScreenEntity.TABLE_NAME + " (" +

                PratilipiContract.HomeScreenEntity._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the location entry associated with this weather data
                PratilipiContract.HomeScreenEntity.COLUMN_PRATILIPI_ID + " INTEGER NOT NULL, " +
                PratilipiContract.HomeScreenEntity.COLUMN_PRATILIPI_TITLE + " TEXT NOT NULL, " +
                PratilipiContract.HomeScreenEntity.COLUMN_CONTENT_TYPE + " TEXT NOT NULL, " +
                PratilipiContract.HomeScreenEntity.COLUMN_COVER_URL + " TEXT NOT NULL, " +
                PratilipiContract.HomeScreenEntity.COLUMN_PRICE + " TEXT NOT NULL, " +
                PratilipiContract.HomeScreenEntity.COLUMN_DISCOUNTED_PRICE + " TEXT NOT NULL, " +

                PratilipiContract.HomeScreenEntity.COLUMN_LANGUAGE_ID + " INTEGER NOT NULL, " +

                PratilipiContract.HomeScreenEntity.COLUMN_AUTHOR_ID + " INTEGER NOT NULL, " +
                PratilipiContract.HomeScreenEntity.COLUMN_AUTHOR_NAME + " TEXT NOT NULL," +

                PratilipiContract.HomeScreenEntity.COLUMN_CATEGORY_ID + " INTEGER NOT NULL, " +
                PratilipiContract.HomeScreenEntity.COLUMN_CATEGORY_NAME + " TEXT NOT NULL, " +
                PratilipiContract.HomeScreenEntity.COLUMN_DATE + " INTEGER NOT NULL " + " )";

        final String SQL_CREATE_USER_TABLE = "CREATE TABLE " + PratilipiContract.UserEntity.TABLE_NAME + " (" +

                PratilipiContract.UserEntity._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the location entry associated with this weather data
                PratilipiContract.UserEntity.COLUMN_DISPLAY_NAME + " TEXT NOT NULL, " +
                PratilipiContract.UserEntity.COLUMN_EMAIL + " TEXT NOT NULL UNIQUE, " +
                PratilipiContract.UserEntity.COLUMN_CONTENTS_IN_SHELF + " INTEGER DEFAULT 0, " +
                PratilipiContract.UserEntity.COLUMN_IS_LOGGED_IN + " INTEGER DEFAULT 0, " +
                PratilipiContract.UserEntity.COLUMN_PROFILE_IMAGE + " TEXT" +
                ")";

        final String SQL_CREATE_PRATILIPI_TABLE = "CREATE TABLE " + PratilipiContract.PratilipiEntity.TABLE_NAME + " (" +

                PratilipiContract.CategoriesEntity._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the location entry associated with this weather data
                PratilipiContract.PratilipiEntity.COLUMN_PRATILIPI_ID + " TEXT NOT NULL, " +
                PratilipiContract.PratilipiEntity.COLUMN_TITLE + " TEXT, " +
                PratilipiContract.PratilipiEntity.COLUMN_TITLE_EN + " TEXT, " +
                PratilipiContract.PratilipiEntity.COLUMN_TYPE + " TEXT NOT NULL, " +
                PratilipiContract.PratilipiEntity.COLUMN_COVER_IMAGE_URL + " TEXT NOT NULL, " +
                PratilipiContract.PratilipiEntity.COLUMN_LANGUAGE_ID + " TEXT NOT NULL, " +
                PratilipiContract.PratilipiEntity.COLUMN_LANGUAGE_NAME + " TEXT NOT NULL, " +
                PratilipiContract.PratilipiEntity.COLUMN_AUTHOR_ID + " TEXT NOT NULL, " +
                PratilipiContract.PratilipiEntity.COLUMN_AUTHOR_NAME + " TEXT NOT NULL, " +
                PratilipiContract.PratilipiEntity.COLUMN_PRICE + " TEXT NOT NULL, " +
                PratilipiContract.PratilipiEntity.COLUMN_DISCOUNTED_PRICE + " TEXT NOT NULL, " +
                PratilipiContract.PratilipiEntity.COLUMN_SUMMARY + " TEXT, " +
                PratilipiContract.PratilipiEntity.COLUMN_INDEX + " TEXT, " +
                PratilipiContract.PratilipiEntity.COLUMN_CONTENT_TYPE + " TEXT NOT NULL, " +
                PratilipiContract.PratilipiEntity.COLUMN_STATE + " TEXT NOT NULL, " +
                PratilipiContract.PratilipiEntity.COLUMN_GENRE_NAME_LIST + " TEXT, " +
                PratilipiContract.PratilipiEntity.COLUMN_PAGE_COUNT + " INTEGER NOT NULL, " +
                PratilipiContract.PratilipiEntity.COLUMN_READ_COUNT + " INTEGER NOT NULL, " +
                PratilipiContract.PratilipiEntity.COLUMN_RATING_COUNT + " INTEGER NOT NULL, " +
                PratilipiContract.PratilipiEntity.COLUMN_AVERAGE_RATING + " REAL NOT NULL, " +
                PratilipiContract.PratilipiEntity.COLUMN_CURRENT_CHAPTER + " INTEGER DEFAULT 0, " +
                PratilipiContract.PratilipiEntity.COLUMN_CURRENT_PAGE + " INTEGER DEFAULT 0, " +
                PratilipiContract.PratilipiEntity.COLUMN_FONT_SIZE + " TEXT, " +
                PratilipiContract.PratilipiEntity.COLUMN_LISTING_DATE + " TEXT, " +
                PratilipiContract.PratilipiEntity.COLUMN_LAST_UPDATED_DATE + " TEXT, " +
                PratilipiContract.PratilipiEntity.COLUMN_CREATION_DATE + " INTEGER NOT NULL, " +
                PratilipiContract.PratilipiEntity.COLUMN_LAST_ACCESSED_ON + " INTEGER, " +

                //To make sure all entries are unique.
                " UNIQUE (" + PratilipiContract.PratilipiEntity.COLUMN_PRATILIPI_ID + ") ON CONFLICT REPLACE" +
                ")";

        sqLiteDatabase.execSQL(SQL_CREATE_HOMESCREEN_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_CATEGORY_PRATILIPI_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_USER_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_CATEGORY_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_PRATILIPI_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PratilipiContract.CategoriesEntity.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PratilipiContract.CategoriesPratilipiEntity.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PratilipiContract.HomeScreenEntity.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PratilipiContract.PratilipiEntity.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PratilipiContract.UserEntity.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
