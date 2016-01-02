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
import com.pratilipi.android.pratilipi_and.datafiles.Content;
import com.pratilipi.android.pratilipi_and.datafiles.Pratilipi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Rahul Ranjan on 10/1/2015.
 */
public class ContentUtil {

    private static final String LOG_TAG = ContentUtil.class.getSimpleName();

    private static final String IMAGE_CONTENT_ENDPOINT = "http://android.pratilipi.com/pratilipi/content/image";
    private static final String TEXT_CONTENT_ENDPOINT = "http://android.pratilipi.com/pratilipi/content";
    private static final String TEXT_CONTENT_ENDPOINT_OLD = "http://www.pratilipi.com/api.pratilipi/pratilipi/content";

    private static final String TEXT_CONTENT = "pageContent";
    private static final String IMAGE_CONTENT = "data";

    private static final String PRATILIPI_ID = "pratilipiId";
    private static final String CHAPTER_NUMBER = "chapterNo";
    private static final String PAGE_NUMBER = "pageNo";
    private static final String PAGE_CONTENT = "pageContent";
    private static final String CONTENT_TYPE = "contentType";

    public static final String TEXT_CONTENT_TYPE = "pratilipi";
    public static final String IMAGE_COTENT_TYPE = "image";

    public static final String INDEX_PAGE_NO = "pageNo";
    public static final String INDEX_LEVEL = "level";
    public static final String PAGELET_LIST = "pageletList";


    public static final String[] CONTENT_COLUMN = {
            PratilipiContract.ContentEntity._ID,
            PratilipiContract.ContentEntity.COLUMN_PRATILIPI_ID,
            PratilipiContract.ContentEntity.COLUMN_CHAPTER_NUMBER,
            PratilipiContract.ContentEntity.COLUMN_PAGE_NUMBER,
            PratilipiContract.ContentEntity.COLUMN_TEXT_CONTENT,
            PratilipiContract.ContentEntity.COLUMN_IMAGE_CONTENT
    };

    public static int PRATILIPI_ID_COLUMN_INDEX = 1;
    public static int CHAPTER_NO_COLUMN_INDEX = 2;
    public static int PAGE_NO_COLUMN_INDEX = 3;
    public static int TEXT_CONTENT_COLUMN_INDEX = 4;
    public static int IMAGE_CONTENT_COLUMN_INDEX = 5;

    public static Cursor query(Context context, String pratilipiId, String chapterNo, String pageNo, String contentType){
        Uri uri = PratilipiContract.ContentEntity.CONTENT_URI;
        String selection = PratilipiContract.ContentEntity.COLUMN_PRATILIPI_ID + "=?";
        String[] selectionArgs;
        String sortOrder;
        if(chapterNo != null){
            selection = selection + " AND " + PratilipiContract.ContentEntity.COLUMN_CHAPTER_NUMBER + "=?";
            selectionArgs = new String[]{pratilipiId, chapterNo};
            sortOrder = null;
        } else if(pageNo != null){
            selection = selection + " AND " + PratilipiContract.ContentEntity.COLUMN_PAGE_NUMBER + "=?";
            selectionArgs = new String[]{pratilipiId, pageNo};
            sortOrder = null;
        } else{
            selectionArgs = new String[]{pratilipiId};
            if( contentType.equals(TEXT_CONTENT_TYPE))
                sortOrder = PratilipiContract.ContentEntity.COLUMN_CHAPTER_NUMBER;
            else
                sortOrder = PratilipiContract.ContentEntity.COLUMN_PAGE_NUMBER;
        }


        return context.getContentResolver().query(
                uri,
                CONTENT_COLUMN,
                selection,
                selectionArgs,
                sortOrder
        );
    }

    public static boolean insert(Context context, JSONObject object, String pratilipiId, String chapterNumber, String pageNumber){

        Uri returnUri = null;
        //IF chapterNumber != null > Text content > Fetch and update DB
        if (chapterNumber != null) {

            String content = "";
            try{
                JSONArray contentArray = object.getJSONArray(PAGELET_LIST);
                for(int i = 0;i < contentArray.length(); ++i){
                    JSONObject jsonObject = contentArray.getJSONObject(i);
                    content = content + jsonObject.getString("data");
                    if(i < contentArray.length()-1)
                        content = content + "\n";
                }
            }catch(JSONException e){
                Log.e(LOG_TAG, "JSON Exception occurred");
                e.printStackTrace();
                Toast.makeText(context, "SOME ERROR OCCURRED. PLEASE TRY AGAIN", Toast.LENGTH_LONG).show();
                return false;
            }

//            Log.e(LOG_TAG, "Content String : " + content);
            Uri uri = PratilipiContract.ContentEntity.getPratilipiContentByChapterUri(pratilipiId, chapterNumber);
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor.moveToFirst()) {
                //USED WHEN ONE CHAPTER IS CONTAINED IN MORE THAN 1 PAGE.
//                Log.e(LOG_TAG, "DB entry already exist");
                String existingContent = cursor.getString(cursor.getColumnIndex(PratilipiContract.ContentEntity.COLUMN_TEXT_CONTENT));
                String updatedContent = existingContent + "\n" + content.trim();
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
//                Log.e(LOG_TAG, "No DB entry exists");
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
//        Log.e(LOG_TAG, "Chapter No : " + chapterNo);
        String contentString = "";
        try{
            JSONObject object = new JSONObject(content);
            JSONArray contentArray = object.getJSONArray("pageletList");
            for(int i = 0;i < contentArray.length(); ++i){
                JSONObject jsonObject = contentArray.getJSONObject(i);
                contentString = contentString + jsonObject.getString("data");
                if(i < contentArray.length()-1)
                    contentString = contentString + "</br>";
            }
        } catch(JSONException e){
            e.printStackTrace();
        }
        Uri uri = PratilipiContract.ContentEntity.CONTENT_URI;
        String selection = PratilipiContract.ContentEntity.COLUMN_PRATILIPI_ID + "=? and "
                + PratilipiContract.ContentEntity.COLUMN_CHAPTER_NUMBER + "=? and "
                + PratilipiContract.ContentEntity.COLUMN_PAGE_NUMBER + "=?";
        ContentValues values = new ContentValues();
        if(content != null)
            values.put(PratilipiContract.ContentEntity.COLUMN_TEXT_CONTENT, contentString);
        values.put(PratilipiContract.ContentEntity.COLUMN_LAST_ACCESSED_ON, AppUtil.getCurrentJulianDay());

        String[] selectionArgs = new String[]{pratilipiId, chapterNo, pageNo};
        context.getContentResolver().update(uri, values, selection, selectionArgs);
        return contentString;
    }

    public Cursor getContentfromDb(
            Context context, Pratilipi pratilipi, Integer chapterNo, Integer pageNo){

        String pratilipiIdStr = pratilipi.getPratilipiId() + "";
        String chapterNoStr = chapterNo + "";
        String pageNoStr = pageNo + "";

        Uri uri = PratilipiContract.ContentEntity.getPratilipiContentByChapterUri(pratilipiIdStr, chapterNoStr);
        Cursor cursor = context.getContentResolver().query(
                uri,
                CONTENT_COLUMN,
                null,
                null,
                null
        );

        return cursor;

//        if(cursor == null || !cursor.moveToFirst()){
//            //INSERT INTO DATABASE WITH CONTENT = ""
//            ContentValues values = new ContentValues();
//            values.put(PratilipiContract.ContentEntity.COLUMN_PRATILIPI_ID, pratilipi.getPratilipiId());
//            values.put(PratilipiContract.ContentEntity.COLUMN_CHAPTER_NUMBER, chapterNoStr);
//            values.put(PratilipiContract.ContentEntity.COLUMN_PAGE_NUMBER, pageNo);
//            values.put(PratilipiContract.ContentEntity.COLUMN_LAST_ACCESSED_ON, AppUtil.getCurrentJulianDay());
//            Uri insertedUri = context.getContentResolver().insert(PratilipiContract.ContentEntity.CONTENT_URI, values);
//            Log.e(LOG_TAG, "Inserted Uri : " + insertedUri);
//
//            HashMap<String, String> params = new HashMap<>();
//            params.put(PRATILIPI_ID, pratilipiIdStr);
//            params.put(CHAPTER_NUMBER, chapterNoStr);
//            params.put(PAGE_NUMBER, pageNoStr);
//            new DownloadAsyncTask(context, pratilipiIdStr, chapterNoStr, pageNoStr, pratilipi.getContentType(), callback)
//                    .execute(params);
//        } else {
//            //update last accessed date
//            update(context, pratilipiIdStr, chapterNoStr, pageNoStr, null);
//            callback.done(true, cursor.toString());
////            try {
////                JSONObject json = new JSONObject();
////                json.put(PRATILIPI_ID, pratilipi.getPratilipiId());
////                json.put(PAGE_NUMBER, pageNo);
////                json.put(PAGE_CONTENT, cursor.getString(4));
////
////            } catch (JSONException e){
////                e.printStackTrace();
////            }
//        }
    }

    public void fetchPageFromServer(Context context, Pratilipi pratilipi, int pageNo, GetCallback callback){
        String pratilipiIdStr = pratilipi.getPratilipiId() + "";
        String chapterNoStr = getChapterNumber(pratilipi, pageNo) + "";
        String pageNoStr = pageNo + "";

        HashMap<String, String> params = new HashMap<>();
        params.put(PRATILIPI_ID, pratilipiIdStr);
        params.put(CHAPTER_NUMBER, chapterNoStr);
        params.put(PAGE_NUMBER, pageNoStr);
        new DownloadAsyncTask(context, pratilipiIdStr, chapterNoStr, pageNoStr, pratilipi.getContentType(), callback)
                .execute(params);
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

    private class ChapterDownloadAsyncTask extends AsyncTask<Void, Void, String> {

        private GetCallback mCallback;
        private boolean mIsSuccessful;
        private Context mContext;
        private String mPratilipiId;
        private String mChapterNo;
        private int mCurrentPage;
        private int mLastPage;

        private int mPostExecuteFunctionCounter;

        public ChapterDownloadAsyncTask(Context context, String pratilipiId, String chapterNo, int firstPage, int lastPage, GetCallback callback){
            this.mContext = context;
            this.mPratilipiId = pratilipiId;
            this.mChapterNo = chapterNo;
            this.mCurrentPage = firstPage;
            this.mLastPage = lastPage;
            this.mCallback = callback;

            mPostExecuteFunctionCounter=0;
        }

        @Override
        protected String doInBackground(Void... params) {
            Log.e(LOG_TAG, "doInBackground() function");
            HashMap<String, String> param = new HashMap<>();
            param.put(PAGE_NUMBER, String.valueOf(mCurrentPage));
            param.put(PRATILIPI_ID, mPratilipiId);
            param.put(CHAPTER_NUMBER, mChapterNo);
            HashMap<String, String> responseMap = HttpUtil.makeGETRequest(mContext, TEXT_CONTENT_ENDPOINT, param);
            mIsSuccessful = Boolean.valueOf(responseMap.get(HttpUtil.IS_SUCCESSFUL));
            return responseMap.get(HttpUtil.RESPONSE_STRING);
        }

        @Override
        protected void onPostExecute(String responseString) {
            mPostExecuteFunctionCounter++;
            Log.e(LOG_TAG, "onPostExecute function. Current Page : " + mCurrentPage);
            if(mIsSuccessful) {
                //UPDATE DATABASE
                try{
                    JSONObject json = new JSONObject(responseString);
                    if(json.has(PAGE_CONTENT))
                        insert(mContext, json.getJSONObject(PAGE_CONTENT), mPratilipiId, mChapterNo, null);

                } catch (JSONException e){
                    e.printStackTrace();
                    if(mPostExecuteFunctionCounter < 4)
                        onPostExecute(responseString);
                    else
                        Toast.makeText(mContext, "Exception while processing server response", Toast.LENGTH_LONG);
                }

                if(mCurrentPage == mLastPage){
                    mCallback.done(mIsSuccessful, responseString);
                    //Problem with response string.
                } else{
                    mCurrentPage++;
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            doInBackground();
                        }
                    });
                }
            }
            super.onPostExecute(responseString);
        }

    }

    public Integer getChapterNumber(Pratilipi pratilipi, Integer pageNo){
        if(pageNo == null)
            return null;
        String indexString = pratilipi.getIndex();
        try{
            JSONArray indexJson = new JSONArray(indexString);
            for(int i=0; i<indexJson.length(); ++i){
                JSONObject chapterObject = indexJson.getJSONObject(i);
                if(chapterObject.getInt(INDEX_LEVEL) != 0)
                    continue;
                if(chapterObject.getInt(INDEX_PAGE_NO) > pageNo){
                    return i;
                }
            }
            //For last Chapter
            if(pageNo < pratilipi.getPageCount())
                return indexJson.length();
            else {
                Log.e(LOG_TAG, "Page number cannot be greater more than total page count");
                return -1;
            }

        }catch (JSONException e){
            e.printStackTrace();
            return -1;
        }

    }

    public boolean fetchChapterFromServer(Context context, Pratilipi pratilipi, int chapterNo, GetCallback callback){
        String indexString = pratilipi.getIndex();
        try{
            JSONArray indexJson = new JSONArray(indexString);
            JSONObject chapterJson = indexJson.getJSONObject(chapterNo - 1);
            int firstPage = 1;
//            if(chapterNo > 1){
//                Log.e(LOG_TAG, "This is not first chapter");
//                firstPage = chapterJson.getInt(PAGE_NUMBER);
//            }
            int lastPage = 1;
//            int lastPage = pratilipi.getPageCount();
//            if(chapterNo < indexJson.length()-1) {
//                Log.e(LOG_TAG, "This is not last chapter");
//                JSONObject nextChapterJson = indexJson.getJSONObject(chapterNo);
//                lastPage = nextChapterJson.getInt(PAGE_NUMBER)-1;
//            }
//            Log.e(LOG_TAG, "First Page / Last Page : " + firstPage + "/" + lastPage);
            new ChapterDownloadAsyncTask(context, pratilipi.getPratilipiId(), String.valueOf(chapterNo), firstPage, lastPage, callback).execute();
        }  catch (JSONException e){
            e.printStackTrace();
        }
        return false;
    }


    public List<Content> createContentList(Cursor cursor){
        if( cursor == null || !cursor.moveToFirst())
            return null;
        List<Content> contentList = new ArrayList<>(cursor.getCount());
        while (!cursor.isAfterLast()){
            Content content = new Content();
            content.setPratilipiId(cursor.getString(PRATILIPI_ID_COLUMN_INDEX));
            content.setChapterNo(cursor.getString(CHAPTER_NO_COLUMN_INDEX));
            content.setPageNo(cursor.getString(PAGE_NO_COLUMN_INDEX));
            content.setTextContent(cursor.getString(TEXT_CONTENT_COLUMN_INDEX));
            content.setImageContent(cursor.getBlob(IMAGE_CONTENT_COLUMN_INDEX));
            contentList.add(content);
            cursor.moveToNext();
        }
        return contentList;
    }

}
