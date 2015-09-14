package com.pratilipi.android.pratilipi_and;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.pratilipi.android.pratilipi_and.adapter.CategoryFragmentAdapter;
import com.pratilipi.android.pratilipi_and.data.PratilipiContract;
import com.pratilipi.android.pratilipi_and.util.CategoryUtil;
import com.pratilipi.android.pratilipi_and.util.AppUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class CategoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = CategoryFragment.class.getSimpleName();
    private static final int CATEGORY_LOADER = 1;
    private static final String LANGUAGE_ID = "languageId";
    private static final String CATEGORY_LOADING_MESSAGE = "Loading...";

    private CategoryFragmentAdapter mCategoryFragmentAdapter;
    private ListView mCategoriesListView;
    private CategoryUtil mCategoryUtil;

    private static final String[] CATEGORY_COLUMNS = {
            PratilipiContract.CategoriesEntity._ID,
            PratilipiContract.CategoriesEntity.COLUMN_CATEGORY_ID,
            PratilipiContract.CategoriesEntity.COLUMN_CATEGORY_NAME,
            PratilipiContract.CategoriesEntity.COLUMN_CREATION_DATE,
    };

    public static final int COL_CATEGORY_ID = 1;
    public static final int COL_CATEGORY_NAME = 2;
    public static final int COL_CREATION_DATE = 3;

    public static final String INTENT_EXTRA_ID = "categoryId";
    public static final String INTENT_EXTRA_TITLE = "categoryName";

    public CategoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_category, container, false);

        mCategoryFragmentAdapter = new CategoryFragmentAdapter(getActivity(), null, 0);
        mCategoriesListView = (ListView) rootView.findViewById(R.id.category_list_listview);
        mCategoriesListView.setAdapter(mCategoryFragmentAdapter);

        mCategoriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                String categoryId = cursor.getString(COL_CATEGORY_ID);
                String categoryName = cursor.getString(COL_CATEGORY_NAME);

                Intent intent = new Intent(getActivity(), CardListActivity.class);
                intent.putExtra(INTENT_EXTRA_ID, categoryId);
                intent.putExtra(INTENT_EXTRA_TITLE, categoryName);

                startActivity(intent);
            }
        });

        fetchData();

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri categoryListUri =
                PratilipiContract.CategoriesEntity
                        .getCategoryListUri(AppUtil.getPreferredLanguage(getActivity()));
        return new CursorLoader( getActivity(), categoryListUri, CATEGORY_COLUMNS, null, null, null );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCategoryFragmentAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCategoryFragmentAdapter.swapCursor(null);
    }

    public void fetchData() {
        Uri uri = PratilipiContract.CategoriesEntity.getCategoryListUri(AppUtil.getPreferredLanguage(getActivity()));
        Cursor cursor = getActivity().getContentResolver().query(uri, CATEGORY_COLUMNS, null, null, null);

        if (!cursor.moveToFirst()) {
            Log.e(LOG_TAG, "Server call for categories");
            fetchDataFromServer();
        }
        else{
            mCategoryFragmentAdapter.swapCursor(cursor);

            int currentJulianDay = AppUtil.getCurrentJulianDay();
            int lastDbUpdateJulianDay = cursor.getInt(COL_CREATION_DATE);
            if( currentJulianDay > lastDbUpdateJulianDay )
                fetchDataFromServer();
        }
    }

    private void fetchDataFromServer(){
        mCategoryUtil = new CategoryUtil(getActivity(), CATEGORY_LOADING_MESSAGE);
        HashMap<String, String> params = new HashMap<>();
        params.put(LANGUAGE_ID, String.valueOf(AppUtil.getPreferredLanguage(getActivity())));
        mCategoryUtil.fetchCategories(params, new GetCallback() {
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
        try{
            rowsInserted = mCategoryUtil.bulkInsert(getActivity(), data);
            if( rowsInserted > 0 ){
                getLoaderManager().initLoader(CATEGORY_LOADER, null, this);
            } else {
                Log.e(LOG_TAG, "Categories Entity update failed");
            }

        } catch (JSONException e){
            e.printStackTrace();
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
}
