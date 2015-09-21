package com.pratilipi.android.pratilipi_and;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.pratilipi.android.pratilipi_and.adapter.CardViewAdapter;
import com.pratilipi.android.pratilipi_and.adapter.HomeFragmentAdapter;
import com.pratilipi.android.pratilipi_and.data.PratilipiContract;
import com.pratilipi.android.pratilipi_and.datafiles.Homescreen;
import com.pratilipi.android.pratilipi_and.service.PratilipiService;
import com.pratilipi.android.pratilipi_and.util.AppUtil;
import com.pratilipi.android.pratilipi_and.util.CategoryUtil;
import com.pratilipi.android.pratilipi_and.util.HomeFragmentUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class HomeFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CATEGORY_LOADER = 0;
    private static final String LOG_TAG = HomeFragment.class.getSimpleName();
    private static final String LOADING_MESSAGE = "Loading...";
    private static final String LANGUAGE_ID = "languageId";

    private HomeFragmentAdapter mHomeFragmentAdapter;
    private ListView mHomeListView;
    private RecyclerView mCardListView;
    private CardViewAdapter mCardViewAdapter;
    private List<Homescreen> mHomescreenList;
    private HomeFragmentUtil mHomeFragmentUtil;


    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHomescreenList = new ArrayList<>();
        mCardViewAdapter = new CardViewAdapter(mHomescreenList);
        mHomeFragmentAdapter = new HomeFragmentAdapter( getActivity(), null, 0, mCardViewAdapter );
        fetchData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.e(LOG_TAG, "onCreateView() function called");

        View cardView = inflater.inflate(R.layout.homescreen_list_item, (ViewGroup) mHomeListView, false);
        mCardListView = (RecyclerView) cardView.findViewById(R.id.homescreen_card_list_view);
        mCardListView.setAdapter(mCardViewAdapter);
        mCardListView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mCardListView.setLayoutManager(layoutManager);

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        mHomeListView = (ListView) rootView.findViewById(R.id.homescreen_fragment_listview);
        mHomeListView.setAdapter(mHomeFragmentAdapter);


        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader;
        Uri categoryUri = PratilipiContract.HomeScreenBridgeEntity.CONTENT_URI
                .buildUpon()
                .appendQueryParameter(PratilipiContract.CategoriesEntity.COLUMN_IS_ON_HOME_SCREEN, String.valueOf(1))
                .build();
        cursorLoader = new CursorLoader(getActivity(), categoryUri, CategoryUtil.CATEGORY_COLUMNS, null, null, null);

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mHomeFragmentAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mHomeFragmentAdapter.swapCursor(null);
    }


    private void fetchData() {
        Cursor cursor = CategoryUtil.getCategoryList(getActivity(), 1);

        if (!cursor.moveToFirst()) {
            fetchDataFromServer();
//            if(isOnline())
//                makeJsonArryReq();
//            else
//                showNoConnectionDialog(getActivity());
        }
        else{
            mHomeFragmentAdapter.swapCursor(cursor);

            int currentJulianDay = AppUtil.getCurrentJulianDay();
            int lastDbUpdateJulianDay = cursor.getInt(CategoryUtil.COL_CREATION_DATE);
            if( currentJulianDay > lastDbUpdateJulianDay )
                startPratilipiService();
        }
    }

    private void fetchDataFromServer(){
        mHomeFragmentUtil = new HomeFragmentUtil(getActivity(), LOADING_MESSAGE);
        HashMap<String, String> params = new HashMap<>();
        params.put( LANGUAGE_ID, String.valueOf(AppUtil.getPreferredLanguage(getActivity())) );
        mHomeFragmentUtil.fetchHomeFragmentContent(params, new GetCallback() {
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
        int rowsInserted = 0;
        //TODO : INSERT BEFORE DELETING DATA.
        HomeFragmentUtil.cleanHomeScreenEntity( getActivity() );
        HomeFragmentUtil.cleanCategoryEntity( getActivity() );
        rowsInserted = mHomeFragmentUtil.bulkInsert( getActivity(), data );
        if( rowsInserted > 0 ){
            getLoaderManager().initLoader(CATEGORY_LOADER, null, this);
        } else {
            Log.e(LOG_TAG, "HomeScreen Entity update failed");
        }
    }

    private void onFailed(String data){
        try {
            JSONObject jsonObject = new JSONObject(data);
            Toast.makeText(getActivity(), jsonObject.getString("message"), Toast.LENGTH_LONG);
        } catch ( JSONException e){
            e.printStackTrace();
        }
    }

    private void startPratilipiService(){
        Intent serviceIntent = new Intent(getActivity(), PratilipiService.class);
        getActivity().startService(serviceIntent);
    }

}
