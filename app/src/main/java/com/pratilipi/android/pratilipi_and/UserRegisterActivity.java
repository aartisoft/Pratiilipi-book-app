package com.pratilipi.android.pratilipi_and;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.pratilipi.android.pratilipi_and.util.UserUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class UserRegisterActivity extends AppCompatActivity {

    private final static String LOG_TAG = UserRegisterActivity.class.getSimpleName();

    private final String USER_NAME = "name";
    private final String USER_EMAIL = "email";
    private final String USER_PASSWORD = "password";

    private EditText mFirstNameEditText;
    private EditText mLastNameEditText;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_register);

        mFirstNameEditText = (EditText) this.findViewById(R.id.user_register_first_name);
        mLastNameEditText = (EditText) this.findViewById(R.id.user_register_last_name);
        mEmailEditText = (EditText) this.findViewById(R.id.user_register_email);
        mPasswordEditText  = (EditText) this.findViewById(R.id.user_register_password);
        mPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        registerButton = (Button) this.findViewById(R.id.user_register_button);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = mFirstNameEditText.getText().toString().trim();
                String lastName = mLastNameEditText.getText().toString().trim();
                final String email = mEmailEditText.getText().toString().trim();
                String password = mPasswordEditText.getText().toString();
                if(firstName == null || firstName.isEmpty()){
                    Toast.makeText(getApplicationContext(),
                            "Please Enter First Name", Toast.LENGTH_LONG).show();
                } else if(email == null || email.isEmpty()){
                    Toast.makeText(getApplicationContext(),
                            "Please Enter Email", Toast.LENGTH_LONG).show();
                } else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    Toast.makeText(getApplicationContext(),
                            "Please Enter Valid Email", Toast.LENGTH_LONG).show();
                } else if(password == null || password.isEmpty()){
                    Toast.makeText(getApplicationContext(),
                            "Please Enter Password", Toast.LENGTH_LONG).show();
                } else{
                    String name;
                    if( lastName == null || lastName.isEmpty() )
                        name = firstName;
                    else
                        name = firstName + " " + lastName;

                    HashMap<String, String>  params = new HashMap<>();
                    params.put(USER_NAME, name);
                    params.put(USER_EMAIL, email);
                    params.put(USER_PASSWORD, password);

                    final UserUtil userUtil = new UserUtil(UserRegisterActivity.this, "Logging In...");
                    userUtil.userRegister(params, new GetCallback() {
                        @Override
                        public void done(boolean isSuccessful, String responseString) {

                            try{
                                JSONObject responseJson = new JSONObject(responseString);
                                if(!isSuccessful){
                                    Toast.makeText(getApplicationContext(),
                                            responseJson.getString("message"), Toast.LENGTH_LONG)
                                            .show();
                                } else {
                                    userUtil.updateUser(UserRegisterActivity.this, email, responseJson);
                                    startActivity(new Intent(UserRegisterActivity.this, MainActivity.class));
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
