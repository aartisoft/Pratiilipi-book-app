package com.pratilipi.android.pratilipi_and.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.pratilipi.android.pratilipi_and.GetCallback;
import com.pratilipi.android.pratilipi_and.data.PratilipiContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by Rahul Ranjan on 9/6/2015.
 */
public class CategoryUtil {

    private static final String LOG_TAG = CategoryUtil.class.getSimpleName();
    private static final String CATEGORIES_ENDPOINT = "http://www.pratilipi.com/api.pratilipi/category";

    private static final String CATEGORY_DATA_LIST = "categoryDataList";
    private static final String CATEGORY_ID = "id";
    private static final String CATEGORY_NAME = "plural";
    private static final String SORT_ORDER = "serialNumber";

    public static final String[] CATEGORY_COLUMNS = {
            PratilipiContract.CategoriesEntity._ID,
            PratilipiContract.CategoriesEntity.COLUMN_CATEGORY_ID,
            PratilipiContract.CategoriesEntity.COLUMN_CATEGORY_NAME,
            PratilipiContract.CategoriesEntity.COLUMN_CREATION_DATE,
    };

    public static final int COL_CATEGORY_ID = 1;
    public static final int COL_CATEGORY_NAME = 2;
    public static final int COL_CREATION_DATE = 3;

    private Context mContext;
    private boolean mIsSuccessful;
    private ProgressBar mProgressBar;

    public CategoryUtil(Context context, ProgressBar progressBar){
        mContext = context;
        mProgressBar = progressBar;
    }

    public void fetchCategories( HashMap<String, String> requestParams, GetCallback callback){
        new CategoriesAsyncTask(callback).execute(requestParams);
    }

    private class CategoriesAsyncTask extends AsyncTask<HashMap<String, String>, Void, String> {

        private GetCallback callback;

        public CategoriesAsyncTask( GetCallback callback){
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
            if( mProgressBar != null )
                mProgressBar.setVisibility( View.VISIBLE );
        }

        @Override
        protected String doInBackground(HashMap<String, String>... params) {
            HashMap<String, String> responseMap = HttpUtil.makeGETRequest(mContext, CATEGORIES_ENDPOINT, params[0]);
            if( responseMap == null )
                return null;
            mIsSuccessful = Boolean.parseBoolean(responseMap.get(HttpUtil.IS_SUCCESSFUL));
            return responseMap.get(HttpUtil.RESPONSE_STRING);
        }

        @Override
        protected void onPostExecute(String responseString) {
            if( mProgressBar != null )
                mProgressBar.setVisibility( View.GONE );
            callback.done(mIsSuccessful, responseString);
            super.onPostExecute(responseString);
        }
    }

    public static ContentValues createCategoryEntityContentValue( Context context, String categoryId, String categoryName, int sortOrder, int isOnHomeScreen ){
        ContentValues values = new ContentValues();
        values.put(PratilipiContract.CategoriesEntity.COLUMN_CATEGORY_ID, categoryId);
        values.put(PratilipiContract.CategoriesEntity.COLUMN_CATEGORY_NAME, categoryName);
        values.put(PratilipiContract.CategoriesEntity.COLUMN_SORT_ORDER, sortOrder);
        values.put(PratilipiContract.CategoriesEntity.COLUMN_LANGUAGE, String.valueOf(AppUtil.getPreferredLanguage(context)));
        values.put(PratilipiContract.CategoriesEntity.COLUMN_IS_ON_HOME_SCREEN, isOnHomeScreen);
        values.put(PratilipiContract.CategoriesEntity.COLUMN_CREATION_DATE, AppUtil.getCurrentJulianDay());

        return values;
    }

    public static int bulkInsert(Context context, String apiResponseString)
            throws JSONException {
        int rowsInserted = 0;
        Vector<ContentValues> cVVector = new Vector<ContentValues>();

        JSONObject apiResponseObject = new JSONObject(apiResponseString);
        JSONArray categoryDataJSONArray = apiResponseObject.getJSONArray(CATEGORY_DATA_LIST);

        int arraySize = categoryDataJSONArray.length();
        for(int i = 0; i < arraySize; ++i){
            JSONObject categoryData = categoryDataJSONArray.getJSONObject(i);
            ContentValues values = new ContentValues();
            values.put(PratilipiContract.CategoriesEntity.COLUMN_CATEGORY_ID, categoryData.getString(CATEGORY_ID));
            values.put(PratilipiContract.CategoriesEntity.COLUMN_CATEGORY_NAME, categoryData.getString(CATEGORY_NAME));
            values.put(PratilipiContract.CategoriesEntity.COLUMN_SORT_ORDER, categoryData.getString(SORT_ORDER));
            values.put(PratilipiContract.CategoriesEntity.COLUMN_LANGUAGE, AppUtil.getPreferredLanguage(context));
            values.put(PratilipiContract.CategoriesEntity.COLUMN_IS_ON_HOME_SCREEN, 0);
            values.put(PratilipiContract.CategoriesEntity.COLUMN_CREATION_DATE, AppUtil.getCurrentJulianDay());

            cVVector.add(values);
        }

        if ( cVVector.size() > 0 ) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            rowsInserted = context.getContentResolver().bulkInsert(PratilipiContract.CategoriesEntity.CONTENT_URI, cvArray);
            Log.e(LOG_TAG, "Number Categories Inserted : " + rowsInserted);
        }

        if( rowsInserted > 0 ) {
            int rowsDeleted = context.getContentResolver().delete(PratilipiContract.CategoriesEntity.CONTENT_URI,
                    PratilipiContract.CategoriesEntity.COLUMN_CREATION_DATE + "<? and " + PratilipiContract.CategoriesEntity.COLUMN_IS_ON_HOME_SCREEN + "=?",
                    new String[]{String.valueOf(AppUtil.getCurrentJulianDay()), String.valueOf(0)});
            Log.e(LOG_TAG, "Number Categories Deleted : " + rowsDeleted);
        }

        return rowsInserted;
    }

    public static Cursor getCategoryList( Context context, int isOnHomeScreen ){
        Long languageId = AppUtil.getPreferredLanguage( context );
        Uri uri = PratilipiContract.CategoriesEntity.getCategoryListUri( languageId, isOnHomeScreen );

        return context.getContentResolver().query(
                uri,
                CATEGORY_COLUMNS,
                null,
                null,
                PratilipiContract.CategoriesEntity.COLUMN_SORT_ORDER );
    }
}
