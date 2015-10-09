package com.pratilipi.android.pratilipi_and.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.pratilipi.android.pratilipi_and.GetCallback;
import com.pratilipi.android.pratilipi_and.MainActivity;
import com.pratilipi.android.pratilipi_and.data.PratilipiContract;
import com.pratilipi.android.pratilipi_and.datafiles.Pratilipi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by Rahul Ranjan on 9/10/2015.
 */
public class PratilipiUtil {

    private static final String LOG_TAG = PratilipiUtil.class.getSimpleName();

    private static final String PRATILIPI_LIST_ENDPOINT = "http://www.pratilipi.com/api.pratilipi/pratilipi/list";

    public static final String PRATILIPI_LIST = "pratilipiList";

    private static final String PRATILIPI_ID = "id";
    private static final String PRATILIPI_TITLE = "title";
    private static final String PRATILIPI_TITLE_EN = "titleEn";
    private static final String PRATILIPI_TYPE = "type";
    private static final String PRATILIPI_AUTHOR_ID = "authorId";
    private static final String PRATILIPI_AUTHOR_OBJECT = "author";
    private static final String PRATILIPI_AUTHOR_NAME = "name";
    private static final String PRATILIPI_AUTHOR_NAME_EN = "nameEn";
    private static final String PRATILIPI_LANGUAGE_ID = "languageId";
    private static final String PRATILIPI_LANGUAGE_OBJECT = "language";
    private static final String PRATILIPI_LANGUAGE_NAME = "name";
    private static final String PRATILIPI_STATE = "state";
    private static final String PRATILIPI_SUMMARY = "summary";
    private static final String PRATILIPI_INDEX = "index";
    private static final String PRATILIPI_CONTENT_TYPE = "contentType";
    private static final String PRATILIPI_PAGE_COUNT = "pageCount";
    private static final String PRATILIPI_READ_COUNT = "readCount";
    private static final String PRATILIPI_RATING_COUNT = "ratingCount";
    private static final String PRATILIPI_STAR_COUNT = "starCount";
    private static final String PRATILIPI_PRICE = "price";
    private static final String PRATILIPI_DISCOUNTED_PRICE = "discountedPrice";
    private static final String PRATILIPI_COVER_IMAGE_URL = "coverImageUrl";
    private static final String PRATILIPI_GENRE_LIST = "genreNameList";
    private static final String PRATILIPI_LISTING_DATE = "listingDate";

    private Context mContext;
    private boolean mIsSuccessful;
    private ProgressDialog mProgressDialog;

    public PratilipiUtil(Context context, String processMesssage){
        mContext = context;
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(processMesssage);
    }

    public void fetchPratilipiList(HashMap<String, String> requestParams, GetCallback callback){
        mProgressDialog.show();
        new PratilipiListAsyncTask(callback).execute(requestParams);
    }


    private class PratilipiListAsyncTask extends AsyncTask<HashMap<String, String>, Void, String> {

        private GetCallback callback;

        public PratilipiListAsyncTask( GetCallback callback){
            this.callback = callback;
        }

        @Override
        protected String doInBackground(HashMap<String, String>... params) {
            HashMap<String, String> responseMap = HttpUtil.makeGETRequest(mContext, PRATILIPI_LIST_ENDPOINT, params[0]);
            if( responseMap == null )
                return  null;
            mIsSuccessful = Boolean.parseBoolean(responseMap.get(HttpUtil.IS_SUCCESSFUL));
            return responseMap.get(HttpUtil.RESPONSE_STRING);
        }

        @Override
        protected void onPostExecute(String responseString) {
            mProgressDialog.hide();
            mProgressDialog.dismiss();
            callback.done(mIsSuccessful, responseString);
            super.onPostExecute(responseString);
        }
    }

    public static int bulkInsert(Context context, JSONArray pratilipiListArray){
        int rowsInserted = 0;
        try{
            int length = pratilipiListArray.length();
            Vector<ContentValues> pratilipiVector = new Vector<ContentValues>();
            for( int i = 0; i < length; ++i ){
                JSONObject pratilipiObject = pratilipiListArray.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put(PratilipiContract.PratilipiEntity.COLUMN_PRATILIPI_ID, pratilipiObject.getString(PRATILIPI_ID));
                values.put(PratilipiContract.PratilipiEntity.COLUMN_TITLE, pratilipiObject.getString(PRATILIPI_TITLE));
                if( pratilipiObject.has(PRATILIPI_TITLE_EN )) {
                    values.put(PratilipiContract.PratilipiEntity.COLUMN_TITLE_EN, pratilipiObject.getString(PRATILIPI_TITLE_EN));
                }
                values.put(PratilipiContract.PratilipiEntity.COLUMN_TYPE, pratilipiObject.getString(PRATILIPI_TYPE));
                values.put(PratilipiContract.PratilipiEntity.COLUMN_AUTHOR_ID, pratilipiObject.getString(PRATILIPI_AUTHOR_ID));

                if( pratilipiObject.has(PRATILIPI_AUTHOR_OBJECT)) {
                    JSONObject author = pratilipiObject.getJSONObject(PRATILIPI_AUTHOR_OBJECT);
                    if ( !author.has(PRATILIPI_AUTHOR_NAME) || author.getString(PRATILIPI_AUTHOR_NAME).isEmpty())
                        values.put(PratilipiContract.PratilipiEntity.COLUMN_AUTHOR_NAME, author.getString(PRATILIPI_AUTHOR_NAME_EN));
                    else
                        values.put(PratilipiContract.PratilipiEntity.COLUMN_AUTHOR_NAME, author.getString(PRATILIPI_AUTHOR_NAME));
                }

                values.put(PratilipiContract.PratilipiEntity.COLUMN_LANGUAGE_ID, pratilipiObject.getString(PRATILIPI_LANGUAGE_ID));
                if( pratilipiObject.has(PRATILIPI_LANGUAGE_OBJECT)) {
                    JSONObject language = pratilipiObject.getJSONObject(PRATILIPI_LANGUAGE_OBJECT);
                    values.put(PratilipiContract.PratilipiEntity.COLUMN_LANGUAGE_NAME, language.getString(PRATILIPI_LANGUAGE_NAME));
                }

                values.put(PratilipiContract.PratilipiEntity.COLUMN_STATE, pratilipiObject.getString(PRATILIPI_STATE));
                if(pratilipiObject.has(PRATILIPI_SUMMARY))
                    values.put(PratilipiContract.PratilipiEntity.COLUMN_SUMMARY, pratilipiObject.getString(PRATILIPI_SUMMARY));
                if(pratilipiObject.has(PRATILIPI_INDEX))
                    values.put(PratilipiContract.PratilipiEntity.COLUMN_INDEX, pratilipiObject.getString(PRATILIPI_INDEX));
                values.put(PratilipiContract.PratilipiEntity.COLUMN_CONTENT_TYPE, pratilipiObject.getString(PRATILIPI_CONTENT_TYPE));
                values.put(PratilipiContract.PratilipiEntity.COLUMN_RATING_COUNT, pratilipiObject.getInt(PRATILIPI_RATING_COUNT));

                double averageRating = 0;
                if( pratilipiObject.getDouble(PRATILIPI_RATING_COUNT) > 0 )
                    averageRating = pratilipiObject.getDouble(PRATILIPI_STAR_COUNT) / pratilipiObject.getDouble(PRATILIPI_RATING_COUNT);
                values.put(PratilipiContract.PratilipiEntity.COLUMN_AVERAGE_RATING, averageRating);

                values.put(PratilipiContract.PratilipiEntity.COLUMN_PRICE, pratilipiObject.getDouble(PRATILIPI_PRICE));
                values.put(PratilipiContract.PratilipiEntity.COLUMN_DISCOUNTED_PRICE, pratilipiObject.getDouble(PRATILIPI_DISCOUNTED_PRICE));
                values.put(PratilipiContract.PratilipiEntity.COLUMN_PAGE_COUNT, pratilipiObject.getInt(PRATILIPI_PAGE_COUNT));
                values.put(PratilipiContract.PratilipiEntity.COLUMN_READ_COUNT, pratilipiObject.getInt(PRATILIPI_READ_COUNT));
                values.put(PratilipiContract.PratilipiEntity.COLUMN_COVER_IMAGE_URL, pratilipiObject.getString(PRATILIPI_COVER_IMAGE_URL));
                values.put(PratilipiContract.PratilipiEntity.COLUMN_GENRE_NAME_LIST, pratilipiObject.getString(PRATILIPI_GENRE_LIST));
                values.put(PratilipiContract.PratilipiEntity.COLUMN_LISTING_DATE, pratilipiObject.getString(PRATILIPI_LISTING_DATE));
                values.put(PratilipiContract.PratilipiEntity.COLUMN_CREATION_DATE, AppUtil.getCurrentJulianDay());
                values.put(PratilipiContract.PratilipiEntity.COLUMN_DOWNLOAD_STATUS, PratilipiContract.PratilipiEntity.CONTENT_NOT_DOWNLOADED);
                values.put(PratilipiContract.PratilipiEntity.COLUMN_LAST_ACCESSED_ON, AppUtil.getCurrentJulianDay());

                pratilipiVector.add(values);

            }

            if ( pratilipiVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[pratilipiVector.size()];
                pratilipiVector.toArray(cvArray);
                rowsInserted = context.getContentResolver().bulkInsert(PratilipiContract.PratilipiEntity.CONTENT_URI, cvArray);
                Log.e(LOG_TAG, "Number of Rows Inserted : " + rowsInserted);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rowsInserted;
    }

    /**
     * TODO : MOVE THIS FUNCTION TO CATEGORYUTIL FILE
     * @param context
     * @param pratilipiListArray
     * @param categoryId
     * @return
     */
    public static int bulkInsert(Context context, JSONArray pratilipiListArray, String categoryId){
        int rowsInserted = 0;
        try{
            int length = pratilipiListArray.length();
            Vector<ContentValues> categoryPratilipiVector = new Vector<ContentValues>();
            for( int i = 0; i < length; ++i ){
                JSONObject pratilipiObject = pratilipiListArray.getJSONObject(i);

                ContentValues categoryPratilipiValue = new ContentValues();
                categoryPratilipiValue.put(PratilipiContract.CategoriesPratilipiEntity.COLUMN_PRATILIPI_ID, pratilipiObject.getString(PRATILIPI_ID));
                categoryPratilipiValue.put(PratilipiContract.CategoriesPratilipiEntity.COLUMN_CATEGORY_ID, categoryId);
                categoryPratilipiValue.put(PratilipiContract.CategoriesPratilipiEntity.COLUMN_CREATION_DATE, AppUtil.getCurrentJulianDay());
                categoryPratilipiVector.add(categoryPratilipiValue);
            }

            rowsInserted = bulkInsert(context, pratilipiListArray);

            if( rowsInserted > 0 && rowsInserted == pratilipiListArray.length() ){
                ContentValues[] cvArray = new ContentValues[categoryPratilipiVector.size()];
                categoryPratilipiVector.toArray(cvArray);
                if( context instanceof Activity ) {
                    if( ((Activity) context).getClass().getSimpleName().equals(MainActivity.class.getSimpleName())) {
                        rowsInserted = context.getContentResolver().bulkInsert(PratilipiContract.HomeScreenBridgeEntity.CONTENT_URI, cvArray);
                    }
                    else {
                        rowsInserted = context.getContentResolver().bulkInsert(PratilipiContract.CategoriesPratilipiEntity.CONTENT_URI, cvArray);
                    }
                }
                else if( context instanceof Service ) {
                    rowsInserted = context.getContentResolver().bulkInsert(PratilipiContract.HomeScreenBridgeEntity.CONTENT_URI, cvArray);
                }
            } else {
                Log.e(LOG_TAG, "Bulk insert in PratilipiEntity failed");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rowsInserted;
    }

    public static Uri insert(Context context, JSONObject pratilipiObject){

        try {
            ContentValues values = new ContentValues();
            values.put(PratilipiContract.PratilipiEntity.COLUMN_PRATILIPI_ID, pratilipiObject.getString(PRATILIPI_ID));
            values.put(PratilipiContract.PratilipiEntity.COLUMN_TITLE, pratilipiObject.getString(PRATILIPI_TITLE));
            if (pratilipiObject.has(PRATILIPI_TITLE_EN)) {
                values.put(PratilipiContract.PratilipiEntity.COLUMN_TITLE_EN, pratilipiObject.getString(PRATILIPI_TITLE_EN));
            }
            values.put(PratilipiContract.PratilipiEntity.COLUMN_TYPE, pratilipiObject.getString(PRATILIPI_TYPE));
            values.put(PratilipiContract.PratilipiEntity.COLUMN_AUTHOR_ID, pratilipiObject.getString(PRATILIPI_AUTHOR_ID));

            if (pratilipiObject.has(PRATILIPI_AUTHOR_OBJECT)) {
                JSONObject author = pratilipiObject.getJSONObject(PRATILIPI_AUTHOR_OBJECT);
                if (!author.has(PRATILIPI_AUTHOR_NAME) || author.getString(PRATILIPI_AUTHOR_NAME).isEmpty())
                    values.put(PratilipiContract.PratilipiEntity.COLUMN_AUTHOR_NAME, author.getString(PRATILIPI_AUTHOR_NAME_EN));
                else
                    values.put(PratilipiContract.PratilipiEntity.COLUMN_AUTHOR_NAME, author.getString(PRATILIPI_AUTHOR_NAME));
            }

            values.put(PratilipiContract.PratilipiEntity.COLUMN_LANGUAGE_ID, pratilipiObject.getString(PRATILIPI_LANGUAGE_ID));
            if (pratilipiObject.has(PRATILIPI_LANGUAGE_OBJECT)) {
                JSONObject language = pratilipiObject.getJSONObject(PRATILIPI_LANGUAGE_OBJECT);
                values.put(PratilipiContract.PratilipiEntity.COLUMN_LANGUAGE_NAME, language.getString(PRATILIPI_LANGUAGE_NAME));
            }

            values.put(PratilipiContract.PratilipiEntity.COLUMN_STATE, pratilipiObject.getString(PRATILIPI_STATE));
            if (pratilipiObject.has(PRATILIPI_SUMMARY))
                values.put(PratilipiContract.PratilipiEntity.COLUMN_SUMMARY, pratilipiObject.getString(PRATILIPI_SUMMARY));
            if (pratilipiObject.has(PRATILIPI_INDEX))
                values.put(PratilipiContract.PratilipiEntity.COLUMN_INDEX, pratilipiObject.getString(PRATILIPI_INDEX));
            values.put(PratilipiContract.PratilipiEntity.COLUMN_CONTENT_TYPE, pratilipiObject.getString(PRATILIPI_CONTENT_TYPE));
            values.put(PratilipiContract.PratilipiEntity.COLUMN_RATING_COUNT, pratilipiObject.getInt(PRATILIPI_RATING_COUNT));

            double averageRating = 0;
            if (pratilipiObject.getDouble(PRATILIPI_RATING_COUNT) > 0)
                averageRating = pratilipiObject.getDouble(PRATILIPI_STAR_COUNT) / pratilipiObject.getDouble(PRATILIPI_RATING_COUNT);
            values.put(PratilipiContract.PratilipiEntity.COLUMN_AVERAGE_RATING, averageRating);

            values.put(PratilipiContract.PratilipiEntity.COLUMN_PRICE, pratilipiObject.getDouble(PRATILIPI_PRICE));
            values.put(PratilipiContract.PratilipiEntity.COLUMN_DISCOUNTED_PRICE, pratilipiObject.getDouble(PRATILIPI_DISCOUNTED_PRICE));
            values.put(PratilipiContract.PratilipiEntity.COLUMN_PAGE_COUNT, pratilipiObject.getInt(PRATILIPI_PAGE_COUNT));
            values.put(PratilipiContract.PratilipiEntity.COLUMN_READ_COUNT, pratilipiObject.getInt(PRATILIPI_READ_COUNT));
            values.put(PratilipiContract.PratilipiEntity.COLUMN_COVER_IMAGE_URL, pratilipiObject.getString(PRATILIPI_COVER_IMAGE_URL));
            values.put(PratilipiContract.PratilipiEntity.COLUMN_GENRE_NAME_LIST, pratilipiObject.getString(PRATILIPI_GENRE_LIST));
            values.put(PratilipiContract.PratilipiEntity.COLUMN_LISTING_DATE, pratilipiObject.getString(PRATILIPI_LISTING_DATE));
            values.put(PratilipiContract.PratilipiEntity.COLUMN_CREATION_DATE, AppUtil.getCurrentJulianDay());
            values.put(PratilipiContract.PratilipiEntity.COLUMN_DOWNLOAD_STATUS, PratilipiContract.PratilipiEntity.CONTENT_NOT_DOWNLOADED);
            values.put(PratilipiContract.PratilipiEntity.COLUMN_LAST_ACCESSED_ON, AppUtil.getCurrentJulianDay());

            Uri uri = context.getContentResolver().insert(PratilipiContract.PratilipiEntity.CONTENT_URI, values);
            return uri;
        } catch (JSONException e){
            e.printStackTrace();
        }

        return null;
    }

    public static Pratilipi createPratilipiFromJsonObject( JSONObject pratilipiObject ){
        Pratilipi pratilipi = new Pratilipi();
        try {
            pratilipi.setPratilipiId(pratilipiObject.getString(PRATILIPI_ID));
            if( pratilipiObject.has( PRATILIPI_TITLE )) {
                pratilipi.setTitle( pratilipiObject.getString( PRATILIPI_TITLE ) );
            } else if( pratilipiObject.has( PRATILIPI_TITLE_EN ) )
                pratilipi.setTitle(pratilipiObject.getString(PRATILIPI_TITLE_EN));
            pratilipi.setType(pratilipiObject.getString(PRATILIPI_TYPE));
            pratilipi.setAuthorId(pratilipiObject.getString(PRATILIPI_AUTHOR_ID));

            if( pratilipiObject.has( PRATILIPI_AUTHOR_OBJECT )){
                JSONObject author = pratilipiObject.getJSONObject( PRATILIPI_AUTHOR_OBJECT );
                if( author.has( PRATILIPI_AUTHOR_NAME ))
                    pratilipi.setAuthorName( author.getString( PRATILIPI_AUTHOR_NAME ));
                else if( author.has( PRATILIPI_AUTHOR_NAME_EN ))
                    pratilipi.setAuthorName( author.getString( PRATILIPI_AUTHOR_NAME_EN ));
            }

            pratilipi.setLanguageId(pratilipiObject.getString(PRATILIPI_LANGUAGE_ID));
            if( pratilipiObject.has(PRATILIPI_LANGUAGE_OBJECT)) {
                JSONObject language = pratilipiObject.getJSONObject(PRATILIPI_LANGUAGE_OBJECT);
                pratilipi.setLanguageName( language.getString(PRATILIPI_LANGUAGE_NAME) );
            }

            pratilipi.setState(pratilipiObject.getString(PRATILIPI_STATE));
            if( pratilipiObject.has(PRATILIPI_SUMMARY) )
                pratilipi.setSummary( pratilipiObject.getString(PRATILIPI_SUMMARY) );
            if( pratilipiObject.has( PRATILIPI_INDEX ))
                pratilipi.setIndex( pratilipiObject.getString( PRATILIPI_INDEX ));
            pratilipi.setContentType(pratilipiObject.getString(PRATILIPI_CONTENT_TYPE));
            pratilipi.setRatingCount(pratilipiObject.getLong(PRATILIPI_RATING_COUNT));

            double averageRating = 0;
            if( pratilipiObject.getDouble(PRATILIPI_RATING_COUNT) > 0 )
                averageRating = pratilipiObject.getDouble(PRATILIPI_STAR_COUNT) / pratilipiObject.getDouble(PRATILIPI_RATING_COUNT);
            pratilipi.setAverageRating((float) averageRating);

            pratilipi.setPrice(pratilipiObject.getDouble(PRATILIPI_PRICE));
            pratilipi.setDiscountedPrice(pratilipiObject.getDouble(PRATILIPI_DISCOUNTED_PRICE));
            pratilipi.setPageCount(pratilipiObject.getInt(PRATILIPI_PAGE_COUNT));
            pratilipi.setCoverImageUrl(pratilipiObject.getString(PRATILIPI_COVER_IMAGE_URL));
            pratilipi.setGenreList(pratilipiObject.getString(PRATILIPI_GENRE_LIST));
        } catch ( JSONException e ){
            e.printStackTrace();
        }

        return pratilipi;
    }

    public static boolean updatePratilipiDownloadStatus(Context context, String pratilipiId, int downloadStatus){
        ContentValues values = new ContentValues();
        values.put(PratilipiContract.PratilipiEntity.COLUMN_DOWNLOAD_STATUS, downloadStatus);
        String selection = PratilipiContract.PratilipiEntity.COLUMN_PRATILIPI_ID + "=?";
        String[] selectionArgs = new String[]{pratilipiId};
        int updatedRows = context.getContentResolver().update(
                PratilipiContract.PratilipiEntity.CONTENT_URI,
                values,
                selection,
                selectionArgs
        );
        if(updatedRows ==1){
            return true;
        }
        return false;
    }
}
