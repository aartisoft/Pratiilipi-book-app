package com.pratilipi.android.pratilipi_and.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.pratilipi.android.pratilipi_and.GetCallback;
import com.pratilipi.android.pratilipi_and.data.PratilipiContract;
import com.pratilipi.android.pratilipi_and.datafiles.Pratilipi;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Rahul Ranjan on 10/1/2015.
 */
public class ContentUtil {

    private static final String LOG_TAG = ContentUtil.class.getSimpleName();

    private static final String IMAGE_CONTENT_ENDPOINT = "http://android.pratilipi.com/pratilipi/content/image";
    private static final String TEXT_CONTENT_ENDPOINT = "http://android.pratilipi.com/pratilipi/content";
    private static final String TEXT_CONTENT = "pageContent";
    private static final String IMAGE_CONTENT = "data";

    private static final String PRATILIPI_ID = "pratilipiId";
    private static final String CHAPTER_NUMBER = "chapterNo";
    private static final String PAGE_NUMBER = "pageNo";
    private static final String PAGE_CONTENT = "pageContent";


    public static final String TEXT_CONTENT_TYPE = "pratilipi";
    public static final String IMAGE_COTENT_TYPE = "image";

    public static boolean insert(Context context, JSONObject object, String pratilipiId, String chapterNumber, String pageNumber){

        Uri returnUri = null;
        //IF chapterNumber != null > Text content > Fetch and update DB
        if (chapterNumber != null) {

            String content = "";
            try{
                content = object.getString(TEXT_CONTENT);
            }catch(JSONException e){
                e.printStackTrace();
            }

            Uri uri = PratilipiContract.ContentEntity.getPratilipiContentByChapterUri(pratilipiId, chapterNumber);
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor.moveToFirst()) {
                //USED WHEN ONE CHAPTER IS CONTAINED IN MORE THAN 1 PAGE.
                String existingContent = cursor.getString(cursor.getColumnIndex(PratilipiContract.ContentEntity.COLUMN_TEXT_CONTENT));
                String updatedContent = existingContent + content.trim();
                ContentValues values = new ContentValues();
                values.put(PratilipiContract.ContentEntity.COLUMN_TEXT_CONTENT, updatedContent);
                String selection = PratilipiContract.ContentEntity.COLUMN_PRATILIPI_ID + "=? AND "
                                    + PratilipiContract.ContentEntity.COLUMN_CHAPTER_NUMBER + "=?";
                String[] selectionArgs = new String[]{pratilipiId, chapterNumber};
                int rowsUpdated = context.getContentResolver().update(PratilipiContract.ContentEntity.CONTENT_URI, values, selection, selectionArgs);
                if(rowsUpdated > 0)
                    return true;

            } else {
                //INSERT
                ContentValues values = new ContentValues();
                values.put(PratilipiContract.ContentEntity.COLUMN_PRATILIPI_ID, pratilipiId);
                values.put(PratilipiContract.ContentEntity.COLUMN_CHAPTER_NUMBER, chapterNumber);
                values.put(PratilipiContract.ContentEntity.COLUMN_TEXT_CONTENT, content.trim());
                returnUri = context.getContentResolver().insert(PratilipiContract.ContentEntity.CONTENT_URI, values);
                if(returnUri != null)
                    return true;
            }
        } else if(pageNumber != null){

            byte[] content = null;
            try{
                content = object.getString(IMAGE_CONTENT).toString().getBytes();
            }catch(JSONException e){
                e.printStackTrace();
            }

            Uri uri = PratilipiContract.ContentEntity.getPratilipiContentByPageNumberUri(pratilipiId, pageNumber);
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

            ContentValues values = new ContentValues();
            if(cursor.moveToFirst()) {
                //TO PREVENT DUPLICATE STORAGE OF IMAGES
                values.put(PratilipiContract.ContentEntity.COLUMN_IMAGE_CONTENT, content);
                String selection = PratilipiContract.ContentEntity.COLUMN_PRATILIPI_ID + "=? AND "
                                    + PratilipiContract.ContentEntity.COLUMN_PAGE_NUMBER + "=?";
                String[] selectionArgs = new String[]{pratilipiId, pageNumber};
                int rowsUpdated = context.getContentResolver().update(PratilipiContract.ContentEntity.CONTENT_URI, values, selection, selectionArgs);
                if(rowsUpdated > 0)
                    return true;
            } else{
                values.put(PratilipiContract.ContentEntity.COLUMN_PRATILIPI_ID, pratilipiId);
                values.put(PratilipiContract.ContentEntity.COLUMN_PAGE_NUMBER, pageNumber);
                values.put(PratilipiContract.ContentEntity.COLUMN_IMAGE_CONTENT, content);
                returnUri = context.getContentResolver().insert(PratilipiContract.ContentEntity.CONTENT_URI, values);
                if (returnUri != null)
                    return true;
            }
        }
        return false;
    }

    public static void delete(Context context, Pratilipi pratilipi){
        Uri uri = PratilipiContract.ContentEntity.CONTENT_URI;
        String selection = PratilipiContract.ContentEntity.COLUMN_PRATILIPI_ID + "=?";
        String[] selectionArgs = new String[]{pratilipi.getPratilipiId()};
        int rowsDeleted = context.getContentResolver().delete(uri, selection, selectionArgs);
        if(rowsDeleted == 0){
            Log.e(LOG_TAG, "Content Deletion failed");
            Toast.makeText(context, "Content Deletion failed. Try again later", Toast.LENGTH_LONG).show();
        } else {
            //UPDATE DOWNLOAD_STATUS
            PratilipiUtil.updatePratilipiDownloadStatus(
                    context,
                    pratilipi.getPratilipiId(),
                    PratilipiContract.PratilipiEntity.CONTENT_NOT_DOWNLOADED);
//            pratilipi.setDownloadStatus(PratilipiContract.PratilipiEntity.CONTENT_NOT_DOWNLOADED);
            Toast.makeText(context, "Content Deleted", Toast.LENGTH_LONG).show();
        }
    }

    public static String update(Context context, String pratilipiId, String chapterNo, String pageNo, String content){
        Uri uri = PratilipiContract.ContentEntity.CONTENT_URI;
        String selection = PratilipiContract.ContentEntity.COLUMN_PRATILIPI_ID + "=? and "
                + PratilipiContract.ContentEntity.COLUMN_CHAPTER_NUMBER + "=? and "
                + PratilipiContract.ContentEntity.COLUMN_PAGE_NUMBER + "=?";
        ContentValues values = new ContentValues();
        if(content != null)
            values.put(PratilipiContract.ContentEntity.COLUMN_TEXT_CONTENT, content);
        values.put(PratilipiContract.ContentEntity.COLUMN_LAST_ACCESSED_ON, AppUtil.getCurrentJulianDay());

        String[] selectionArgs = new String[]{pratilipiId, chapterNo, pageNo};
        context.getContentResolver().update(uri, values, selection, selectionArgs);
        return content;
    }

    public static void getContent(
            Context context, long pratilipiId, int chapterNo, int pageNo, String contentType, GetCallback callback){

        String pratilipiIdStr = pratilipiId + "";
        String chapterNoStr = chapterNo + "";
        String pageNoStr = pageNo + "";

        Uri uri = PratilipiContract.ContentEntity.getPratilipiContentUri(pratilipiIdStr, chapterNoStr, pageNoStr);
        String[] projection = new String[]{PratilipiContract.ContentEntity.COLUMN_TEXT_CONTENT};
        Cursor cursor = context.getContentResolver().query(
                uri,
                projection,
                null,
                null,
                null
        );

        if(cursor == null || !cursor.moveToFirst()){
            //INSERT INTO DATABASE WITH CONTENT = ""
            ContentValues values = new ContentValues();
            values.put(PratilipiContract.ContentEntity.COLUMN_PRATILIPI_ID, pratilipiId);
            values.put(PratilipiContract.ContentEntity.COLUMN_CHAPTER_NUMBER, chapterNo);
            values.put(PratilipiContract.ContentEntity.COLUMN_PAGE_NUMBER, pageNo);
            values.put(PratilipiContract.ContentEntity.COLUMN_LAST_ACCESSED_ON, AppUtil.getCurrentJulianDay());
            Uri insertedUri = context.getContentResolver().insert(PratilipiContract.ContentEntity.CONTENT_URI, values);
            Log.e(LOG_TAG, "Inserted Uri : " + insertedUri);

            HashMap<String, String> params = new HashMap<>();
            params.put(PRATILIPI_ID, pratilipiIdStr);
            params.put(CHAPTER_NUMBER, chapterNoStr);
            params.put(PAGE_NUMBER, pageNoStr);
            new DownloadAsyncTask(context, pratilipiIdStr, chapterNoStr, pageNoStr, contentType, callback).execute(params);
        } else {
            //update last accessed date
            update(context, pratilipiIdStr, chapterNoStr, pageNoStr, null);
            try {
                JSONObject json = new JSONObject();
                json.put(PRATILIPI_ID, pratilipiId);
                json.put(PAGE_NUMBER, pageNo);
                json.put(PAGE_CONTENT, cursor.getString(0));
                callback.done(true, json.toString());
            } catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    private static class DownloadAsyncTask extends AsyncTask<HashMap<String, String>, Void, String> {

        private GetCallback mCallback;
        private boolean mIsSuccessful;
        private String mContentType;
        private Context mContext;
        private String mPratilipiId;
        private String mChapterNo;
        private String mPageNo;

        public DownloadAsyncTask(Context context, String pratilipiId, String chapterNo, String pageNo, String contentType, GetCallback callback){
            this.mContext = context;
            this.mPratilipiId = pratilipiId;
            this.mChapterNo = chapterNo;
            this.mPageNo = pageNo;
            this.mCallback = callback;
            this.mContentType = contentType;
        }

        @Override
        protected String doInBackground(HashMap<String, String>... params) {
            String apiEndPoint;
            if(mContentType.toLowerCase().equals(TEXT_CONTENT_TYPE)){
                apiEndPoint = TEXT_CONTENT_ENDPOINT;
            } else
                apiEndPoint = IMAGE_CONTENT_ENDPOINT;
            HashMap<String, String> responseMap = HttpUtil.makeGETRequest(mContext, apiEndPoint, params[0]);
            mIsSuccessful = Boolean.valueOf(responseMap.get(HttpUtil.IS_SUCCESSFUL));

            //TODO : MOVE DELETE CODE TO onPostExecute FUNCTION
            if(!mIsSuccessful){
                //DELETE INSERTED ROW. onPostExecute is not getting called in case of error. Hence
                //included here.
                Uri uri = PratilipiContract.ContentEntity.CONTENT_URI;
                String selection = PratilipiContract.ContentEntity.COLUMN_PRATILIPI_ID + "=? and "
                        + PratilipiContract.ContentEntity.COLUMN_CHAPTER_NUMBER + "=? and "
                        + PratilipiContract.ContentEntity.COLUMN_PAGE_NUMBER + "=?";
                String[] selectionArgs = new String[]{mPratilipiId, mChapterNo, mPageNo};
                int deletedRows = mContext.getContentResolver().delete(uri, selection, selectionArgs);
                Log.e(LOG_TAG, "Inserted Uri : " + deletedRows);
            }
            return responseMap.get(HttpUtil.RESPONSE_STRING);
        }

        @Override
        protected void onPostExecute(String responseString) {
            if(mIsSuccessful) {
                //UPDATE DATABASE
                try{
                    JSONObject json = new JSONObject(responseString);
                    update(
                            mContext,
                            json.getString(PRATILIPI_ID),
                            mChapterNo,
                            json.getString(PAGE_NUMBER),
                            json.getString(PAGE_CONTENT)
                    );
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
            mCallback.done(mIsSuccessful, responseString);
            super.onPostExecute(responseString);
        }

    }

}
