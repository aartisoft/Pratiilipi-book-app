package com.pratilipi.android.pratilipi_and;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.pratilipi.android.pratilipi_and.adapter.CardListViewAdapter;
import com.pratilipi.android.pratilipi_and.data.PratilipiContract;
import com.pratilipi.android.pratilipi_and.util.AppUtil;
import com.pratilipi.android.pratilipi_and.util.PratilipiUtil;
import com.pratilipi.android.pratilipi_and.util.SearchUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Have to extends FragmentActivity to use getSupportLoadManager but this does not have actionbar.
 * That's using AppCompatActivity instead ( AppCompatActivity extends from FragmentActivity ).
 */

public class CardListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    private static final String LOG_TAG = CardListActivity.class.getSimpleName();
    private static final int PRATILIPI_LIST_LOADER = 0;
    private static final String LANGUAGE_ID = "languageId";
    private static final String CATEGORY_ID = "categoryId";
    private static final String QUERY = "query";
    private static final String STATE = "state";
    private static final String STATE_PUBLISHED = "PUBLISHED";
    private static final String RESULT_COUNT_STRING = "resultCount";
    private static final int RESULT_COUNT = 20;
    private static final String CURSOR = "cursor";

    private String mId;
    private String mTitle;
    private String mLauncher;
    private String mSearchQuery;
    private LinearLayoutManager mLayoutManager;
    private CardListViewAdapter mCardListViewAdapter;
    private RecyclerView mCardListRecyclerView;
    private PratilipiUtil mPratilipiUtil;
    private SearchUtil mSearchUtil;
    private int mRowsInserted = 0;
    private String mCursorString;
    private int mNumberOfRetries = 0;

    public static final String INTENT_EXTRA_LAUNCHER = "launcher";
    public static final String INTENT_EXTRA_ID = "categoryId";
    public static final String INTENT_EXTRA_TITLE = "categoryName";
    public static final String INTENT_EXTRA_SEARCH_QUERY = "query";
    public static final String LAUNCHER_SEARCH = "search";
    public static final String LAUNCHER_CATEGORY = "category";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list);

        String parentClass = this.getParentActivityIntent().getClass().getSimpleName();
        Log.e(LOG_TAG, "Parent Activity Class Name : " + parentClass );

        Bundle bundle = getIntent().getExtras();
        mLauncher = bundle.getString(INTENT_EXTRA_LAUNCHER);
        if( mLauncher.equals(LAUNCHER_CATEGORY) ) {
            mId = bundle.getString(INTENT_EXTRA_ID);
            mTitle = bundle.getString(INTENT_EXTRA_TITLE);
        } else if( mLauncher.equals(LAUNCHER_SEARCH) ) {
            mSearchQuery = bundle.getString(INTENT_EXTRA_SEARCH_QUERY);
        }

        setTitle(mTitle);
        getSupportActionBar().setElevation(1f);

        mCardListRecyclerView = (RecyclerView) findViewById(R.id.card_list_recyclerview);
        mCardListRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mCardListRecyclerView.setLayoutManager(mLayoutManager);

        mCardListViewAdapter = new CardListViewAdapter();
        mCardListRecyclerView.setAdapter(mCardListViewAdapter);
        mCardListViewAdapter.notifyDataSetChanged();



        fetchData();

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri pratilipiListUri =
                PratilipiContract.PratilipiEntity
                        .getPratilipiListByCategoryUri(mId);
        Log.e(LOG_TAG, "ONCREATELOADER. Pratilipi List Uri : " + pratilipiListUri);
        return new CursorLoader( this, pratilipiListUri, null, null, null, null );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCardListViewAdapter.swapCursor(data);
        Log.e(LOG_TAG, "onLoadFinished function call");
        mCardListViewAdapter.notifyDataSetChanged();
    }



    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCardListViewAdapter.swapCursor(null);
        mCardListViewAdapter.notifyDataSetChanged();
    }

    @Nullable
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    public void fetchData() {
        if( mLauncher.equals( LAUNCHER_CATEGORY )) {
            Uri uri = PratilipiContract.PratilipiEntity.getPratilipiListByCategoryUri(mId);
            Cursor cursor = this.getContentResolver().query(uri, null, null, null, null);

            if (!cursor.moveToFirst()) {
                Log.e(LOG_TAG, "Server call for categories");
                fetchDataFromServer(null);
            } else {
                mCardListViewAdapter.swapCursor(cursor);
                mCardListViewAdapter.notifyDataSetChanged();

                int currentJulianDay = AppUtil.getCurrentJulianDay();
                cursor.moveToFirst();
                int lastDbUpdateJulianDay = cursor.getInt(cursor.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_CREATION_DATE));
                if (currentJulianDay > lastDbUpdateJulianDay)
                    fetchDataFromServer(null);
            }
        } else if( mLauncher.equals( LAUNCHER_SEARCH )){
            fetchSearchData( null );
        }
    }

    public void fetchSearchData( String cursorString ){
        mSearchUtil = new SearchUtil(this, "Loading Result...");
        HashMap<String, String> params = new HashMap<>();
        params.put(LANGUAGE_ID, String.valueOf(AppUtil.getPreferredLanguage(this)));
        params.put( QUERY, mSearchQuery );
        if( cursorString != null && !cursorString.isEmpty() )
            params.put( CURSOR, cursorString );
        params.put( RESULT_COUNT_STRING, String.valueOf(RESULT_COUNT) );
        mSearchUtil.fetchSearchResults( params, new GetCallback() {
            @Override
            public void done(boolean isSuccessful, String data) {
                if (isSuccessful) {
                    onSuccess(data);
                } else
                    onFailed(data);
            }
        });
    }


    private void fetchDataFromServer(String cursorString){
        mPratilipiUtil = new PratilipiUtil(this, "Loading...");
        HashMap<String, String> params = new HashMap<>();
        params.put(LANGUAGE_ID, String.valueOf(AppUtil.getPreferredLanguage(this)));
        params.put(CATEGORY_ID, mId);
        params.put(STATE, STATE_PUBLISHED);
        params.put(RESULT_COUNT_STRING, String.valueOf(RESULT_COUNT));
        if( cursorString != null )
            params.put(CURSOR, cursorString);
        mPratilipiUtil.fetchPratilipiList(params, new GetCallback() {
            @Override
            public void done(boolean isSuccessful, String data) {
                if (isSuccessful) {
                    onSuccess(data);
                } else
                    onFailed(data);
            }
        });
    }


    private void onSuccess(String data){
        try{
            JSONObject responseJSON = new JSONObject(data);
            mCursorString = responseJSON.getString(CURSOR);
            if( mLauncher.equals( LAUNCHER_CATEGORY )) {
                JSONArray pratilipiListArray = responseJSON.getJSONArray(PratilipiUtil.PRATILIPI_LIST);
                mRowsInserted = PratilipiUtil.bulkInsert(this, pratilipiListArray, mId);
                if (mRowsInserted > 0) {
                    getSupportLoaderManager().initLoader(PRATILIPI_LIST_LOADER, null, this);
                    if (mRowsInserted == RESULT_COUNT && (mCursorString != null || !mCursorString.isEmpty())) {
                        fetchDataFromServer(mCursorString);
                    }
                } else {
                    Log.e(LOG_TAG, "Pratilipi Entity update failed");
                    while (mNumberOfRetries < 3)
                        fetchDataFromServer(mCursorString);
                }
            } else if( mLauncher.equals( LAUNCHER_SEARCH )){
                //WHEN LAUNCHER IS SEARCH
                JSONArray pratilipiListArray = responseJSON.getJSONArray( SearchUtil.PRATILIPI_DATA_LIST );
                mCardListViewAdapter.addToPratilipiList( mSearchUtil.getPratilipiList(pratilipiListArray));
                mCardListViewAdapter.notifyDataSetChanged();
                int resultCount = pratilipiListArray.length();
                if( resultCount == RESULT_COUNT && ( mCursorString != null || !mCursorString.isEmpty() ))
                    fetchSearchData( mCursorString );
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void onFailed(String data){
        try {
            JSONObject jsonObject = new JSONObject(data);
            Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_LONG);
        } catch ( JSONException e){
            e.printStackTrace();
        }
    }

}
