package com.pratilipi.android.pratilipi_and.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * Created by Rahul Ranjan on 8/21/2015.
 */
public class PratilipiProvider extends ContentProvider {

    private static final String LOG_TAG = PratilipiProvider.class.getSimpleName();

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private PratilipiDbHelper mOpenHelper;

    static final int HOMESCREEN = 100;
    static final int HOMESCREEN_CONTENT_BY_CATEGORY = 101;
    private static final String sLanguageAndCategorySelection =
            PratilipiContract.HomeScreenEntity.COLUMN_CATEGORY_ID + "=?" +
                    PratilipiContract.HomeScreenEntity.COLUMN_LANGUAGE_ID + "=?";

    static UriMatcher buildUriMatcher(){
        final UriMatcher uriMatcher = new UriMatcher( UriMatcher.NO_MATCH );
        final String pratilipiAuthority = PratilipiContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(pratilipiAuthority, PratilipiContract.PATH_HOMESCREEN, HOMESCREEN);
        uriMatcher.addURI( pratilipiAuthority, PratilipiContract.PATH_HOMESCREEN + "/*/#", HOMESCREEN_CONTENT_BY_CATEGORY );

        return uriMatcher;
    }

    private Cursor getContentByLanguageAndCategory( Uri uri, String[] projection, String sortOrder){

        String languageId = PratilipiContract.HomeScreenEntity.getLanguageIdFromUri(uri);
        String categoryId = PratilipiContract.HomeScreenEntity.getCategoryIdFromUri(uri);

        SQLiteQueryBuilder contentByCategoryAndLanguage = new SQLiteQueryBuilder();
        contentByCategoryAndLanguage.setTables(PratilipiContract.HomeScreenEntity.TABLE_NAME);

        return contentByCategoryAndLanguage.query( mOpenHelper.getReadableDatabase(),
                projection,
                sLanguageAndCategorySelection,
                new String[]{categoryId, languageId},
                null,
                null,
                null );
    }


    @Override
    public boolean onCreate() {
        mOpenHelper = new PratilipiDbHelper(getContext());
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        Log.e(LOG_TAG, "URI : " + uri);
        Log.e(LOG_TAG, "URI MATCHED WITH : " + sUriMatcher.match(uri));
        switch (sUriMatcher.match(uri)){
            case HOMESCREEN_CONTENT_BY_CATEGORY: {
                retCursor = getContentByLanguageAndCategory(uri, projection, sortOrder);
                break;
            }
            case HOMESCREEN:{
                retCursor = getDistinctCategory(projection);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    private void normalizeDate(ContentValues values) {
        // normalize the date value
        if (values.containsKey(PratilipiContract.HomeScreenEntity.COLUMN_DATE)) {
            long dateValue = values.getAsLong(PratilipiContract.HomeScreenEntity.COLUMN_DATE);
            values.put(PratilipiContract.HomeScreenEntity.COLUMN_DATE, PratilipiContract.normalizeDate(dateValue));
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match){
            case HOMESCREEN:{
                normalizeDate(values);
                long id = db.insert(PratilipiContract.HomeScreenEntity.TABLE_NAME, null, values);
                if( id > 0 )
                    Log.d(LOG_TAG, "Inserted Row Id : " + id);
                break;
            }
            default:
                throw  new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return null;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)){
            case HOMESCREEN: {
                db.beginTransaction();
                int rowsInserted = 0;
                try{
                    for( ContentValues value : values ){
                        normalizeDate(value);
                        long _id = db.insert(PratilipiContract.HomeScreenEntity.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                  db.endTransaction();
                }
                return rowsInserted;
            }
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    public Cursor getDistinctCategory(String[] projection){
        SQLiteQueryBuilder contentByCategoryAndLanguage = new SQLiteQueryBuilder();
        contentByCategoryAndLanguage.setTables(PratilipiContract.HomeScreenEntity.TABLE_NAME);

        return contentByCategoryAndLanguage.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                null,
                null,
                PratilipiContract.HomeScreenEntity.COLUMN_CATEGORY_ID,
                null,
                null);

    }

}
