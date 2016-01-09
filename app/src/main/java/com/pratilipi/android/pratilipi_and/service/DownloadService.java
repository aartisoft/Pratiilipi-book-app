package com.pratilipi.android.pratilipi_and.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.pratilipi.android.pratilipi_and.util.ContentUtil;
import com.pratilipi.android.pratilipi_and.util.HttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Rahul Ranjan on 10/1/2015.
 */
public class DownloadService extends IntentService {

    private static final String LOG_TAG = DownloadService.class.getSimpleName();

    public static final String INTENT_EXTRA_CONTENT_TYPE = "contentType";
    public static final String INTENT_EXTRA_PAGE_NUMBER = "pageNumber";
    public static final String INTENT_EXTRA_CHAPTER_NUMBER = "chapterNumber";
    public static final String INTENT_EXTRA_PRATILIPI_ID = "pratilipiId";
    public static final String INTENT_EXTRA_RECEIVER = "receiver";
    public static final String TEXT_CONTENT_TYPE = "pratilipi";
    public static final String IMAGE_COTENT_TYPE = "image";
    public static final int STATUS_CODE_SUCCESS = 200;
    public static final int STATUS_CODE_FAILURE = 500;

    public DownloadService(){
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String contentType = intent.getStringExtra(INTENT_EXTRA_CONTENT_TYPE);
        String pratilipiId = intent.getStringExtra(INTENT_EXTRA_PRATILIPI_ID);
        int pageNumber = intent.getIntExtra(INTENT_EXTRA_PAGE_NUMBER, 1);
        int chapterNumber = intent.getIntExtra(INTENT_EXTRA_CHAPTER_NUMBER, 1);
        ResultReceiver receiver = (ResultReceiver) intent.getParcelableExtra(INTENT_EXTRA_RECEIVER);

        String uriString;
        if(contentType.equals(IMAGE_COTENT_TYPE))
            uriString = ContentUtil.IMAGE_CONTENT_ENDPOINT;
        else
            uriString = ContentUtil.TEXT_CONTENT_ENDPOINT;

        if(pageNumber == 0 ){
            Log.e(LOG_TAG, "Page Number : " + pageNumber);
            return;
        }
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put(ContentUtil.PRATILIPI_ID, pratilipiId);
        paramMap.put(ContentUtil.CHAPTER_NUMBER, String.valueOf(chapterNumber));
        paramMap.put(ContentUtil.PAGE_NUMBER, String.valueOf(pageNumber));

        HashMap<String, String> responseMap = HttpUtil.makeGETRequest(this, uriString, paramMap);
        if(Boolean.parseBoolean(responseMap.get(HttpUtil.IS_SUCCESSFUL))){
            try {
                JSONObject responseJson = new JSONObject(responseMap.get(HttpUtil.RESPONSE_STRING));
                if( responseJson.has(ContentUtil.PAGE_CONTENT))
                    ContentUtil.insert(this, responseJson.getJSONObject(ContentUtil.PAGE_CONTENT), pratilipiId, String.valueOf(chapterNumber), String.valueOf(pageNumber));

                Bundle bundle = new Bundle();
                bundle.putString(INTENT_EXTRA_PRATILIPI_ID, pratilipiId);
                bundle.putString(INTENT_EXTRA_CHAPTER_NUMBER, String.valueOf(chapterNumber));
                receiver.send(STATUS_CODE_SUCCESS, bundle);
            } catch (JSONException e){
                e.printStackTrace();
                receiver.send(STATUS_CODE_FAILURE, null);
            }
        } else{
            receiver.send(STATUS_CODE_FAILURE, null);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
