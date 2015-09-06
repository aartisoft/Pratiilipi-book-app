package com.pratilipi.android.pratilipi_and.util;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.pratilipi.android.pratilipi_and.GetCallback;
import com.pratilipi.android.pratilipi_and.data.PratilipiContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by Rahul Ranjan on 9/6/2015.
 */
public class CategoriesUtil {

    private static final String LOG_TAG = CategoriesUtil.class.getSimpleName();
    private static final String CATEGORIES_ENDPOINT = "http://www.pratilipi.com/api.pratilipi/category";

    private static final String CATEGORY_DATA_LIST = "categoryDataList";
    private static final String CATEGORY_ID = "id";
    private static final String CATEGORY_NAME = "plural";
    private static final String SORT_ORDER = "serialNumber";

    private boolean mIsSuccessful;
    private ProgressDialog mProgressDialog;

    public CategoriesUtil(Context context, String processMessage){
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(processMessage);
    }

    public void fetchCategories( HashMap<String, String> requestParams, GetCallback callback){
        mProgressDialog.show();
        new CategoriesAsyncTask(callback).execute(requestParams);
    }

    private class CategoriesAsyncTask extends AsyncTask<HashMap<String, String>, Void, String> {

        private GetCallback callback;

        public CategoriesAsyncTask( GetCallback callback){
            this.callback = callback;
        }

        @Override
        protected String doInBackground(HashMap<String, String>... params) {
            HashMap<String, String> responseMap = HttpUtil.makeGETRequest(CATEGORIES_ENDPOINT, params[0]);
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

    public int updateCategoriesEntity(Context context, String apiResponseString)
            throws JSONException {
        int rowsInserted = 0;
        Vector<ContentValues> cVVector = new Vector<ContentValues>();

        Date date = new Date();
        long dateInMillis = date.getTime();

        JSONObject apiResposneObject = new JSONObject(apiResponseString);
        JSONArray categoryDataJSONArray = apiResposneObject.getJSONArray(CATEGORY_DATA_LIST);

        int arraySize = categoryDataJSONArray.length();
        for(int i = 0; i < arraySize; ++i){
            JSONObject categoryData = categoryDataJSONArray.getJSONObject(i);
            ContentValues values = new ContentValues();
            values.put(PratilipiContract.CategoriesEntity.COLUMN_CATEGORY_ID, categoryData.getString(CATEGORY_ID));
            values.put(PratilipiContract.CategoriesEntity.COLUMN_CATEGORY_NAME, categoryData.getString(CATEGORY_NAME));
            values.put(PratilipiContract.CategoriesEntity.COLUMN_SORT_ORDER, categoryData.getString(SORT_ORDER));
            values.put(PratilipiContract.CategoriesEntity.COLUMN_LANGUAGE, PratilipiUtil.getPreferredLanguage(context));
            values.put(PratilipiContract.CategoriesEntity.COLUMN_CREATION_DATE, PratilipiUtil.getCurrentJulianDay());

            cVVector.add(values);
        }

        if ( cVVector.size() > 0 ) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            rowsInserted = context.getContentResolver().bulkInsert(PratilipiContract.CategoriesEntity.CONTENT_URI, cvArray);
            Log.e(LOG_TAG, "Number of Rows Inserted : " + rowsInserted);
        }

        if( rowsInserted > 0 ) {
            int rowsDeleted = context.getContentResolver().delete(PratilipiContract.HomeScreenEntity.CONTENT_URI,
                    PratilipiContract.HomeScreenEntity.COLUMN_DATE + "<?",
                    new String[]{String.valueOf(dateInMillis)});
            Log.e(LOG_TAG, "Number of Rows Deleted : " + rowsDeleted);
        }

        return rowsInserted;
    }
}
