package com.pratilipi.android.pratilipi_and.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Rahul Ranjan on 9/3/2015.
 */
public class HttpUtil {

    public static final String IS_SUCCESSFUL = "isSuccessful";
    public static final String RESPSONE_CODE = "resposneCode";
    public static final String RESPONSE_STRING = "responseString";

    private static final String ACCESS_TOKEN = "accessToken";

    private static int sResponseCode = -1;
    private static String sResponseString;

    private static final String LOG_TAG = HttpUtil.class.getSimpleName();

    public static HashMap<String, String> makeGETRequest(Context context, String apiEndpoint, HashMap<String, String> requestParams){
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
            if(requestParams != null) {
                for (Map.Entry<String, String> entry : requestParams.entrySet()) {
                    query.append(entry.getKey() + "=" + entry.getValue() + "&");
                }
            }

            String accessToken = UserUtil.getAccessToken(context);
            if(accessToken != null)
                query.append(ACCESS_TOKEN + "=" + accessToken);
            else{
                //FETCH FRESH ACCESS TOKEN FROM SERVER AND APPEND IN QUERY
                String token = fetchAccessToken(context);
                Log.v(LOG_TAG, "Fresh Access Token fetched : " + token);
                if(token == null) {
                    Log.e(LOG_TAG, "Unable to fetch fresh access token from server");
                    HashMap<String, String> returnMap = new HashMap<>(2);
                    returnMap.put(IS_SUCCESSFUL, String.valueOf(false));
                    returnMap.put(RESPONSE_STRING, null);
                    return returnMap;
                } else
                    query.append(ACCESS_TOKEN + "=" + token);
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
                sResponseString = null;
            }

            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null){
                buffer.append(line + "\n");
            }

            bufferedReader.close();

            if( buffer.length() == 0 )
                sResponseString = null;

            Log.i(LOG_TAG, "Api response : " + buffer.toString());
            sResponseString = buffer.toString();
            HashMap<String, String> returnMap = new HashMap<>(2);
            returnMap.put(IS_SUCCESSFUL, String.valueOf(isSuccessful));
            returnMap.put(RESPONSE_STRING, sResponseString);
            return returnMap;

        } catch (SocketTimeoutException e){
            Log.e(LOG_TAG, "Error ", e);
            return createConnectionTimeoutResponse();
        } catch (ConnectTimeoutException e) {
            Log.e(LOG_TAG, "Error ", e);
            return createConnectionTimeoutResponse();
        } catch (IOException e){
            Log.e(LOG_TAG, "Error ", e);
        }
        return null;
    }

    public static HashMap<String, String> makePOSTRequest(Context context, String apiEndpoint, HashMap<String, String> requestParams){
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

                String accessToken = UserUtil.getAccessToken(context);
                if(accessToken != null)
                    payload += ACCESS_TOKEN + "=" + accessToken;
                else{
                    //FETCH FRESH ACCESS TOKEN FROM SERVER AND APPEND IN QUERY
                    String token = fetchAccessToken(context);
                    Log.v(LOG_TAG, "Fresh Access Token fetched : " + token);
                    if(token == null) {
                        Toast.makeText(context, "Unable to fetch access token from server. Please contact admin", Toast.LENGTH_SHORT).show();
                        return null;
                    } else
                        payload += ACCESS_TOKEN + "=" + token;
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

    public static HashMap<String, String> makePUTRequest(Context context, String apiEndpoint, HashMap<String, String> requestParams) {

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

            String accessToken = UserUtil.getAccessToken(context);
            if(accessToken == null){
                //FETCH FRESH ACCESS TOKEN FROM SERVER AND APPEND IN QUERY
                accessToken = fetchAccessToken(context);
                Log.v(LOG_TAG, "Fresh Access Token fetched : " + accessToken);
                if(accessToken == null) {
                    Toast.makeText(context, "Unable to fetch access token from server. Please contact admin", Toast.LENGTH_SHORT).show();
                    return null;
                }
            }
            rawFormat += "\"" + ACCESS_TOKEN + "\":\"" + accessToken + "\"";
            rawFormat += "}";

            Log.e(LOG_TAG, "Raw Format : " + rawFormat);
            URL url = new URL(builtUri.toString());

            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Length", rawFormat.length() + "");
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
            e.printStackTrace();
        }
        return null;
    }

    private static HashMap<String, String> createConnectionTimeoutResponse(){
        HashMap<String, String> returnMap = new HashMap<>();
        returnMap.put(IS_SUCCESSFUL, String.valueOf(false));
        returnMap.put(RESPSONE_CODE, String.valueOf(408));
        returnMap.put(RESPONSE_STRING, "Connection Timeout. Please Try Again");
        return returnMap;
    }

    private static String fetchAccessToken(Context context){
        HttpURLConnection connection = null;
        BufferedReader bufferedReader = null;

        try {
            URL url = new URL(UserUtil.ACCESS_TOKEN_ENDPOINT);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int status = connection.getResponseCode();
            Log.e(LOG_TAG, "Response Code : " + status);
            InputStream inputStream;
            if(status >= 400){
                Log.e(LOG_TAG, "Error Returned");
                return null;
            } else{
                inputStream = connection.getInputStream();
            }

            StringBuffer buffer = new StringBuffer();
            if( inputStream == null ){
                sResponseString = null;
            }

            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null){
                buffer.append(line + "\n");
            }

            bufferedReader.close();

            if( buffer.length() == 0 )
                sResponseString = null;

            Log.e(LOG_TAG, "Access Token response : " + buffer.toString());
            sResponseString = buffer.toString();

            JSONObject jsonObject = new JSONObject(sResponseString);
            Log.e(LOG_TAG, "Access Token : " + jsonObject.getString(UserUtil.ACCESS_TOKEN));
            UserUtil.saveAccessToken(
                    context,
                    jsonObject.getString(UserUtil.ACCESS_TOKEN),
                    jsonObject.getLong(UserUtil.ACCESS_TOKEN_EXPIRY)
            );
            return jsonObject.getString(UserUtil.ACCESS_TOKEN);
        } catch (JSONException e){
            Log.e(LOG_TAG, "JSON Exception");
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

}
