package com.pratilipi.android.pratilipi_and;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.pratilipi.android.pratilipi_and.data.PratilipiContract;
import com.pratilipi.android.pratilipi_and.datafiles.User;


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

    private User mUser;

    public ProfileFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        if( mUser == null ) {
            Button loginLink = (Button) rootView.findViewById(R.id.profile_login_button);
            Button registerButton = (Button) rootView.findViewById(R.id.profile_register_button);

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
        } else {
            rootView.findViewById(R.id.guest_user_profile).setVisibility(View.GONE);
            rootView.findViewById(R.id.registered_user_profile).setVisibility(View.VISIBLE);
            ((TextView) rootView.findViewById(R.id.profile_name_textview)).setText(mUser.getDisplayName());

            Button logoutButton = (Button) rootView.findViewById(R.id.profile_logout_button);

            logoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = getActivity().getIntent();
                    int userLoggedOutCount = logoutUser(mUser.getEmail());
                    if( userLoggedOutCount == 1 )
                        startActivity(intent);
                    else{
                        Toast.makeText(getActivity(), "Error While Logging out", Toast.LENGTH_LONG).show();
                    }
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

    private int logoutUser(String email){
        Uri uri = PratilipiContract.UserEntity.CONTENT_URI;
        String selection = PratilipiContract.UserEntity.COLUMN_EMAIL + "=?";
        String[] selectionArgs = {email};

        ContentValues values = new ContentValues();
        values.put(PratilipiContract.UserEntity.COLUMN_IS_LOGGED_IN, 0);

        return getActivity().getContentResolver().update(uri, values, selection, selectionArgs);
    }

}
