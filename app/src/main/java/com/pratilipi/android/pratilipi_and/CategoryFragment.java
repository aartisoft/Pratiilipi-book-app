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
import android.widget.ProgressBar;
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
    private static final String LANGUAGE = "language";

    private CategoryFragmentAdapter mCategoryFragmentAdapter;
    private ListView mCategoriesListView;
    private CategoryUtil mCategoryUtil;
    private ProgressBar mProgressBar;


    public CategoryFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_category, container, false);

        mProgressBar = ( ProgressBar ) rootView.findViewById( R.id.card_list_progress_bar );

        mCategoryFragmentAdapter = new CategoryFragmentAdapter(getActivity(), null, 0);
        mCategoriesListView = (ListView) rootView.findViewById(R.id.category_list_listview);
        mCategoriesListView.setAdapter(mCategoryFragmentAdapter);

        mCategoriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                String categoryId = cursor.getString(CategoryUtil.COL_CATEGORY_ID);
                String categoryName = cursor.getString(CategoryUtil.COL_CATEGORY_NAME);

                Intent intent = new Intent(getActivity(), CardListActivity.class);
                intent.putExtra( CardListActivity.INTENT_EXTRA_LAUNCHER, CardListActivity.LAUNCHER_CATEGORY );
                intent.putExtra(CardListActivity.INTENT_EXTRA_ID, categoryId);
                intent.putExtra(CardListActivity.INTENT_EXTRA_TITLE, categoryName);

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
                        .getCategoryListUri(AppUtil.getPreferredLanguage(getActivity()), 0);
        return new CursorLoader( getActivity(), categoryListUri, CategoryUtil.CATEGORY_COLUMNS, null, null, null );
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
        Uri uri = PratilipiContract.CategoriesEntity.getCategoryListUri(AppUtil.getPreferredLanguage(getActivity()), 0);
//        Cursor cursor = getActivity().getContentResolver().query(uri, CATEGORY_COLUMNS, null, null, null);
        Cursor cursor  = CategoryUtil.getCategoryList(getActivity(), 0);

        if (!cursor.moveToFirst()) {
            fetchDataFromServer();
        }
        else{
            mCategoryFragmentAdapter.swapCursor(cursor);

            int currentJulianDay = AppUtil.getCurrentJulianDay();
            int lastDbUpdateJulianDay = cursor.getInt(CategoryUtil.COL_CREATION_DATE);
            if( currentJulianDay > lastDbUpdateJulianDay )
                fetchDataFromServer();
        }
    }

    private void fetchDataFromServer(){
        if(!AppUtil.isOnline(getActivity())) {
            return;
        }
        mCategoryUtil = new CategoryUtil( getActivity(), mProgressBar );
        HashMap<String, String> params = new HashMap<>();
        params.put(LANGUAGE, String.valueOf(AppUtil.getPreferredLanguageName(getActivity())));
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
