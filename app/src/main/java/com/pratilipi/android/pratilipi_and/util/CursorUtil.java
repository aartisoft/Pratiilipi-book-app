package com.pratilipi.android.pratilipi_and.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.pratilipi.android.pratilipi_and.data.PratilipiContract;

/**
 * Created by Rahul Ranjan on 1/10/2016.
 */
public class CursorUtil {

    private final String LOG_TAG = CursorUtil.class.getSimpleName();

    private final String[] COLUMN_PROJECTION = {PratilipiContract.CursorEntity.COLUMN_CURSOR};

    public boolean insertCursor(Context context, String listName, String cursor){
        ContentValues values = new ContentValues();
        values.put(PratilipiContract.CursorEntity.COLUMN_LIST_NAME, listName);
        values.put(PratilipiContract.CursorEntity.COLUMN_CURSOR, cursor);
        values.put(PratilipiContract.CursorEntity.COLUMN_CREATION_DATE, AppUtil.getCurrentJulianDay());

        Uri uri = PratilipiContract.CursorEntity.CONTENT_URI;

        Uri returnedUri = context.getContentResolver().insert(uri, values);
        if(returnedUri != null) {
            Log.i(LOG_TAG, "Cursor insert for list id : " + listName);
            return true;
        }
        else
            Log.e(LOG_TAG, "Cursor insert failed for list id : " + listName);

        return false;
    }

    public boolean updateCursor(Context context, String listName, String cursor){
        Uri uri = PratilipiContract.CursorEntity.CONTENT_URI;
        ContentValues values = new ContentValues();
        values.put(PratilipiContract.CursorEntity.COLUMN_CURSOR, cursor);
        values.put(PratilipiContract.CursorEntity.COLUMN_CREATION_DATE, AppUtil.getCurrentJulianDay());

        String selections = PratilipiContract.CursorEntity.COLUMN_LIST_NAME + "=?";
        String[] selectionArgs = new String[]{listName};
        int rowsUpdated = context.getContentResolver().update(uri, values, selections, selectionArgs);
        if(rowsUpdated > 0) {
            Log.i(LOG_TAG, "Cursor updated for list id " + listName);
            return true;
        }
        else{
            Log.e(LOG_TAG, "Cursor update for list id " + listName + " failed");
            return false;
        }
    }

    public String getCursor(Context context, String listName){
        Log.e(LOG_TAG, "CursorUtil.getCursor() called");
        Uri uri = PratilipiContract.CursorEntity.CONTENT_URI;
        String selection = PratilipiContract.CursorEntity.COLUMN_LIST_NAME + "=?";
        String[] selectionArgs = new String[]{listName};
        Cursor cursor = context.getContentResolver()
                .query(uri,
                        COLUMN_PROJECTION,
                        selection,
                        selectionArgs,
                        null);
        if(cursor != null && cursor.moveToFirst())
            return cursor.getString(0);
        else
            return null;
    }

    public int deleteCursor(Context context, String listName){

        Uri uri = PratilipiContract.CursorEntity.CONTENT_URI;
        int deletedRows = 0;
        if(listName == null){
            //DELETE ALL ROWS
            deletedRows = context.getContentResolver().delete(uri, null, null);
        } else{
            String selection = PratilipiContract.CursorEntity.COLUMN_LIST_NAME + "=?";
            String[] selectionArgs = new String[]{listName};
            deletedRows = context.getContentResolver().delete(uri, selection, selectionArgs);
        }

        return deletedRows;
    }
}
