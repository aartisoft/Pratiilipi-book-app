package com.pratilipi.android.pratilipi_and.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.pratilipi.android.pratilipi_and.data.PratilipiContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Vector;

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

        String languageId = "5130467284090880";

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

            Log.w(LOG_TAG, "Api response : " + buffer.toString());
            getHomescreenData(buffer.toString());

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
        }
    }

    private void getHomescreenData(String initApiResponse){
        final String RESPONSE_OBJECT = "response";
        final String RESPONSE_ARRAY = "elements";

        final String CATEGORY_ID = "id";
        final String CATEGORY_NAME = "name";
        final String CONTENT = "content";

        final String PRATILIPI_ID = "id";
        final String PRATILIPI_TITLE = "title";
        final String PRATILIPI_TITLE_EN = "titleEn";
        final String PRATILIPI_CONTENT_TYPE = "contentType";
        final String PRATILIPI_COVER_URL = "coverImageUrl";
        final String PRATIILPI_PRICE = "price";
        final String PRATILILIPI_DISCOUNTED_PRICE = "discountedPrice";

        final String LANGUAGE_ID = "languageId";

        final String AUTHOR_ID = "authorId";
        final String AUTHOR_FULLNAME = "name";
        final String AUTHOR_FULLNAME_EN = "nameEn";

        try{
            JSONObject resposneJsonObject = new JSONObject(initApiResponse).getJSONObject(RESPONSE_OBJECT);
            JSONArray responsneJSONArray = resposneJsonObject.getJSONArray(RESPONSE_ARRAY);

            Vector<ContentValues> cVVector = new Vector<ContentValues>();

            Date date = new Date();
            long dateInMillis = date.getTime();

            int arraySize = responsneJSONArray.length();
            for( int i = 0; i < arraySize; ++i ){
                JSONObject object = responsneJSONArray.getJSONObject(i);
                ContentValues contentValues = new ContentValues();
                contentValues.put(PratilipiContract.HomeScreenEntity.COLUMN_CATEGORY_ID, object.getString(CATEGORY_ID));
                contentValues.put(PratilipiContract.HomeScreenEntity.COLUMN_CATEGORY_NAME, object.getString(CATEGORY_NAME));
                JSONArray contentList = object.getJSONArray(CONTENT);
                int contentListSize = contentList.length();
                for( int j = 0; j < contentListSize; ++j ){
                    JSONObject contentObject = contentList.getJSONObject(j);
                    contentValues.put(PratilipiContract.HomeScreenEntity.COLUMN_PRATILIPI_ID, contentObject.getLong(PRATILIPI_ID));
                    if( contentObject.getString(PRATILIPI_TITLE) != null )
                        contentValues.put(PratilipiContract.HomeScreenEntity.COLUMN_PRATILIPI_TITLE, contentObject.getString(PRATILIPI_TITLE));
                    else
                        contentValues.put(PratilipiContract.HomeScreenEntity.COLUMN_PRATILIPI_TITLE, contentObject.getString(PRATILIPI_TITLE_EN));
                    contentValues.put(PratilipiContract.HomeScreenEntity.COLUMN_CONTENT_TYPE, contentObject.getString(PRATILIPI_CONTENT_TYPE));
                    contentValues.put(PratilipiContract.HomeScreenEntity.COLUMN_COVER_URL, contentObject.getString(PRATILIPI_COVER_URL));
                    contentValues.put(PratilipiContract.HomeScreenEntity.COLUMN_PRICE, contentObject.getString(PRATIILPI_PRICE));
                    contentValues.put(PratilipiContract.HomeScreenEntity.COLUMN_DISCOUNTED_PRICE, contentObject.getString(PRATILILIPI_DISCOUNTED_PRICE));

                    contentValues.put(PratilipiContract.HomeScreenEntity.COLUMN_AUTHOR_ID, contentObject.getLong(AUTHOR_ID));
                    JSONObject authorJson = contentObject.getJSONObject("author");
                    if( authorJson.getString(AUTHOR_FULLNAME) != null)
                        contentValues.put(PratilipiContract.HomeScreenEntity.COLUMN_AUTHOR_NAME, authorJson.getString(AUTHOR_FULLNAME));
                    else
                        contentValues.put(PratilipiContract.HomeScreenEntity.COLUMN_AUTHOR_NAME, authorJson.getString(AUTHOR_FULLNAME_EN));

                    contentValues.put(PratilipiContract.HomeScreenEntity.COLUMN_LANGUAGE_ID, contentObject.getLong(LANGUAGE_ID));
                    contentValues.put(PratilipiContract.HomeScreenEntity.COLUMN_DATE, dateInMillis);

                    //TODO : INSERT TABLE QUERY.
                    cVVector.add(contentValues);

                }
                Log.w(LOG_TAG, "Vector Size : " + cVVector.size());
            }

            int inserted = 0;
            // add to database
            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                this.getContentResolver().bulkInsert(PratilipiContract.HomeScreenEntity.CONTENT_URI, cvArray);

                //TODO : DELETE OLD RECORDS AND NOTIFY CURSOR ADAPTERS
//                getContext().getContentResolver().delete(PratilipiContract.HomeScreenEntity.CONTENT_URI,
//                        PratilipiContract.HomeScreenEntity.COLUMN_DATE + "<=?",
//                        new String[]{Long.toString(dayTime.setJulianDay(julianStartDay-1))});
//                notifyWeather();
            }

            Log.w(LOG_TAG, "Pratilipi Sync Complete. " + cVVector.size() + " Inserted");

        } catch (JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

    }
}
