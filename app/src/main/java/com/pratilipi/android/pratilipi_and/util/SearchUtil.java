package com.pratilipi.android.pratilipi_and.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.pratilipi.android.pratilipi_and.GetCallback;
import com.pratilipi.android.pratilipi_and.datafiles.Pratilipi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Rahul Ranjan on 9/20/2015.
 */
public class SearchUtil {

    private static final String LOG_TAG = CategoryUtil.class.getSimpleName();
    private static final String SEARCH_ENDPOINT = "http://www.pratilipi.com/api.pratilipi/search";

    public static  final String PRATILIPI_DATA_LIST = "pratilipiDataList";

    private boolean mIsSuccessful;
    private ProgressDialog mProgressDialog;

    public SearchUtil( Context context, String processMessage ){
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage( processMessage );
    }

    public void fetchSearchResults( HashMap<String, String> requestParams, GetCallback callback){
        mProgressDialog.show();
        new SearchAsyncTask(callback).execute(requestParams);
    }

    private class SearchAsyncTask extends AsyncTask<HashMap<String, String>, Void, String> {

        private GetCallback callback;

        public SearchAsyncTask( GetCallback callback){
            this.callback = callback;
        }

        @Override
        protected String doInBackground(HashMap<String, String>... params) {
            HashMap<String, String> responseMap = HttpUtil.makeGETRequest(SEARCH_ENDPOINT, params[0]);
            Log.e( LOG_TAG, "Response Map Length : " + responseMap.size() );
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

    public List<Pratilipi> getPratilipiList( JSONArray responseJsonArray ){
        List<Pratilipi> pratilipiList = new ArrayList<>( responseJsonArray.length() );
        try {
            for (int i = 0; i < responseJsonArray.length(); ++i) {
                JSONObject pratilipiObject = responseJsonArray.getJSONObject(i);
                pratilipiList.add( PratilipiUtil.createPratilipiFromJsonObject( pratilipiObject ));
            }
        } catch ( JSONException e ){
            e.printStackTrace();
        }

        return pratilipiList;
    }

}
