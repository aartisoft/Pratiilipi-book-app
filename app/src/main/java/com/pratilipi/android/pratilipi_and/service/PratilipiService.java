package com.pratilipi.android.pratilipi_and.service;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.pratilipi.android.pratilipi_and.util.AppUtil;
import com.pratilipi.android.pratilipi_and.util.HomeFragmentUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Rahul Ranjan on 8/24/2015.
 */
public class PratilipiService extends IntentService {

    private static final String LOG_TAG = PratilipiService.class.getSimpleName();

    public PratilipiService() {
        super("Pratilipi");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.w(LOG_TAG, "onHandleIntent called");

        HttpURLConnection connection = null;
        BufferedReader bufferedReader = null;

        String languageId = String.valueOf(AppUtil.getPreferredLanguage(this));

        try{
            String apiUrl = "http://www.pratilipi.com/api.pratilipi/mobileinit?";

            Uri builtUri = Uri.parse(apiUrl).buildUpon()
                    .appendQueryParameter("languageId", languageId)
                    .build();

            URL url = new URL(builtUri.toString());

            connection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = connection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if( inputStream == null ){
                return;
            }

            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null){
                buffer.append(line + "\n");
            }

            if( buffer.length() == 0 )
                return;

            HomeFragmentUtil.cleanHomeScreenEntity( this );
            HomeFragmentUtil.cleanCategoryEntity( this );
            HomeFragmentUtil.bulkInsert( this, buffer.toString());

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

}
