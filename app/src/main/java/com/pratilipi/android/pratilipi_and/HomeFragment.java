package com.pratilipi.android.pratilipi_and;

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

import com.pratilipi.android.pratilipi_and.adapter.CardViewAdapter;
import com.pratilipi.android.pratilipi_and.adapter.HomeFragmentAdapter;
import com.pratilipi.android.pratilipi_and.data.PratilipiContract;
import com.pratilipi.android.pratilipi_and.datafiles.Homescreen;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CATEGORY_LOADER = 0;
    private static final String LOG_TAG = HomeFragment.class.getSimpleName();

    private HomeFragmentAdapter mHomeFragmentAdapter;
    private ListView mHomeListView;

    private CardViewAdapter mCardViewAdapter;
    private RecyclerView mcardListView;
    private List<Homescreen> mHomescreenList = new ArrayList<Homescreen>();

    private static final String[] CATEGORY_COLUMNS = {
            PratilipiContract.HomeScreenEntity._ID,
            PratilipiContract.HomeScreenEntity.COLUMN_CATEGORY_ID,
            PratilipiContract.HomeScreenEntity.COLUMN_CATEGORY_NAME};

    public static final int COL_CATEGORY_ID = 1;
    public static final int COL_CATEGORY_NAME = 2;


    private static final String[] CONTENT_COLUMNS = {
            PratilipiContract.HomeScreenEntity._ID,
            PratilipiContract.HomeScreenEntity.COLUMN_PRATILIPI_ID,
            PratilipiContract.HomeScreenEntity.COLUMN_PRATILIPI_TITLE,
            PratilipiContract.HomeScreenEntity.COLUMN_COVER_URL,
            PratilipiContract.HomeScreenEntity.COLUMN_PRICE,
            PratilipiContract.HomeScreenEntity.COLUMN_DISCOUNTED_PRICE
    };

    public static final int COL_PRATILIPI_ID = 1;
    public static final int COL_PRATILIPI_TITLE = 2;
    public static final int COL_COVER_URL = 3;
    public static final int COL_PRICE = 4;
    public static final int COL_DISCOUNTED_PRICE = 5;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(LOG_TAG, "HomeFragment onCreateView function called");

        mHomeFragmentAdapter = new HomeFragmentAdapter( getActivity(), null, 0 );
        mCardViewAdapter = new CardViewAdapter(mHomescreenList);

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        mHomeListView = (ListView) rootView.findViewById(R.id.homescreen_fragment_listview);
        mHomeListView.setAdapter(mHomeFragmentAdapter);

        View cardView = inflater.inflate(R.layout.homescreen_list_item, (ViewGroup) mHomeListView, false);
        mcardListView = (RecyclerView) cardView.findViewById(R.id.homescreen_card_list_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        mcardListView.setHasFixedSize(true);
        mcardListView.setLayoutManager(layoutManager);
        mcardListView.setAdapter(mCardViewAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.e(LOG_TAG, "onActivityCreated function called");
        fetchData();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader;
        Log.e(LOG_TAG, "HomeFragment onCreateLoader function :: CATEGORY_LOADER");
        Uri distinctCategoryUri = PratilipiContract.HomeScreenEntity.CONTENT_URI;
        cursorLoader = new CursorLoader(getActivity(), distinctCategoryUri, CATEGORY_COLUMNS, null, null, null);
        Log.e(LOG_TAG, "Selection : " + cursorLoader.getProjection());

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int loaderId = loader.getId();
        Log.e(LOG_TAG, "HomeFragment onLoaderFinished function. Loader Id : " + loaderId);
        mHomeFragmentAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mHomeFragmentAdapter.swapCursor(null);
    }

    public void fetchData() {
        Uri baseUri = PratilipiContract.HomeScreenEntity.CONTENT_URI;
        Cursor cursor = getActivity().getContentResolver().query(baseUri, CATEGORY_COLUMNS, null, null, null);


        if (!cursor.moveToFirst()) {
            getLoaderManager().initLoader(CATEGORY_LOADER, null, this);
//            if(isOnline())
//                makeJsonArryReq();
//            else
//                showNoConnectionDialog(getActivity());
        }
        else{
            mHomeFragmentAdapter.swapCursor(cursor);
        }
    }

    public void getDataFromDb(String languageId, String categoryId){

        Uri uri = PratilipiContract.HomeScreenEntity.getCategoryWiseContentForHomeScreenUri(languageId, categoryId);
        Cursor cursor = getActivity().getContentResolver().query(uri, CONTENT_COLUMNS, null, null, null);
        int contentSize = 0;
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            Homescreen homescreen = new Homescreen();
            homescreen.setmPratilipiId(cursor.getString(COL_PRATILIPI_ID));
            homescreen.setmTitle(cursor.getString(COL_PRATILIPI_TITLE));
            homescreen.setmCoverImageUrl(cursor.getString(COL_COVER_URL));
            homescreen.setmPrice(cursor.getFloat(COL_PRICE));
            homescreen.setmDiscountedPrice(COL_DISCOUNTED_PRICE);
            contentSize++;
            mHomescreenList.add(homescreen);
            mCardViewAdapter.notifyDataSetChanged();
        }
        Log.e(LOG_TAG, "Count of Content Objects : " + contentSize);

        return;
    }

}
