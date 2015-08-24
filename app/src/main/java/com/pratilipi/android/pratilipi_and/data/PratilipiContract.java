package com.pratilipi.android.pratilipi_and.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Created by Rahul Ranjan on 8/21/2015.
 */

public class PratilipiContract {

    public static final String CONTENT_AUTHORITY = "com.pratilipi.android.pratilipi_and.app";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.pratilipi.android.pratilipi_and.app/categories/ is a valid path for
    // looking at weather data. content://com.pratilipi.android.pratilipi_and.app/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.
    public static final String PATH_CATEGORIES = "categories";
    public static final String PATH_HOMESCREEN = "homescreen";
    public static final String PATH_CONTENT = "content";

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    /**
     * Following inner classes defines database entities.
     */

    public static final class CategoriesEntity implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CATEGORIES).build();

        public static final String TABLE_NAME = "categories";

        public static final String COLUMN_CATEGORY_ID = "category_id";
        public static final String COLUMN_CATEGORY_NAME = "category_name";
        public static final String COLUMN_CREATION_DATE = "creation_date";
        public static final String COLUMN_LANGUAGE = "language";


        public static Uri buildLocationUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }

    public static final class HomeScreenEntity implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_HOMESCREEN).build();

        public static final String TABLE_NAME = "homescreen";

        // fields for the database
        public static final String COLUMN_CATEGORY_NAME = "category_name";
        public static final String COLUMN_CATEGORY_ID = "category_id";

        public static final String COLUMN_PRATILIPI_ID = "pratilipi_id";
        public static final String COLUMN_PRATILIPI_TITLE = "pratilipi_title";
        public static final String COLUMN_CONTENT_TYPE = "content_type";
        public static final String COLUMN_COVER_URL = "cover_url";
        public static final String COLUMN_PRICE = "price";

        public static final String COLUMN_LANGUAGE_ID = "language_id";

        public static final String COLUMN_AUTHOR_ID = "author_id";
        public static final String COLUMN_AUTHOR_NAME = "author_name";

        public static final String COLUMN_DATE = "date";


        public static Uri getCategoryWiseContentForHomeScreen(String languageId, String categoryId) {
            return CONTENT_URI.buildUpon().appendPath(languageId)
                    .appendPath(categoryId).build();
        }

        public static String getLanguageIdFromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }

        public static String getCategoryIdFromUri(Uri uri){
            return uri.getPathSegments().get(2);
        }
    }

    public static final class ContentEntity implements BaseColumns {

    }



}
