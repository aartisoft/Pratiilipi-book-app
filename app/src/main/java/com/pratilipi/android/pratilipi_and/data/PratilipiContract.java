package com.pratilipi.android.pratilipi_and.data;

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
    public static final String PATH_CATEGORY = "category";
    public static final String PATH_CATEGORY_PRATILIPI = "category_pratilipi";
    public static final String PATH_HOMESCREEN = "homescreen";
    public static final String PATH_USER = "user";
    public static final String PATH_PRATILIPI = "pratilipi";

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
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CATEGORY).build();

        public static final String TABLE_NAME = "category";

        public static final String COLUMN_CATEGORY_ID = "category_id";
        public static final String COLUMN_CATEGORY_NAME = "category_name";
        public static final String COLUMN_LANGUAGE = "language";
        public static final String COLUMN_SORT_ORDER = "sort_order";
        public static final String COLUMN_CREATION_DATE = "creation_date";

        public static Uri getCategoryUri(String string) {
            return CONTENT_URI.buildUpon().appendPath(string)
                    .build();
        }

        public static Uri getCategoryListUri(long language){
            Uri uri = CONTENT_URI
                            .buildUpon()
                            .appendQueryParameter(COLUMN_LANGUAGE, String.valueOf(language))
                            .build();
            return uri;
        }

        public static String getLanguageFromUri(Uri uri){
            return uri.getQueryParameter(COLUMN_LANGUAGE);
        }

    }

    public static final class CategoriesPratilipiEntity implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CATEGORY_PRATILIPI).build();

        public static final String TABLE_NAME = "category_pratilipi";

        public static final String COLUMN_CATEGORY_ID = "category_id";
        public static final String COLUMN_PRATILIPI_ID = "pratilipi_id";
        public static final String COLUMN_CREATION_DATE = "creation_date";

        public static Uri getCategoryPratilipiUri(String categoryPratilipiId){
            return CONTENT_URI.buildUpon().appendPath(categoryPratilipiId).build();
        }

        public static Uri getPratilipiIdListByCategoryUri(String categoryId){
            return CONTENT_URI.buildUpon().appendQueryParameter(COLUMN_CATEGORY_ID, categoryId).build();
        }

        public static String getCategoryIdFromUri(Uri uri){
            return uri.getQueryParameter(COLUMN_CATEGORY_ID);
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
        public static final String COLUMN_DISCOUNTED_PRICE = "discounted_price";

        public static final String COLUMN_LANGUAGE_ID = "language_id";

        public static final String COLUMN_AUTHOR_ID = "author_id";
        public static final String COLUMN_AUTHOR_NAME = "author_name";

        public static final String COLUMN_DATE = "date";


        public static Uri getCategoryWiseContentForHomeScreenUri(String languageId, String categoryId) {
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

    public static final class PratilipiEntity implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PRATILIPI).build();

        public static final String TABLE_NAME = "pratilipi";

        public static final String COLUMN_PRATILIPI_ID = "pratilipi_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_TITLE_EN = "title_en";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_COVER_IMAGE_URL = "cover_image_url";
        public static final String COLUMN_LANGUAGE_ID = "language_id";
        public static final String COLUMN_LANGUAGE_NAME = "language_name";
        public static final String COLUMN_AUTHOR_ID = "author_id";
        public static final String COLUMN_AUTHOR_NAME = "author_name";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_DISCOUNTED_PRICE = "discounted_price";
        public static final String COLUMN_SUMMARY = "summary";
        public static final String COLUMN_INDEX = "content_index";
        public static final String COLUMN_CONTENT_TYPE = "content_type";
        public static final String COLUMN_STATE = "state";
        public static final String COLUMN_GENRE_NAME_LIST = "genre_name_list";
        public static final String COLUMN_PAGE_COUNT = "page_count";
        public static final String COLUMN_READ_COUNT = "read_count";
        public static final String COLUMN_RATING_COUNT = "rating_count";
        public static final String COLUMN_AVERAGE_RATING = "average_rating";
        public static final String COLUMN_CURRENT_CHAPTER = "current_chapter";
        public static final String COLUMN_CURRENT_PAGE = "current_page";
        public static final String COLUMN_FONT_SIZE = "font_size";
        public static final String COLUMN_LISTING_DATE = "listing_date";
        public static final String COLUMN_LAST_UPDATED_DATE = "last_updated_date";
        public static final String COLUMN_CREATION_DATE = "creation_date";
        public static final String COLUMN_LAST_ACCESSED_ON = "last_accessed_on";

        public static Uri getPratilipiByIdUri(String pratilipiId){
            return CONTENT_URI.buildUpon()
                    .appendPath(pratilipiId)
                    .build();
        }

        public static Uri getPratilipiListByCategoryUri(String categoryId){
            return CONTENT_URI.buildUpon()
                    .appendQueryParameter(CategoriesEntity.COLUMN_CATEGORY_ID, categoryId)
                    .build();
        }

        public static String getCategoryIdFromUri(Uri uri){
            return uri.getQueryParameter(CategoriesEntity.COLUMN_CATEGORY_ID);
        }

        public static String getPratilipiIdFromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }

    }

    public static final class UserEntity implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_USER).build();

        public static final String TABLE_NAME = "user";

        public static final String COLUMN_DISPLAY_NAME = "display_name";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_CONTENTS_IN_SHELF = "contents_in_shelf";
        public static final String COLUMN_IS_LOGGED_IN = "is_logged_in";
        public static final String COLUMN_PROFILE_IMAGE = "profile_image";

        public static Uri getUserUri(String string) {
            return CONTENT_URI.buildUpon().appendPath(string)
                    .build();
        }

        public static String getEmailFromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }
    }

}
