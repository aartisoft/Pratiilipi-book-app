package com.pratilipi.android.pratilipi_and.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.pratilipi.android.pratilipi_and.data.PratilipiContract;
import com.pratilipi.android.pratilipi_and.data.PratilipiDbHelper;

import java.util.Vector;

/**
 * Created by Rahul Ranjan on 9/11/2015.
 */
public class CategoryPratilipiUtil {

    private static final String LOG_TAG = CategoryPratilipiUtil.class.getSimpleName();

    public static int bulkInsert(Context context, Vector<ContentValues> values){
        int rowsInserted = 0;

        return rowsInserted;
    }

    public static int delete(Context context, String selection, String[] selectionArgs){
        return context.getContentResolver().delete(
                PratilipiContract.CategoriesPratilipiEntity.CONTENT_URI,
                selection,
                selectionArgs);
    }

    public static boolean isTableEmpty(Context context){
        //TODO: GET RID OF rawQuery() FUNCTION. SECURITY HAZARD
        String query = "SELECT * FROM " + PratilipiContract.CategoriesPratilipiEntity.TABLE_NAME;
        Cursor cursor = new PratilipiDbHelper(context).getReadableDatabase().rawQuery(query, null);
        if(cursor.moveToFirst())
            return false;
        else
            return true;
    }
}
