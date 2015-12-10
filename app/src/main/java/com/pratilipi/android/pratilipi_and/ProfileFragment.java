package com.pratilipi.android.pratilipi_and;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.pratilipi.android.pratilipi_and.data.PratilipiContract;
import com.pratilipi.android.pratilipi_and.datafiles.User;
import com.pratilipi.android.pratilipi_and.util.FbLoginUtil;
import com.pratilipi.android.pratilipi_and.util.ShelfUtil;
import com.pratilipi.android.pratilipi_and.util.UserUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;


public class ProfileFragment extends Fragment {

    private static final String LOG_TAG = ProfileFragment.class.getSimpleName();

    private static final String[] USER_PROJECTION = {
            PratilipiContract.UserEntity._ID,
            PratilipiContract.UserEntity.COLUMN_DISPLAY_NAME,
            PratilipiContract.UserEntity.COLUMN_CONTENTS_IN_SHELF,
            PratilipiContract.UserEntity.COLUMN_IS_LOGGED_IN,
            PratilipiContract.UserEntity.COLUMN_PROFILE_IMAGE,
            PratilipiContract.UserEntity.COLUMN_EMAIL
    };

    private static final int COL_DISPLAY_NAME = 1;
    private static final int COL_CONTENT_IN_SHELF = 2;
    private static final int COL_IS_LOGGED_IN = 3;
    private static final int COL_PROFILE_IMAGE = 4;
    private static final int COL_EMAIL = 5;

    private final String PROFILE_FRAGMENT_TAG = "profileFragmentTag";
    private final String USER_EMAIL = "email";

    private User mUser;
    private CallbackManager mCallbackManager;
    private ProgressDialog dialog;

    public ProfileFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getActivity());
        mCallbackManager = CallbackManager.Factory.create();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .add(this, PROFILE_FRAGMENT_TAG);

        if( mUser == null ) {
            final Button loginLink = (Button) rootView.findViewById(R.id.profile_login_button);
            Button registerButton = (Button) rootView.findViewById(R.id.profile_register_button);
            final LoginButton fbLoginButton = (LoginButton) rootView.findViewById(R.id.profile_fb_login_button);
            fbLoginButton.setReadPermissions(Arrays.asList("public_profile, email, user_birthday"));
            fbLoginButton.setFragment(this);

            loginLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent loginIntent = new Intent(getActivity(), UserLoginActivity.class);
                    startActivity(loginIntent);
                }
            });

            registerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity(), UserRegisterActivity.class));
                }
            });

            fbLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(final LoginResult loginResult) {
                    dialog = new ProgressDialog(getActivity());
                    dialog.setMessage("Updating user details...");
                    dialog.show();


                    //GETTING PROFILE INFO
                    GraphRequest request = GraphRequest.newMeRequest(
                            loginResult.getAccessToken(),
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(
                                        JSONObject object,
                                        GraphResponse response) {
                                    // Application code
                                    Log.e(LOG_TAG, response.toString());
                                    final JSONObject responseObject = response.getJSONObject();
                                    String accessTokenString = loginResult.getAccessToken().getToken();
                                    Log.e(LOG_TAG, "FB AccessToken : " + accessTokenString);
                                    HashMap<String, String> params = new HashMap<String, String>();
                                    params.put(FbLoginUtil.FB_ACCESS_TOKEN_PARAM, accessTokenString);

                                    FbLoginUtil fbLoginUtil = new FbLoginUtil(getActivity());
                                    fbLoginUtil.fbUserLogin(params, new GetCallback() {
                                        @Override
                                        public void done(boolean isSuccessful, String data) {

                                            Log.e(LOG_TAG, "Fb login Async task callback");
                                            try{
                                                Log.e(LOG_TAG, "Server Response : " + data);
                                                JSONObject responseJson = new JSONObject(data);
                                                if( !isSuccessful ) {
                                                    FbLoginUtil.facebookLogout();
                                                    Toast.makeText(getActivity(),
                                                            responseJson.getString("message"), Toast.LENGTH_LONG)
                                                            .show();
                                                } else {
                                                    String serverEmail = responseJson.getString(USER_EMAIL);
                                                    String fbEmail = responseObject.getString(USER_EMAIL);
                                                    if(serverEmail.equals(fbEmail)) {
                                                        Log.e(LOG_TAG, "Server and fb login matches");
                                                        UserUtil userUtil = new UserUtil(getActivity(), null);
                                                        userUtil.updateUser(getActivity(), fbEmail, responseObject);
                                                    } else{
                                                        Log.e(LOG_TAG, "Server email and facebook email Id mismatch");
                                                        FbLoginUtil.facebookLogout();
                                                        Toast.makeText(getActivity(),
                                                                "Error while trying to login using facebook", Toast.LENGTH_LONG)
                                                                .show();
                                                    }
                                                }
                                            } catch(JSONException e){
                                                e.printStackTrace();
                                            }
                                            Fragment currentFragment = getActivity().getSupportFragmentManager()
                                                    .findFragmentByTag(PROFILE_FRAGMENT_TAG);
                                            //Recreate fragment. - Below if block is doing no good
                                            if(currentFragment instanceof ProfileFragment){
                                                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                                                transaction.detach(currentFragment);
                                                transaction.attach(currentFragment);
                                                transaction.commit();
                                            }
                                            dialog.hide();
                                            dialog.dismiss();
                                        }
                                    });
                                }
                            });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,name,email,gender, birthday,picture");
                    request.setParameters(parameters);
                    request.executeAsync();

                }

                @Override
                public void onCancel() {
                    // App code
                    Log.e(LOG_TAG, "Login call cancelled");
                }

                @Override
                public void onError(FacebookException exception) {
                    // App code
                    Log.e(LOG_TAG, "Exception thrown");
                    Log.e(LOG_TAG, exception.getMessage());
                }
            });


        } else {
            rootView.findViewById(R.id.guest_user_profile).setVisibility(View.GONE);
            rootView.findViewById(R.id.registered_user_profile).setVisibility(View.VISIBLE);
            ((TextView) rootView.findViewById(R.id.profile_name_textview)).setText(mUser.getDisplayName());
            ((TextView) rootView.findViewById(R.id.profile_shelf_count_textview))
                    .setText(String.valueOf(ShelfUtil.numberOfContentInShelf(getActivity(), mUser.getEmail())));

            Button logoutButton = (Button) rootView.findViewById(R.id.profile_logout_button);

            logoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UserUtil userUtil = new UserUtil(getActivity(), "Logging Out...");
                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put(UserUtil.ACCESS_TOKEN, UserUtil.getAccessToken(getActivity()));
                    userUtil.userLogout(params, new GetCallback() {
                        @Override
                        public void done(boolean isSuccessful, String data) {
                            if(isSuccessful)
                                onLogoutSuccess(data);
                            else
                                onLogoutFail(data);
                        }
                    });
                }
            });
        }

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mUser = getLoggedInUser();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private User getLoggedInUser(){
        Uri uri = PratilipiContract.UserEntity.CONTENT_URI;
        String selection = PratilipiContract.UserEntity.COLUMN_IS_LOGGED_IN + "=?";
        String[] selectionArgs = {"1"};

        Cursor cursor = getActivity().getContentResolver().query(uri,USER_PROJECTION, selection, selectionArgs, null);

        if( cursor.getCount() > 1 )
            Log.e(LOG_TAG, "More than 1 user logged in at same time");

        if( cursor.getCount() > 0 ) {
            cursor.moveToFirst();
            User user = new User();
            user.setDisplayName(cursor.getString(COL_DISPLAY_NAME));
            user.setContentsInShelf(cursor.getInt(COL_CONTENT_IN_SHELF));
            user.setProfileImageUrl(cursor.getString(COL_PROFILE_IMAGE));
            user.setEmail(cursor.getString(COL_EMAIL));
            return user;
        } else
            return null;
    }

    private int updateUserEntity(String email){
        Uri uri = PratilipiContract.UserEntity.CONTENT_URI;
        String selection = PratilipiContract.UserEntity.COLUMN_EMAIL + "=?";
        String[] selectionArgs = {email};

        ContentValues values = new ContentValues();
        values.put(PratilipiContract.UserEntity.COLUMN_IS_LOGGED_IN, 0);

        return getActivity().getContentResolver().update(uri, values, selection, selectionArgs);
    }

    private void onLogoutSuccess(String data){
        try{
            JSONObject jsonObject = new JSONObject(data);
            if(jsonObject.has(UserUtil.ACCESS_TOKEN)){
                UserUtil.saveAccessToken(getActivity(), jsonObject.getString(UserUtil.ACCESS_TOKEN), jsonObject.getLong(UserUtil.ACCESS_TOKEN_EXPIRY));
                Intent intent = getActivity().getIntent();
                int userLoggedOutCount = updateUserEntity(mUser.getEmail());
                if( userLoggedOutCount == 1 ) {
                    //Invalidate facebook session
                    FbLoginUtil.facebookLogout();
                    startActivity(intent);
                }
                else{
                    Log.e(LOG_TAG, "Error while updating user entity");
                }
            } else {
                Log.e(LOG_TAG, "Error while fetching access token from server");
            }

        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void onLogoutFail(String data){
        try {
            JSONObject jsonObject = new JSONObject(data);
            Toast.makeText(getActivity(), jsonObject.getString("message"), Toast.LENGTH_LONG);
        } catch ( JSONException e){
            e.printStackTrace();
        }
    }

}
