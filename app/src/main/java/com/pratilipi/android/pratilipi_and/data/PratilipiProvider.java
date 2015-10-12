package com.pratilipi.android.pratilipi_and.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.pratilipi.android.pratilipi_and.CardListActivity;

/**
 * Created by Rahul Ranjan on 8/21/2015.
 */
public class PratilipiProvider extends ContentProvider {

    private static final String LOG_TAG = PratilipiProvider.class.getSimpleName();

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private PratilipiDbHelper mOpenHelper;

    static final int HOME_SCREEN_BRIDGE = 100;
//    static final int HOMESCREEN_CONTENT_BY_CATEGORY = 101;
    static final int USER = 200;
    static final int USER_BY_EMAIL = 201;
    static final int CATEGORY = 300;
    static final int CATEGORY_LIST = 301;
    static final int PRATILIPI = 400;
    static final int PRATILIPI_BY_ID = 401;
    static final int CATEGORY_PRATILIPI = 500;
    static final int SHELF = 600;
    static final int CONTENT = 700;

    static UriMatcher buildUriMatcher(){
        final UriMatcher uriMatcher = new UriMatcher( UriMatcher.NO_MATCH );
        final String pratilipiAuthority = PratilipiContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(pratilipiAuthority, PratilipiContract.PATH_HOMESCREEN_BRIDGE, HOME_SCREEN_BRIDGE);
        uriMatcher.addURI( pratilipiAuthority, PratilipiContract.PATH_USER, USER );
        uriMatcher.addURI( pratilipiAuthority, PratilipiContract.PATH_USER + "/*", USER_BY_EMAIL );
        uriMatcher.addURI( pratilipiAuthority, PratilipiContract.PATH_CATEGORY, CATEGORY_LIST);
        uriMatcher.addURI( pratilipiAuthority, PratilipiContract.PATH_CATEGORY + "/*", CATEGORY);
        uriMatcher.addURI( pratilipiAuthority, PratilipiContract.PATH_PRATILIPI, PRATILIPI);
        uriMatcher.addURI( pratilipiAuthority, PratilipiContract.PATH_PRATILIPI + "/*", PRATILIPI_BY_ID);
        uriMatcher.addURI( pratilipiAuthority, PratilipiContract.PATH_CATEGORY_PRATILIPI, CATEGORY_PRATILIPI);
        uriMatcher.addURI( pratilipiAuthority, PratilipiContract.PATH_SHELF, SHELF);
        uriMatcher.addURI( pratilipiAuthority, PratilipiContract.PATH_CONTENT, CONTENT);

        return uriMatcher;
    }


    @Override
    public boolean onCreate() {
        mOpenHelper = new PratilipiDbHelper(getContext());
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsDeleted=0;
        switch (sUriMatcher.match(uri)){
            case HOME_SCREEN_BRIDGE: {
                rowsDeleted = db.delete( PratilipiContract.HomeScreenBridgeEntity.TABLE_NAME, selection, selectionArgs );
                break;
            }
            case CATEGORY_LIST: {
                rowsDeleted = db.delete( PratilipiContract.CategoriesEntity.TABLE_NAME, selection, selectionArgs );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return rowsDeleted;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)){
            case HOME_SCREEN_BRIDGE:{
                retCursor = getPratilipiListByCategory(uri, HOME_SCREEN_BRIDGE);
                break;
            }
            case USER :{
                //TODO : add filter condition as query parameter in uri
                retCursor =  getUser(uri, projection, selection, selectionArgs);
                break;
            }
            case USER_BY_EMAIL :{
                //NOT REQUIRED ACTUALLY AS WE ARE PLANNING TO USE IS_LOGGED_IN = TRUE FILTER IN FUTURE
                retCursor = getUserByEmail(uri, projection);
                break;
            }
            case CATEGORY_LIST :{
                retCursor = getCategoryList(uri, projection);
                break;
            }
            case PRATILIPI :{
                retCursor = getPratilipiListByCategory( uri, CATEGORY_PRATILIPI );
                break;
            }
            case PRATILIPI_BY_ID :{
                retCursor = getPratilipi(uri, null);
                break;
            }
            case SHELF: {
                retCursor = getPratilipiListInShelf();
                break;
            }
            case CONTENT: {
                retCursor = getPratilipiContent( uri );
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

//    private void normalizeDate(ContentValues values) {
//        // normalize the date value
//        if (values.containsKey(PratilipiContract.HomeScreenEntity.COLUMN_DATE)) {
//            long dateValue = values.getAsLong(PratilipiContract.HomeScreenEntity.COLUMN_DATE);
//            values.put(PratilipiContract.HomeScreenEntity.COLUMN_DATE, PratilipiContract.normalizeDate(dateValue));
//        }
//    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri = null;
        switch (match){
            case HOME_SCREEN_BRIDGE:{
                long id = db.insert(PratilipiContract.HomeScreenBridgeEntity.TABLE_NAME, null, values);
                if( id > 0 )
                    Log.d(LOG_TAG, "Inserted Row Id : " + id);
                break;
            }
            case USER :{
                long id = db.insert(PratilipiContract.UserEntity.TABLE_NAME, null, values);
                if( id > 0 ){
                    returnUri = PratilipiContract.UserEntity.getUserUri(String.valueOf(id));
                } else
                    Log.e(LOG_TAG, "User Insert Failed");
                break;
            }
            case CATEGORY_LIST:{
                long id = db.insert(PratilipiContract.CategoriesEntity.TABLE_NAME, null, values);
                if( id > 0 ){
                    returnUri = PratilipiContract.CategoriesEntity.getCategoryUri(String.valueOf(id));
                } else
                    Log.e(LOG_TAG, "Category Insert Failed");
                break;
            }
            case CATEGORY_PRATILIPI :{
                long id = db.insert(PratilipiContract.CategoriesPratilipiEntity.TABLE_NAME, null, values);
                if( id > 0 ){
                    returnUri = PratilipiContract.CategoriesPratilipiEntity.getCategoryPratilipiUri(String.valueOf(id));
                } else
                    Log.e(LOG_TAG, "Category Pratilipi Insert Failed");
                break;
            }
            case SHELF: {
                long id = db.insert(PratilipiContract.ShelfEntity.TABLE_NAME, null, values);
                if( id > 0 ){
                    returnUri = PratilipiContract.ShelfEntity.getShelfEntityUri( String.valueOf(id) );
                } else
                    Log.e(LOG_TAG, "Shelf Insert Failed");
                break;
            }
            case CONTENT: {
                long id = db.insert(PratilipiContract.ContentEntity.TABLE_NAME, null, values);
                if( id > 0 ){
                    returnUri = PratilipiContract.ContentEntity.getContentEntityUri( String.valueOf(id) );
                } else
                    Log.e(LOG_TAG, "Content Insert Failed");
                break;
            }
            case PRATILIPI: {
                long id = db.insert(PratilipiContract.PratilipiEntity.TABLE_NAME, null, values);
                if( id > 0 ){
                    returnUri = PratilipiContract.PratilipiEntity.getPratilipiEntityUri(String.valueOf(id));
                } else
                    Log.e(LOG_TAG, "Content Insert Failed");
                break;
            }
            default:
                throw  new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)){
            case HOME_SCREEN_BRIDGE: {
                db.beginTransaction();
                int rowsInserted = 0;
                try{
                    for( ContentValues value : values ){
                        long _id = db.insert(PratilipiContract.HomeScreenBridgeEntity.TABLE_NAME, null, value);
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
            case CATEGORY_LIST:{
                db.beginTransaction();
                int rowsInserted = 0;
                try{
                    for( ContentValues value : values ){
                        long _id = db.insert(PratilipiContract.CategoriesEntity.TABLE_NAME, null, value);
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
            case PRATILIPI :{
                db.beginTransaction();
                int rowsInserted = 0;
                try{
                    for( ContentValues value : values ){
                        long _id = db.insert(PratilipiContract.PratilipiEntity.TABLE_NAME, null, value);
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
            case CATEGORY_PRATILIPI :{
                db.beginTransaction();
                int rowsInserted = 0;
                try{
                    for( ContentValues value : values ){
                        long _id = db.insert(PratilipiContract.CategoriesPratilipiEntity.TABLE_NAME, null, value);
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
            case SHELF: {
                db.beginTransaction();
                int rowsInserted = 0;
                try{
                    for( ContentValues value : values ){
                        long _id = db.insert(PratilipiContract.ShelfEntity.TABLE_NAME, null, value);
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
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated = 0;
        switch (match){
            case PRATILIPI: {
                rowsUpdated = db.update(PratilipiContract.PratilipiEntity.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case USER:{
                rowsUpdated = db.update(PratilipiContract.UserEntity.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case SHELF: {
                rowsUpdated = db.update( PratilipiContract.ShelfEntity.TABLE_NAME, values, selection, selectionArgs );
                break;
            }
            default: throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return rowsUpdated;
    }

    private Cursor getUser( Uri uri, String[] projection, String selection, String[] selectionArgs ){
        SQLiteQueryBuilder userQuery = new SQLiteQueryBuilder();
        userQuery.setTables(PratilipiContract.UserEntity.TABLE_NAME);

        return userQuery.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null );
    }

    private Cursor getUserByEmail(Uri uri, String[] projection){

        String email = PratilipiContract.UserEntity.getEmailFromUri(uri);
        String selection = PratilipiContract.UserEntity.COLUMN_EMAIL + "=?";
        SQLiteQueryBuilder userQuery = new SQLiteQueryBuilder();
        userQuery.setTables(PratilipiContract.UserEntity.TABLE_NAME);

        return userQuery.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                new String[]{email},
                null,
                null,
                null );
    }

    private Cursor getCategoryList(Uri uri, String[] projection){
        SQLiteQueryBuilder categoryList = new SQLiteQueryBuilder();
        categoryList.setTables(PratilipiContract.CategoriesEntity.TABLE_NAME);

        String languageId = PratilipiContract.CategoriesEntity.getLanguageFromUri(uri);
        String isOnHomeScreen = PratilipiContract.CategoriesEntity.getColumnIsOnHomeScreenFromUri(uri);
        String selection = PratilipiContract.CategoriesEntity.COLUMN_LANGUAGE + "=? and " +
                PratilipiContract.CategoriesEntity.COLUMN_IS_ON_HOME_SCREEN + "=?";
        String[] selectionArgs = {languageId, isOnHomeScreen};
        String sortOrder = PratilipiContract.CategoriesEntity.COLUMN_SORT_ORDER;

        return categoryList.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    private Cursor  getPratilipi(Uri uri, String[] projection){
        SQLiteQueryBuilder pratilipiQuery = new SQLiteQueryBuilder();
        pratilipiQuery.setTables(PratilipiContract.PratilipiEntity.TABLE_NAME);

        String pratilipiId = PratilipiContract.PratilipiEntity.getPratilipiIdFromUri(uri);
        String selection = PratilipiContract.PratilipiEntity.COLUMN_PRATILIPI_ID + "=?";
        String[] selectionArgs = {pratilipiId};

        return pratilipiQuery.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null);
    }

    private Cursor getPratilipiListByCategory(Uri uri, int switchCase){
        String categoryId = null;
        String tableName = null;
        String subQuery = null;
        switch (switchCase){
            case HOME_SCREEN_BRIDGE: {
                tableName = PratilipiContract.HomeScreenBridgeEntity.TABLE_NAME;
                categoryId = PratilipiContract.HomeScreenBridgeEntity.getCategoryIdFromUri(uri);
                subQuery = "select " + PratilipiContract.HomeScreenBridgeEntity.COLUMN_PRATILIPI_ID
                        + " from " + tableName
                        + " where " + PratilipiContract.HomeScreenBridgeEntity.COLUMN_CATEGORY_ID + " = ?";
                break;
            }
            case CATEGORY_PRATILIPI: {
                tableName = PratilipiContract.CategoriesPratilipiEntity.TABLE_NAME;
                categoryId = PratilipiContract.PratilipiEntity.getCategoryIdFromUri(uri);
                subQuery = "select " + PratilipiContract.CategoriesPratilipiEntity.COLUMN_PRATILIPI_ID
                        + " from " + tableName
                        + " where " + PratilipiContract.CategoriesPratilipiEntity.COLUMN_CATEGORY_ID + " = ?";
                break;
            }
        }

        Integer lowerLimit = uri.getQueryParameter(CardListActivity.LOWER_LIMIT) == null ? 0 : Integer.parseInt( uri.getQueryParameter( CardListActivity.LOWER_LIMIT ));
        Integer upperLimit = uri.getQueryParameter(CardListActivity.UPPER_LIMIT) == null ? 50 : Integer.parseInt( uri.getQueryParameter( CardListActivity.UPPER_LIMIT ));


        String rawQuery = "SELECT * FROM "
                            + PratilipiContract.PratilipiEntity.TABLE_NAME + " WHERE "
                            + PratilipiContract.PratilipiEntity.COLUMN_PRATILIPI_ID + " IN ( "
                            + subQuery
                            + " LIMIT "
                            + lowerLimit + ", " + upperLimit
                            + " )";

        return mOpenHelper.getReadableDatabase()
                .rawQuery(rawQuery, new String[]{categoryId});
    }

    private Cursor getPratilipiListInShelf(){

        String query = "SELECT " + PratilipiContract.PratilipiEntity.TABLE_NAME + ".* "
                + " FROM " + PratilipiContract.PratilipiEntity.TABLE_NAME + ", " + PratilipiContract.ShelfEntity.TABLE_NAME
                + " WHERE " + PratilipiContract.PratilipiEntity.TABLE_NAME + "." + PratilipiContract.PratilipiEntity.COLUMN_PRATILIPI_ID
                    + " = " + PratilipiContract.ShelfEntity.TABLE_NAME + "." + PratilipiContract.ShelfEntity.COLUMN_PRATILIPI_ID
                + " ORDER BY " + PratilipiContract.ShelfEntity.COLUMN_LAST_ACCESSED_DATE;

        return mOpenHelper.getReadableDatabase()
                .rawQuery( query, null );
    }

    private Cursor getPratilipiContent(Uri uri){
        SQLiteQueryBuilder pratilipiContent = new SQLiteQueryBuilder();
        pratilipiContent.setTables(PratilipiContract.ContentEntity.TABLE_NAME);

        String pratilipiId = PratilipiContract.ContentEntity.getPratilipiIdFromUri(uri);
        String chapterNumber = PratilipiContract.ContentEntity.getChapterNumberFromUri(uri);
        String pageNumber = PratilipiContract.ContentEntity.getPageNumberFromUri(uri);

        String selection = null;
        String[] selectionArgs = null;

        if(chapterNumber != null){
            selection = PratilipiContract.ContentEntity.COLUMN_PRATILIPI_ID + "=? AND "
                    + PratilipiContract.ContentEntity.COLUMN_CHAPTER_NUMBER + "=?";
            selectionArgs = new String[]{pratilipiId, chapterNumber};
        }

        if(pageNumber != null){
            selection = PratilipiContract.ContentEntity.COLUMN_PRATILIPI_ID + "=? AND "
                    + PratilipiContract.ContentEntity.COLUMN_PAGE_NUMBER + "=?";
            selectionArgs = new String[]{pratilipiId, pageNumber};
        }

        if(selectionArgs != null)
            return pratilipiContent.query(
                    mOpenHelper.getReadableDatabase(),
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );
        else
            return null;
    }

}
