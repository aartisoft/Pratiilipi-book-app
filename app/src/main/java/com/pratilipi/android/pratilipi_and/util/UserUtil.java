package com.pratilipi.android.pratilipi_and.util;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.pratilipi.android.pratilipi_and.GetCallback;
import com.pratilipi.android.pratilipi_and.data.PratilipiContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by Rahul Ranjan on 9/2/2015.
 */
public class UserUtil {

    private static final String LOG_TAG = UserUtil.class.getSimpleName();

    private final String LOGIN_ENDPOINT = "http://www.pratilipi.com/api.pratilipi/oauth";
    private final String REGISTER_ENDPOINT = "http://www.pratilipi.com/api.pratilipi/register";
    private final String USER_PROFILE_ENDPOINT = "http://www.pratilipi.com/api.pratilipi/userprofile";


    private static final String USER_NAME = "userName";
    private static final String ACCESS_TOKEN_EXPIRY = "expiry";
    private static final String ACCESS_TOKEN = "accessToken";

    private boolean mIsSuccessful;
    private ProgressDialog mProgressDialog;


    public UserUtil(Context context, String processMesssage){
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(processMesssage);
    }

    public void userLogin( HashMap<String, String> requestParams, GetCallback callback){
        mProgressDialog.show();
        new UserLoginAsyncTask(callback).execute(requestParams);
    }

    public void userRegister( HashMap<String, String> requestParams, GetCallback callback){
        mProgressDialog.show();
        new UserRegisterAsyncTask(callback).execute(requestParams);
    }

    public void getUserDetails( String data, GetCallback callback){
        mProgressDialog.show();
        new GetUserProfileAsyncTask(callback).execute(data);
    }

    public static void updateUser(Context context, String email, JSONObject responseJson)
            throws JSONException {
        Log.e(LOG_TAG, "updateUser function called");

        saveAccessToken(context, responseJson.getString(ACCESS_TOKEN), responseJson.getLong(ACCESS_TOKEN_EXPIRY));

        ContentValues values = new ContentValues();
        values.put(PratilipiContract.UserEntity.COLUMN_DISPLAY_NAME, responseJson.getString(USER_NAME));
        values.put(PratilipiContract.UserEntity.COLUMN_IS_LOGGED_IN, 1);

        String where = PratilipiContract.UserEntity.COLUMN_EMAIL + "=?";
        String[] selectionArgs = {email};

        int rowsUpdated = context.getContentResolver().update(PratilipiContract.UserEntity.CONTENT_URI, values, where, selectionArgs);

        if( rowsUpdated == 0 ){
            values.put(PratilipiContract.UserEntity.COLUMN_EMAIL, email);
            insertUser(context, values);
        }

    }

    public static void insertUser( Context context, ContentValues values ) {
        context.getContentResolver().insert(PratilipiContract.UserEntity.CONTENT_URI, values);
    }

    public static boolean saveAccessToken(Context context, String accessToken, Long expiry){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ACCESS_TOKEN, accessToken);
        editor.putString(ACCESS_TOKEN_EXPIRY, expiry.toString());
        return editor.commit();
    }

    public static String getAccessToken(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(ACCESS_TOKEN, null);
    }

    private String makeServerCall (String apiEndpoint, String method, String requestData){
        HttpURLConnection connection = null;
        BufferedReader bufferedReader = null;

        try {
            Uri builtUri = Uri.parse(apiEndpoint).buildUpon()
                    .build();

            URL url = new URL(builtUri.toString());

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);

            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());

            writer.write(requestData);
            writer.flush();

            int status = connection.getResponseCode();
            Log.e(LOG_TAG, "Response Code : " + status);
            InputStream inputStream;
            if(status >= 400){
                Log.e(LOG_TAG, "Error Returned");
                mIsSuccessful = false;
                inputStream = connection.getErrorStream();
            } else{
                mIsSuccessful = true;
                inputStream = connection.getInputStream();
            }

            StringBuffer buffer = new StringBuffer();
            if( inputStream == null ){
                return null;
            }

            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null){
                buffer.append(line + "\n");
            }

            writer.close();
            bufferedReader.close();

            if( buffer.length() == 0 )
                return null;

            Log.w(LOG_TAG, "Api response : " + buffer.toString());
            return buffer.toString();

        } catch (IOException e){
            Log.e(LOG_TAG, "Error ", e);
        }
        return null;
    }

    private class UserLoginAsyncTask extends AsyncTask<HashMap<String, String>, Void, String> {

        private GetCallback callback;

        public UserLoginAsyncTask( GetCallback callback){
            this.callback = callback;
        }

        @Override
        protected String doInBackground(HashMap<String, String>... params) {
            HashMap<String, String> responseMap = HttpUtil.makePOSTRequest(LOGIN_ENDPOINT, params[0]);
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

    private class UserRegisterAsyncTask extends AsyncTask<HashMap<String, String>, Void, String> {

        private GetCallback callback;

        public UserRegisterAsyncTask( GetCallback callback){
            this.callback = callback;
        }

        @Override
        protected String doInBackground(HashMap<String, String>... params){
            try {
                HashMap<String, String> responseMap = HttpUtil.makePUTRequest(REGISTER_ENDPOINT, params[0]);
                mIsSuccessful = Boolean.parseBoolean(responseMap.get(HttpUtil.IS_SUCCESSFUL));
                return responseMap.get(HttpUtil.RESPONSE_STRING);
            } catch (JSONException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String responseString) {
            mProgressDialog.hide();
            mProgressDialog.dismiss();
            callback.done(mIsSuccessful, responseString);
            super.onPostExecute(responseString);
        }
    }

    private class GetUserProfileAsyncTask extends AsyncTask<String, Void, String> {

        private GetCallback callback;

        public GetUserProfileAsyncTask( GetCallback callback){
            this.callback = callback;
        }

        @Override
        protected String doInBackground(String... params) {
            String accessToken = params[0];
            HashMap<String, String> requestParams = new HashMap<>(1);
            requestParams.put(ACCESS_TOKEN, accessToken);
            HashMap<String, String> responseMap = HttpUtil.makeGETRequest(USER_PROFILE_ENDPOINT, requestParams);
            mIsSuccessful = Boolean.getBoolean(responseMap.get(HttpUtil.IS_SUCCESSFUL));
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
}
