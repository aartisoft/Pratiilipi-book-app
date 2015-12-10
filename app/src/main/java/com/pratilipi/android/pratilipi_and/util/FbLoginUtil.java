package com.pratilipi.android.pratilipi_and.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.facebook.login.LoginManager;
import com.pratilipi.android.pratilipi_and.GetCallback;

import java.util.HashMap;

/**
 * Created by Rahul Ranjan on 12/9/2015.
 */
public class FbLoginUtil {

    private final String LOG_TAG = FbLoginUtil.class.getSimpleName();
    private final String LOGIN_ENDPOINT = "http://android.pratilipi.com/user/login/facebook";

    public static String FB_ACCESS_TOKEN_PARAM = "fbUserAccessToken";

    private Context mContext;
    private Boolean mIsSuccessful;

    public FbLoginUtil(Context context){
        mContext = context;
    }

    public void fbUserLogin(HashMap<String, String> requestParams, GetCallback callback){
        Log.e(LOG_TAG, "Making server call for fbLogin");
        new FbUserLoginAsyncTask(callback).execute(requestParams);
    }

    private class FbUserLoginAsyncTask extends AsyncTask<HashMap<String, String>, Void, String> {

        private GetCallback callback;

        public FbUserLoginAsyncTask( GetCallback callback){
            this.callback = callback;
        }

        @Override
        protected String doInBackground(HashMap<String, String>... params) {
            HashMap<String, String> responseMap = HttpUtil.makePOSTRequest(mContext, LOGIN_ENDPOINT, params[0]);
            mIsSuccessful = Boolean.parseBoolean(responseMap.get(HttpUtil.IS_SUCCESSFUL));
            return responseMap.get(HttpUtil.RESPONSE_STRING);
        }

        @Override
        protected void onPostExecute(String responseString) {
            callback.done(mIsSuccessful, responseString);
            super.onPostExecute(responseString);
        }
    }

    public static void facebookLogout(){
        LoginManager.getInstance().logOut();
    }

}
