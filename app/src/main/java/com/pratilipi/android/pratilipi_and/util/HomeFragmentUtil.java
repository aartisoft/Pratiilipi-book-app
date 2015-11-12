package com.pratilipi.android.pratilipi_and.util;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.pratilipi.android.pratilipi_and.GetCallback;
import com.pratilipi.android.pratilipi_and.data.PratilipiContract;
import com.pratilipi.android.pratilipi_and.data.PratilipiDbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Rahul Ranjan on 9/18/2015.
 */
public class HomeFragmentUtil {

    private static final String LOG_TAG = HomeFragmentUtil.class.getSimpleName();
    private static final String MOBILE_INIT_ENDPOINT = "http://www.pratilipi.com/api.pratilipi/mobileinit";

    private static final String RESPONSE_OBJECT = "response";
    private static final String RESPONSE_ARRAY = "elements";

    private static final String CATEGORY_ID = "id";
    private static final String CATEGORY_NAME = "name";
    private static final String CONTENT = "content";

    private Context mContext;
    private boolean mIsSuccessful;
    private ProgressDialog mProgressDialog;

    public HomeFragmentUtil(Context context, String processMessage){
        mContext = context;
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(processMessage);
    }

    public void fetchHomeFragmentContent( HashMap<String, String> requestParams, GetCallback callback){
        mProgressDialog.show();
        new HomeFragmentAsyncTask(callback).execute(requestParams);
    }

    private class HomeFragmentAsyncTask extends AsyncTask<HashMap<String, String>, Void, String> {

        private GetCallback callback;

        public HomeFragmentAsyncTask( GetCallback callback){
            this.callback = callback;
        }

        @Override
        protected String doInBackground(HashMap<String, String>... params) {
            HashMap<String, String> responseMap = HttpUtil.makeGETRequest(mContext, MOBILE_INIT_ENDPOINT, params[0]);
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

    public static int bulkInsert( Context context, String apiResponseString ){
        int rowsInserted = 0;
        try {
            JSONObject responseJsonObject = new JSONObject(apiResponseString).getJSONObject(RESPONSE_OBJECT);
            JSONArray responseJSONArray = responseJsonObject.getJSONArray(RESPONSE_ARRAY);
            int arraySize = responseJSONArray.length();
            for( int i = 0; i < arraySize; ++i ){
                JSONObject object = responseJSONArray.getJSONObject(i);
                String categoryId = object.getString(CATEGORY_ID);
                String categoryName = object.getString(CATEGORY_NAME);
                ContentValues values = CategoryUtil.createCategoryEntityContentValue(context, categoryId, categoryName, i, 1);
                Uri uri = context.getContentResolver().insert(PratilipiContract.CategoriesEntity.CONTENT_URI, values);
                if( uri != null ){
                    JSONArray contentList = object.getJSONArray(CONTENT);
                    rowsInserted = PratilipiUtil.bulkInsert(context, contentList, categoryId, categoryName);
                    if( rowsInserted == contentList.length() ){
                    } else {
                        Log.e(LOG_TAG, "Pratilipi Insert failed." );
                    }
                } else{
                    Log.e(LOG_TAG, "Category Insert failed." );
                }
            }
        } catch (JSONException e ){
            e.printStackTrace();
        }
        return rowsInserted;
    }

    public static int cleanHomeScreenEntity( Context context ){
        int deletedRows = context.getContentResolver().delete( PratilipiContract.HomeScreenBridgeEntity.CONTENT_URI, null, null );
        Log.v(LOG_TAG, "Clean Home Screen Entity : " + deletedRows);
        return deletedRows;
    }

    public static int cleanCategoryEntity( Context context ){
        Uri uri = PratilipiContract.CategoriesEntity.CONTENT_URI;
        String selection = PratilipiContract.CategoriesEntity.COLUMN_IS_ON_HOME_SCREEN + "=?";
        String[] selectionArgs = {String.valueOf(1)};
        int deletedRows = context.getContentResolver().delete(uri, selection, selectionArgs);
        Log.v( LOG_TAG, "Clean Home Screen Entity : " + deletedRows );
        return deletedRows;
    }

    public static int delete(Context context, String selection, String[] selectionArgs){
        return context.getContentResolver().delete(
                PratilipiContract.HomeScreenBridgeEntity.CONTENT_URI,
                selection,
                selectionArgs);
    }

    public static boolean isTableEmpty(Context context){
        //TODO: GET RID OF rawQuery() FUNCTION. SECURITY HAZARD
        String query = "SELECT * FROM " + PratilipiContract.HomeScreenBridgeEntity.TABLE_NAME;
        Cursor cursor = new PratilipiDbHelper(context).getReadableDatabase().rawQuery(query, null);
        if(cursor.moveToFirst())
            return false;
        else
            return true;
    }

}
