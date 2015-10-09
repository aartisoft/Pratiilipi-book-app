package com.pratilipi.android.pratilipi_and;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.pratilipi.android.pratilipi_and.util.UserUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class UserLoginActivity extends AppCompatActivity {

    private final static String LOG_TAG = UserLoginActivity.class.getSimpleName();

    private final String USER_ID = "userId";
    private final String USER_SECRET = "userSecret";

    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private Button loginButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        mEmailEditText = (EditText) this.findViewById(R.id.user_login_email);
        mPasswordEditText  = (EditText) this.findViewById(R.id.user_login_password);
        loginButton = (Button) this.findViewById(R.id.user_login_button);



        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmailEditText.getText().toString().trim();
                String password = mPasswordEditText.getText().toString();
                if(email == null || email.isEmpty()){
                    Toast.makeText(getApplicationContext(),
                            "Please Enter Email", Toast.LENGTH_LONG).show();
                } else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    Toast.makeText(getApplicationContext(),
                            "Please Enter Valid Email", Toast.LENGTH_LONG).show();
                } else if(password == null || password.isEmpty()){
                    Toast.makeText(getApplicationContext(),
                            "Please Enter Password", Toast.LENGTH_LONG).show();
                } else{
                    HashMap<String, String> params = new HashMap<>();
                    params.put(USER_ID, email);
                    params.put(USER_SECRET, password);

                    final UserUtil userUtil = new UserUtil(UserLoginActivity.this, "Logging In...");
                    userUtil.userLogin(params, new GetCallback() {
                        @Override
                        public void done(boolean isSuccessful, String responseString) {

                            try{
                                JSONObject responseJson = new JSONObject(responseString);
                                if( !isSuccessful ){
                                    Toast.makeText(getApplicationContext(),
                                            responseJson.getString("message"), Toast.LENGTH_LONG)
                                            .show();
                                } else {
                                    userUtil.updateUser(UserLoginActivity.this, email, responseJson);
                                    int sdkVersion = Integer.valueOf(Build.VERSION.SDK_INT);
                                    Log.e(LOG_TAG, "SDK Version : " + sdkVersion);
                                    if(  sdkVersion >=16 ){
                                        startActivity(getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                    } else {
                                        Intent intent = new Intent(UserLoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    }
                                }
                            } catch (JSONException e){
                                e.printStackTrace();
                            }

                        }
                    });
                }
            }
        });

    }

    @Nullable
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}
