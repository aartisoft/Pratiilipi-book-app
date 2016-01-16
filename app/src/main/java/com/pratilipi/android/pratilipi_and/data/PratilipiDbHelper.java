package com.pratilipi.android.pratilipi_and.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Rahul Ranjan on 8/21/2015.
 */
public class PratilipiDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 3;

    static final String DATABASE_NAME = "pratilipi.db";

    public PratilipiDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_CATEGORY_TABLE = "CREATE TABLE " + PratilipiContract.CategoriesEntity.TABLE_NAME + " (" +

                PratilipiContract.CategoriesEntity._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                PratilipiContract.CategoriesEntity.COLUMN_CATEGORY_ID + " INTEGER, " +
                PratilipiContract.CategoriesEntity.COLUMN_CATEGORY_NAME + " TEXT, " +
                PratilipiContract.CategoriesEntity.COLUMN_LANGUAGE + " TEXT NOT NULL, " +
                PratilipiContract.CategoriesEntity.COLUMN_FILTERS + " TEXT, " +
                PratilipiContract.CategoriesEntity.COLUMN_SORT_ORDER + " INTEGER NOT NULL, " +
                PratilipiContract.CategoriesEntity.COLUMN_IS_ON_HOME_SCREEN + " INTEGER NOT NULL, " +
                PratilipiContract.CategoriesEntity.COLUMN_CREATION_DATE + " INTEGER NOT NULL " +
                /**
                 * CATEGORY_ID CANNOT BE UNIQUE AS THIS TABLE IS POPULATED FROM TWO DIFFERENT API
                 * RESPONSE. AND RESULTS OF BOTH APIs MAY NOT BE MUTUALLY EXCLUSIVE.
                 * AND I AM NOT CHECKING FOR THEIR PRESENCE BEFORE INSERTING EACH CATEGORY, SEEMS TO COSTLY
                 * RIGHT NOW.
                 */
                ")";

        final String SQL_CREATE_CATEGORY_PRATILIPI_TABLE = "CREATE TABLE " + PratilipiContract.CategoriesPratilipiEntity.TABLE_NAME + " (" +

                PratilipiContract.CategoriesEntity._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                PratilipiContract.CategoriesPratilipiEntity.COLUMN_CATEGORY_ID + " TEXT, " +
                PratilipiContract.CategoriesPratilipiEntity.COLUMN_CATEGORY_NAME + " TEXT, " +
                PratilipiContract.CategoriesPratilipiEntity.COLUMN_PRATILIPI_ID + " TEXT NOT NULL, " +
                PratilipiContract.CategoriesPratilipiEntity.COLUMN_CREATION_DATE + " INTEGER NOT NULL, " +

                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + PratilipiContract.CategoriesPratilipiEntity.COLUMN_PRATILIPI_ID + ") REFERENCES " +
                PratilipiContract.PratilipiEntity.TABLE_NAME + " (" + PratilipiContract.PratilipiEntity.COLUMN_PRATILIPI_ID + ") " +
                " FOREIGN KEY (" + PratilipiContract.CategoriesPratilipiEntity.COLUMN_CATEGORY_ID + ") REFERENCES " +
                PratilipiContract.CategoriesEntity.TABLE_NAME + " (" + PratilipiContract.CategoriesEntity.COLUMN_CATEGORY_ID + ") " +
                " FOREIGN KEY (" + PratilipiContract.CategoriesPratilipiEntity.COLUMN_CATEGORY_NAME + ") REFERENCES " +
                PratilipiContract.CategoriesEntity.TABLE_NAME + " (" + PratilipiContract.CategoriesEntity.COLUMN_CATEGORY_NAME + ") " +
                " UNIQUE (" + PratilipiContract.CategoriesPratilipiEntity.COLUMN_CATEGORY_ID + ", " +
                PratilipiContract.CategoriesPratilipiEntity.COLUMN_CATEGORY_NAME + ", " +
                PratilipiContract.CategoriesPratilipiEntity.COLUMN_PRATILIPI_ID + ") ON CONFLICT REPLACE" +
                ")";

        final String SQL_CREATE_HOME_SCREEN_BRIDGE_TABLE = "CREATE TABLE " + PratilipiContract.HomeScreenBridgeEntity.TABLE_NAME + " (" +

                PratilipiContract.HomeScreenBridgeEntity._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                PratilipiContract.HomeScreenBridgeEntity.COLUMN_CATEGORY_ID + " TEXT NOT NULL, " +
                PratilipiContract.HomeScreenBridgeEntity.COLUMN_PRATILIPI_ID + " TEXT NOT NULL, " +
                PratilipiContract.HomeScreenBridgeEntity.COLUMN_CREATION_DATE + " INTEGER NOT NULL, " +

                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + PratilipiContract.HomeScreenBridgeEntity.COLUMN_PRATILIPI_ID + ") REFERENCES " +
                PratilipiContract.PratilipiEntity.TABLE_NAME + " (" + PratilipiContract.PratilipiEntity.COLUMN_PRATILIPI_ID + ") " +
                " FOREIGN KEY (" + PratilipiContract.HomeScreenBridgeEntity.COLUMN_CATEGORY_ID + ") REFERENCES " +
                PratilipiContract.CategoriesEntity.TABLE_NAME + " (" + PratilipiContract.CategoriesEntity.COLUMN_CATEGORY_ID + ") " +
                // UNIQUE CATEGORY_ID AND PRATILIPI_ID ARE REQUIRED AS TABLE IS RE-POPULATED EACH TIME
                ")";


        final String SQL_CREATE_USER_TABLE = "CREATE TABLE " + PratilipiContract.UserEntity.TABLE_NAME + " (" +

                PratilipiContract.UserEntity._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                PratilipiContract.UserEntity.COLUMN_DISPLAY_NAME + " TEXT, " +
                PratilipiContract.UserEntity.COLUMN_EMAIL + " TEXT NOT NULL UNIQUE, " +
                PratilipiContract.UserEntity.COLUMN_CONTENTS_IN_SHELF + " INTEGER DEFAULT 0, " +
                PratilipiContract.UserEntity.COLUMN_IS_LOGGED_IN + " INTEGER DEFAULT 0, " +
                PratilipiContract.UserEntity.COLUMN_PROFILE_IMAGE + " TEXT" +
                ")";

        final String SQL_CREATE_PRATILIPI_TABLE = "CREATE TABLE " + PratilipiContract.PratilipiEntity.TABLE_NAME + " (" +

                PratilipiContract.CategoriesEntity._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                PratilipiContract.PratilipiEntity.COLUMN_PRATILIPI_ID + " TEXT NOT NULL, " +
                PratilipiContract.PratilipiEntity.COLUMN_TITLE + " TEXT, " +
                PratilipiContract.PratilipiEntity.COLUMN_TITLE_EN + " TEXT, " +
                PratilipiContract.PratilipiEntity.COLUMN_TYPE + " TEXT NOT NULL, " +
                PratilipiContract.PratilipiEntity.COLUMN_COVER_IMAGE_URL + " TEXT NOT NULL, " +
                PratilipiContract.PratilipiEntity.COLUMN_LANGUAGE_ID + " TEXT, " +
                PratilipiContract.PratilipiEntity.COLUMN_LANGUAGE_NAME + " TEXT, " +
                PratilipiContract.PratilipiEntity.COLUMN_AUTHOR_ID + " TEXT NOT NULL, " +
                PratilipiContract.PratilipiEntity.COLUMN_AUTHOR_NAME + " TEXT, " +
                PratilipiContract.PratilipiEntity.COLUMN_PRICE + " TEXT, " +
                PratilipiContract.PratilipiEntity.COLUMN_DISCOUNTED_PRICE + " TEXT, " +
                PratilipiContract.PratilipiEntity.COLUMN_SUMMARY + " TEXT, " +
                PratilipiContract.PratilipiEntity.COLUMN_INDEX + " TEXT, " +
                PratilipiContract.PratilipiEntity.COLUMN_CONTENT_TYPE + " TEXT, " +
                PratilipiContract.PratilipiEntity.COLUMN_STATE + " TEXT NOT NULL, " +
                PratilipiContract.PratilipiEntity.COLUMN_GENRE_NAME_LIST + " TEXT, " +
                PratilipiContract.PratilipiEntity.COLUMN_PAGE_COUNT + " INTEGER NOT NULL, " +
                PratilipiContract.PratilipiEntity.COLUMN_READ_COUNT + " INTEGER NOT NULL, " +
                PratilipiContract.PratilipiEntity.COLUMN_RATING_COUNT + " INTEGER NOT NULL, " +
                PratilipiContract.PratilipiEntity.COLUMN_AVERAGE_RATING + " REAL NOT NULL, " +
                PratilipiContract.PratilipiEntity.COLUMN_CURRENT_CHAPTER + " INTEGER DEFAULT 1, " +
                PratilipiContract.PratilipiEntity.COLUMN_CURRENT_PAGE + " INTEGER DEFAULT 1, " +
                PratilipiContract.PratilipiEntity.COLUMN_FONT_SIZE + " TEXT DEFAULT '12dp', " +
                PratilipiContract.PratilipiEntity.COLUMN_LISTING_DATE + " TEXT, " +
                PratilipiContract.PratilipiEntity.COLUMN_LAST_UPDATED_DATE + " TEXT, " +
                PratilipiContract.PratilipiEntity.COLUMN_CREATION_DATE + " INTEGER NOT NULL, " +

                PratilipiContract.PratilipiEntity.COLUMN_LAST_ACCESSED_ON + " INTEGER NOT NULL, " +

                //To make sure all entries are unique.
                " UNIQUE (" + PratilipiContract.PratilipiEntity.COLUMN_PRATILIPI_ID + ") ON CONFLICT REPLACE" +
                ")";

        final String SQL_CREATE_SHELF_TABLE = "CREATE TABLE " + PratilipiContract.ShelfEntity.TABLE_NAME + " (" +

                PratilipiContract.ShelfEntity._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                PratilipiContract.ShelfEntity.COLUMN_USER_EMAIL + " TEXT NOT NULL, " +
                PratilipiContract.ShelfEntity.COLUMN_PRATILIPI_ID + " TEXT NOT NULL, " +
                PratilipiContract.ShelfEntity.COLUMN_CREATION_DATE + " INTEGER NOT NULL, " +
                PratilipiContract.ShelfEntity.COLUMN_LAST_ACCESSED_DATE + " INTEGER NOT NULL, " +
                PratilipiContract.ShelfEntity.COLUMN_DOWNLOAD_STATUS + " INTEGER, " +

                // Set up the pratilipi_id column as a foreign key to pratilipi table.
                " FOREIGN KEY (" + PratilipiContract.ShelfEntity.COLUMN_PRATILIPI_ID + ") REFERENCES " +
                PratilipiContract.PratilipiEntity.TABLE_NAME + " (" + PratilipiContract.PratilipiEntity.COLUMN_PRATILIPI_ID + ") " +
                " UNIQUE (" + PratilipiContract.ShelfEntity.COLUMN_PRATILIPI_ID + ") ON CONFLICT REPLACE" +
                ")";

        final String SQL_CREATE_CONTENT_TABLE = "CREATE TABLE " + PratilipiContract.ContentEntity.TABLE_NAME + " (" +

                PratilipiContract.ContentEntity._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                PratilipiContract.ContentEntity.COLUMN_PRATILIPI_ID + " TEXT NOT NULL, " +
                PratilipiContract.ContentEntity.COLUMN_CHAPTER_NUMBER + " INTEGER, " +
                PratilipiContract.ContentEntity.COLUMN_PAGE_NUMBER + " INTEGER, " +
                PratilipiContract.ContentEntity.COLUMN_TEXT_CONTENT + " TEXT, " +
                PratilipiContract.ContentEntity.COLUMN_IMAGE_CONTENT + " BLOB, " +
                PratilipiContract.ContentEntity.COLUMN_LAST_ACCESSED_ON + " INTEGER, " +

                " UNIQUE (" + PratilipiContract.ContentEntity.COLUMN_PRATILIPI_ID +  ", " +
                PratilipiContract.ContentEntity.COLUMN_CHAPTER_NUMBER + ", " +
                PratilipiContract.ContentEntity.COLUMN_PAGE_NUMBER + ") ON CONFLICT REPLACE" +
                ")";

        final String SQL_CREATE_CURSOR_TABLE = "CREATE TABLE " + PratilipiContract.CursorEntity.TABLE_NAME + " (" +

                PratilipiContract.UserEntity._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the location entry associated with this weather data
                PratilipiContract.CursorEntity.COLUMN_LIST_NAME + " TEXT NOT NULL, " +
                PratilipiContract.CursorEntity.COLUMN_CURSOR + " TEXT, " +
                PratilipiContract.CursorEntity.COLUMN_CREATION_DATE + " INTEGER, " +
                " UNIQUE (" + PratilipiContract.CursorEntity.COLUMN_LIST_NAME + ") ON CONFLICT REPLACE" +
                ")";

        sqLiteDatabase.execSQL(SQL_CREATE_CATEGORY_PRATILIPI_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_HOME_SCREEN_BRIDGE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_USER_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_CATEGORY_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_PRATILIPI_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_SHELF_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_CONTENT_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_CURSOR_TABLE);
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
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PratilipiContract.ContentEntity.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PratilipiContract.HomeScreenBridgeEntity.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PratilipiContract.PratilipiEntity.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PratilipiContract.ShelfEntity.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PratilipiContract.UserEntity.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PratilipiContract.CursorEntity.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
