package com.pratilipi.android.pratilipi_and.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.pratilipi.android.pratilipi_and.data.PratilipiContract;
import com.pratilipi.android.pratilipi_and.datafiles.Pratilipi;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Rahul Ranjan on 10/1/2015.
 */
public class ContentUtil {

    private static final String LOG_TAG = ContentUtil.class.getSimpleName();

    private static final String TEXT_CONTENT = "pageContent";
    private static final String IMAGE_CONTENT = "data";

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
}
