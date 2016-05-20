package com.pratilipi.android.pratilipi_and;

import android.app.Application;
import android.util.Log;

//import com.crashlytics.android.Crashlytics;
//import io.fabric.sdk.android.Fabric;

/**
 * Created by Rahul Ranjan on 8/27/2015.
 */
public class AppController extends Application {
    public static final String LOG_TAG = AppController.class.getSimpleName();

    private static AppController mInstance;

    @Override
    public void onCreate() {
        Log.i(LOG_TAG, "App Controller is created");
        super.onCreate();
//        Fabric.with(this, new Crashlytics());
//        Fabric.with(this, new Crashlytics());
        mInstance = this;
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

}