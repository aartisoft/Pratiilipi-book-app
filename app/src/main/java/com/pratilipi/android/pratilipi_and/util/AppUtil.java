package com.pratilipi.android.pratilipi_and.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;

import com.pratilipi.android.pratilipi_and.LanguageSelectionActivity;
import com.pratilipi.android.pratilipi_and.R;

/**
 * Created by Rahul Ranjan on 9/6/2015.
 */
public class AppUtil {

    private static final String LOG_TAG = AppUtil.class.getSimpleName();

    private static final long GUJARATI_LANGUAGE_ID = 5965057007550464L;
    private static final long HINDI_LANGUAGE_ID = 5130467284090880L;
    private static final long TAMIL_LANGUAGE_ID = 6319546696728576L;

    public static final String GUJARATI_LOCALE = "gu";
    public static final String HINDI_LOCALE = "hi";
    public static final String TAMIL_LOCALE = "ta";

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        Log.e(LOG_TAG, "Network Info : " + netInfo);
    if( netInfo != null )
            Log.e(LOG_TAG, "Is Connected : " + netInfo.isConnectedOrConnecting());
    return netInfo != null && netInfo.isConnectedOrConnecting();
}

    public static void showNoConnectionDialog(Context ctx1) {
        final Context ctx = ctx1;
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setCancelable(true);
        builder.setMessage(R.string.no_connection);
        builder.setTitle(R.string.no_connection_title);
        builder.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                Intent dialogIntent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(dialogIntent);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                return;
            }
        });

        builder.show();
    }

    public static Long getPreferredLanguage(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String preferedLanguage =  prefs.getString(LanguageSelectionActivity.SELECTED_LANGUAGE,
                LanguageSelectionActivity.SELECTED_LANGUAGE_GUJARATI);
        if( preferedLanguage.equals(GUJARATI_LOCALE))
            return GUJARATI_LANGUAGE_ID;
        else if ( preferedLanguage.equals(HINDI_LOCALE) )
            return HINDI_LANGUAGE_ID;
        else if( preferedLanguage.equals(TAMIL_LOCALE))
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
