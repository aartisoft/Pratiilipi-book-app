package com.pratilipi.android.pratilipi_and.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.pratilipi.android.pratilipi_and.GetCallback;
import com.pratilipi.android.pratilipi_and.UserLoginActivity;
import com.pratilipi.android.pratilipi_and.data.PratilipiContract;
import com.pratilipi.android.pratilipi_and.datafiles.Pratilipi;
import com.pratilipi.android.pratilipi_and.datafiles.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by Rahul Ranjan on 9/26/2015.
 */
public class ShelfUtil {

    private static final String LOG_TAG = ShelfUtil.class.getSimpleName();
//    private static final String SHELF_ENDPOINT = "http://mark-4p47.www.prod-pratilipi.appspot.com/api.pratilipi/userpratilipi/library";
    private static final String SHELF_ENDPOINT = "http://www.pratilipi.com/api.pratilipi/userpratilipi/library";

    private static final String PRATILIPI_ID = "pratilipiId";
    private static final String ADD_TO_LIB = "addToLib";

    public static final String PRATILIPI_DATA_LIST = "pratilipiDataList";
    public static final String PRATILIPI_ID_KEY = "id";
    public static final String NUMBER_OF_CONTENT_IN_SHELF = "numberOfContentInLib";

    private static Context mContext;

    public static void getShelfPratilipiListFromServer(Context context, GetCallback callback){
        mContext = context;
        new GetShelfAsyncTask(callback).execute();
    }

    public static void addPratilipiToShelf(Context context, Pratilipi pratilipi, GetCallback callback){
        mContext = context;
        User user = UserUtil.getLoggedInUser(context);

        if(user == null){
            Intent intent = new Intent(context, UserLoginActivity.class);
            context.startActivity(intent);
            return;
        }

        ContentValues values = new ContentValues();
        values.put(PratilipiContract.ShelfEntity.COLUMN_USER_EMAIL, user.getEmail());
        values.put(PratilipiContract.ShelfEntity.COLUMN_PRATILIPI_ID, pratilipi.getPratilipiId());
        values.put(PratilipiContract.ShelfEntity.COLUMN_CREATION_DATE, AppUtil.getCurrentJulianDay());
        values.put(PratilipiContract.ShelfEntity.COLUMN_LAST_ACCESSED_DATE, System.currentTimeMillis());

        Uri uri = context.getContentResolver().insert(
                PratilipiContract.ShelfEntity.CONTENT_URI, values );

        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put(PRATILIPI_ID, pratilipi.getPratilipiId());
        paramsMap.put(ADD_TO_LIB, String.valueOf(true));

        new PutToShelfAsyncTask(callback).execute(paramsMap);
    }

    public static void removePratilipiFromShelf(Context context, Pratilipi pratilipi, GetCallback callback) {

        //REMOVE PRATILIPI FROM SHELF ENTITY
        mContext = context;
        User user = UserUtil.getLoggedInUser(context);

        if(user == null){
            Log.e(LOG_TAG, "User is not logged in");
        }

        int deleteRowCount = delete(context, pratilipi.getPratilipiId());
        if(deleteRowCount == 0){
            Log.e(LOG_TAG, "ShelfEntity deletion failed");
            Toast.makeText(context, "Request failed. Try after some time", Toast.LENGTH_LONG);
            return;
        }

        //MAKE SERVER CALL TO UPDATE SITE DATABASE
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put(PRATILIPI_ID, pratilipi.getPratilipiId());
        paramsMap.put(ADD_TO_LIB, String.valueOf(false));

        new PutToShelfAsyncTask(callback).execute(paramsMap);
    }

    private static class GetShelfAsyncTask extends AsyncTask<Void, Void, String> {

        private GetCallback callback;
        private boolean mIsSuccessful;

        public GetShelfAsyncTask( GetCallback callback){
            this.callback = callback;
        }

        @Override
        protected String doInBackground(Void... params) {
            HashMap<String, String> responseMap = HttpUtil.makeGETRequest(mContext, SHELF_ENDPOINT, null);
            mIsSuccessful = Boolean.valueOf(responseMap.get(HttpUtil.IS_SUCCESSFUL));
            return responseMap.get(HttpUtil.RESPONSE_STRING);
        }

        @Override
        protected void onPostExecute(String responseString) {
            callback.done(mIsSuccessful, responseString);
            super.onPostExecute(responseString);
        }

    }

    private static class PutToShelfAsyncTask extends AsyncTask<HashMap<String, String>, Void, String> {

        private GetCallback callback;
        private boolean mIsSuccessful;

        public PutToShelfAsyncTask( GetCallback callback){
            this.callback = callback;
        }

        @Override
        protected String doInBackground(HashMap<String, String>... params) {
            HashMap<String, String> responseMap = HttpUtil.makePUTRequest(mContext, SHELF_ENDPOINT, params[0]);
            mIsSuccessful = Boolean.valueOf(responseMap.get(HttpUtil.IS_SUCCESSFUL));
            return responseMap.get(HttpUtil.RESPONSE_STRING);
        }

        @Override
        protected void onPostExecute(String responseString) {
            callback.done(mIsSuccessful, responseString);
            super.onPostExecute(responseString);
        }

    }

    public static int bulkInsert(Context context, JSONArray pratilipiListArray){
        try{
            int length = pratilipiListArray.length();
            Vector<ContentValues> vector = new Vector<ContentValues>();
            for( int i = 0; i < length; ++i ){
                JSONObject pratilipiObject = pratilipiListArray.getJSONObject(i);
                Uri uri = PratilipiContract.PratilipiEntity.getPratilipiByIdUri(pratilipiObject.getString(PRATILIPI_ID_KEY));
                Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
                if( !cursor.moveToFirst() ){
                    Uri pratilipiUri = PratilipiUtil.insert(context, pratilipiObject);
                    if( pratilipiUri == null ){
                        Log.e(LOG_TAG, "Unable to insert into PratilipiEntity table.");
                        return 0;
                    }
                }

                ContentValues values = new ContentValues();
                values.put(PratilipiContract.ShelfEntity.COLUMN_PRATILIPI_ID, pratilipiObject.getString(PRATILIPI_ID_KEY));
                values.put(PratilipiContract.ShelfEntity.COLUMN_USER_EMAIL, UserUtil.getLoggedInUser(context).getEmail());
                values.put(PratilipiContract.ShelfEntity.COLUMN_CREATION_DATE, AppUtil.getCurrentJulianDay());
                values.put(PratilipiContract.ShelfEntity.COLUMN_LAST_ACCESSED_DATE, AppUtil.getCurrentJulianDay());
                vector.add(values);
            }

            ContentValues[] contentValuesArray = new ContentValues[vector.size()];
            vector.toArray(contentValuesArray);
            int rowsInserted = context.getContentResolver().bulkInsert(PratilipiContract.ShelfEntity.CONTENT_URI, contentValuesArray);
            return rowsInserted;
        } catch (JSONException e){
            e.printStackTrace();
        }
        return 0;
    }

    public static int delete(Context context, String pratilipiId){
        Uri uri = PratilipiContract.ShelfEntity.CONTENT_URI;
        String selection = PratilipiContract.ShelfEntity.COLUMN_PRATILIPI_ID + "=?";
        String[] selectionArgs = new String[]{pratilipiId};
        return context.getContentResolver().delete(uri, selection, selectionArgs);
    }

}
