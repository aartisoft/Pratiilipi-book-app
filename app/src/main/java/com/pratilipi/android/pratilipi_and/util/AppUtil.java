package com.pratilipi.android.pratilipi_and.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;

import com.pratilipi.android.pratilipi_and.LanguageSelectionActivity;

/**
 * Created by Rahul Ranjan on 9/6/2015.
 */
public class AppUtil {

    private static final String LOG_TAG = AppUtil.class.getSimpleName();

    private static final long GUJARATI_LANGUAGE_ID = 5965057007550464L;
    private static final long HINDI_LANGUAGE_ID = 5130467284090880L;
    private static final long TAMIL_LANGUAGE_ID = 6319546696728576L;


    public static Long getPreferredLanguage(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String preferedLanguage =  prefs.getString(LanguageSelectionActivity.SELECTED_LANGUAGE,
                LanguageSelectionActivity.SELECTED_LANGUAGE_GUJARATI);
        Log.e(LOG_TAG, "Prefered Language : " + preferedLanguage);
        if( preferedLanguage.equals("gu"))
            return GUJARATI_LANGUAGE_ID;
        else if ( preferedLanguage.equals("hi") )
            return HINDI_LANGUAGE_ID;
        else if( preferedLanguage.equals("ta"))
            return TAMIL_LANGUAGE_ID;
        else
            return 0L;
    }

    public static int getCurrentJulianDay(){
        Time t = new Time();
        t.setToNow();
        return Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
    }

    public static int convertToJulianDay( long dateInMillis ){
        Time t = new Time();
        t.setToNow();
        return Time.getJulianDay(dateInMillis, t.gmtoff);
    }
}
