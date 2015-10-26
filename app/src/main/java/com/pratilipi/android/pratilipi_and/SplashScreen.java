package com.pratilipi.android.pratilipi_and;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.pratilipi.android.pratilipi_and.util.AppUtil;
import com.pratilipi.android.pratilipi_and.util.UserUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class SplashScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        String accessToken = UserUtil.getAccessToken(this);
        if(accessToken == null && AppUtil.isOnline(this)){

            UserUtil.fetchAccessToken(getBaseContext(), new GetCallback() {
                Context context = getBaseContext();
                @Override
                public void done(boolean isSuccessful, String data) {
                    try{
                        JSONObject jsonObject = new JSONObject(data);
                        if(isSuccessful){
                            UserUtil.saveAccessToken(context,
                                    jsonObject.getString(UserUtil.ACCESS_TOKEN),
                                    jsonObject.getLong(UserUtil.ACCESS_TOKEN_EXPIRY));
                        }
                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                    finish();
                }
            });
        } else{
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.e("SplashScreen", "onDestroy function");
        unbindDrawables(findViewById(R.id.splashscreen_linear_layout));
        System.gc();
    }

    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        Log.e("SplashScreen", "unbindDrawables function");

        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }
}
