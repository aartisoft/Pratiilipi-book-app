package com.pratilipi.android.pratilipi_and.util;

import android.net.Uri;
import android.util.Log;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Rahul Ranjan on 9/3/2015.
 */
public class HttpUtil {

    public static final String IS_SUCCESSFUL = "isSuccessful";
    public static final String RESPONSE_STRING = "responseString";

    private static int sResponseCode = -1;

    private static final String LOG_TAG = HttpUtil.class.getSimpleName();

    public static HashMap<String, String> makeGETRequest(String apiEndpoint, HashMap<String, String> requestParams){
        HttpURLConnection connection = null;
        BufferedReader bufferedReader = null;

        boolean isSuccessful;

        try {

            StringBuilder query;
            if (apiEndpoint.contains("?")) {
                query = new StringBuilder("&");
            } else {
                query = new StringBuilder("?");
            }
            for (Map.Entry<String, String> entry : requestParams.entrySet()) {
                query.append(entry.getKey() + "=" + entry.getValue() + "&");
            }

            Log.e(LOG_TAG, "URL : " + apiEndpoint + query);

            URL url = new URL( apiEndpoint + query );

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int status = connection.getResponseCode();
            Log.e(LOG_TAG, "Response Code : " + status);
            InputStream inputStream;
            if(status >= 400){
                Log.e(LOG_TAG, "Error Returned");
                isSuccessful = false;
                inputStream = connection.getErrorStream();
            } else{
                isSuccessful = true;
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

            bufferedReader.close();

            if( buffer.length() == 0 )
                return null;

            Log.e(LOG_TAG, "Api response : " + buffer.toString());
            HashMap<String, String> returnMap = new HashMap<>(2);
            returnMap.put(IS_SUCCESSFUL, String.valueOf(isSuccessful));
            returnMap.put(RESPONSE_STRING, buffer.toString());
            return returnMap;

        } catch (IOException e){
            Log.e(LOG_TAG, "Error ", e);
        }
        return null;
    }

    public static HashMap<String, String> makePOSTRequest(String apiEndpoint, HashMap<String, String> requestParams){
        HttpURLConnection connection = null;
        BufferedReader bufferedReader = null;

        boolean isSuccessful;

        try {

            Uri builtUri = Uri.parse(apiEndpoint).buildUpon()
                    .build();

            URL url = new URL(builtUri.toString());

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);

            HashMap<String, String> payloadMap = requestParams;
            if (payloadMap != null) {
                String payload = "";
                for (Map.Entry<String, String> entry : payloadMap
                        .entrySet()) {
                    payload += entry.getKey() + "=" + entry.getValue()
                            + "&";
                }
                DataOutputStream wr = new DataOutputStream(
                        connection.getOutputStream());
                wr.writeBytes(payload);
                wr.flush();
                wr.close();
            }

            int status = connection.getResponseCode();
            InputStream inputStream;
            if(status >= 400){
                Log.e(LOG_TAG, "Error Returned");
                isSuccessful = false;
                inputStream = connection.getErrorStream();
            } else{
                isSuccessful = true;
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
            bufferedReader.close();
            Log.v(LOG_TAG, "Api response : " + buffer.toString());

            if( buffer.length() == 0 )
                return null;

            HashMap<String, String> returnMap = new HashMap<>(2);
            returnMap.put(IS_SUCCESSFUL, String.valueOf(isSuccessful));
            returnMap.put(RESPONSE_STRING, buffer.toString());
            return returnMap;

        } catch (IOException e){
            Log.e(LOG_TAG, "Error ", e);
        }
        return null;
    }

    public static HashMap<String, String> makePUTRequest(String apiEndpoint, HashMap<String, String> requestParams) throws JSONException{

        HttpURLConnection connection = null;
        BufferedReader bufferedReader = null;

        boolean isSuccessful;

        try {

            Uri builtUri = Uri.parse(apiEndpoint).buildUpon()
                    .build();

//            JSONObject jsonObject = new JSONObject();
//            for (Map.Entry<String, String> entry : requestParams.entrySet()) {
//                jsonObject.put(entry.getKey(), entry.getValue());
//            }

            String rawFormat = "{";
            for (Map.Entry<String, String> entry : requestParams.entrySet()) {
                rawFormat += "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\", ";
            }
            rawFormat += "}";

            Log.e(LOG_TAG, "Raw Format : " + rawFormat);
            URL url = new URL(builtUri.toString());

            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            Random random = new Random();
            OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            osw.write(String.format(rawFormat, random.nextInt(30), random.nextInt(20)));
            osw.flush();
            osw.close();

            connection.connect();

            Log.e(LOG_TAG, "URL : " + url.toString());
//            Log.e(LOG_TAG, "JSON Object : " + jsonObject.toString());
            sResponseCode = connection.getResponseCode();
            Log.e(LOG_TAG, "Response Code : " + sResponseCode);
            InputStream inputStream;
            if(sResponseCode >= 400){
                Log.e(LOG_TAG, "Error Returned");
                isSuccessful = false;
                inputStream = connection.getErrorStream();
            } else{
                isSuccessful = true;
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
            bufferedReader.close();
            Log.e(LOG_TAG, "Api response : " + buffer.toString());

            if( buffer.length() == 0 )
                return null;

            connection.disconnect();

            HashMap<String, String> returnMap = new HashMap<>(2);
            returnMap.put(IS_SUCCESSFUL, String.valueOf(isSuccessful));
            returnMap.put(RESPONSE_STRING, buffer.toString());
            return returnMap;

        } catch ( IOException e){
            Log.e(LOG_TAG, "Error ", e);
        }
        return null;
    }
}
