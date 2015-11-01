package com.pratilipi.android.pratilipi_and;

import android.app.Activity;
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
import android.widget.Toast;

import com.pratilipi.android.pratilipi_and.adapter.ShelfAdapter;
import com.pratilipi.android.pratilipi_and.data.PratilipiContract;
import com.pratilipi.android.pratilipi_and.datafiles.User;
import com.pratilipi.android.pratilipi_and.util.AppUtil;
import com.pratilipi.android.pratilipi_and.util.ShelfUtil;
import com.pratilipi.android.pratilipi_and.util.UserUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ShelfFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ShelfFragment.class.getSimpleName();
    private static int SHELF_LOADER = 0;
    private ShelfAdapter mShelfAdapter;



    public ShelfFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_shelf, container, false);

        mShelfAdapter = new ShelfAdapter();

        RecyclerView recyclerView  = ( RecyclerView ) rootView.findViewById( R.id.shelf_recyclerview );
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mShelfAdapter);

        Log.e(LOG_TAG, "onCreateView function of ShelfFragment");
        User user = UserUtil.getLoggedInUser(getActivity());
        if(user != null)
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
        return new CursorLoader( getActivity(), PratilipiContract.ShelfEntity.CONTENT_URI, null, null, null, null );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mShelfAdapter.swapCursor(data);
        mShelfAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mShelfAdapter.swapCursor(null);
        mShelfAdapter.notifyDataSetChanged();
    }

    private void fetchData(){
        Log.e(LOG_TAG, "fetchData function of ShelfFragment");
        User user = UserUtil.getLoggedInUser(getActivity());
        if(user == null)
            return;
        Uri uri = PratilipiContract.ShelfEntity.getShelfContentByUser(user.getEmail());
        Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
        if(!cursor.moveToFirst())
            fetchDataFromServer();
        else{
            mShelfAdapter.swapCursor(cursor);
            cursor.moveToFirst();
            int currentJulianDay = AppUtil.getCurrentJulianDay();
            int lastDbUpdateJulianDay = cursor.getInt(cursor.getColumnIndex(PratilipiContract.ShelfEntity.COLUMN_CREATION_DATE));
            if( currentJulianDay > lastDbUpdateJulianDay )
                fetchDataFromServer();
        }
    }

    private void fetchDataFromServer(){
        if(!AppUtil.isOnline(getActivity()))
            return;
        ShelfUtil.getShelfPratilipiListFromServer(getActivity(), new GetCallback() {
            @Override
            public void done(boolean isSuccessful, String data) {
                if(data == null)
                    return;

                if( isSuccessful )
                    onSuccess(data);
                else
                    onFailed(data);
            }
        });
    }

    private void onSuccess(String data){
        try{
            JSONObject responseJSON = new JSONObject( data );
            JSONArray pratilipiDataListJSONArray = responseJSON.getJSONArray(ShelfUtil.PRATILIPI_DATA_LIST);
            Log.e(LOG_TAG, "JSONArray is extracted from json object with length : " + pratilipiDataListJSONArray.length());
            int rowsInserted = ShelfUtil.bulkInsert( getActivity(), pratilipiDataListJSONArray );
            if( rowsInserted > 0 )
                getLoaderManager().initLoader(SHELF_LOADER, null, this);
            else {
                Log.e(LOG_TAG, "Shelf Entity update failed");
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void onFailed(String data){
        Log.e(LOG_TAG, "onFailed function of ShelfFragment");
        try {
            JSONObject jsonObject = new JSONObject(data);
            Toast.makeText(getActivity(), jsonObject.getString("message"), Toast.LENGTH_LONG);
        } catch ( JSONException e){
            e.printStackTrace();
        }
    }



}
