package com.pratilipi.android.pratilipi_and.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pratilipi.android.pratilipi_and.HomeFragment;
import com.pratilipi.android.pratilipi_and.R;

/**
 * Created by Rahul Ranjan on 8/25/2015.
 */
public class HomeFragmentAdapter extends CursorAdapter {

    private static final String LOG_TAG = HomeFragmentAdapter.class.getSimpleName();

    private Context mContext;



    public HomeFragmentAdapter( Context context, Cursor cursor, int flags ){
        super(context, cursor, flags);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.homescreen_list_item, parent, false);
        return view;
    }

    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Log.e(LOG_TAG, "bindView function called");
        TextView title = (TextView) view.findViewById(R.id.homescreen_list_item_title_textview);
        TextView viewAll = (TextView) view.findViewById(R.id.homescreen_list_item_viewall_textview);

        title.setText(cursor.getString(HomeFragment.COL_CATEGORY_NAME));
        viewAll.setHint(cursor.getString(HomeFragment.COL_CATEGORY_ID));

        String languageId = "5130467284090880";
        String categoryId = cursor.getString(HomeFragment.COL_CATEGORY_ID);
        
//        Fragment homeFragment = ((Activity)context).getFragmentManager().findFragmentByTag("home_fragment_tag");
//        Log.e(LOG_TAG, "Home Fragment in bindView function : " + homeFragment.getTag());
//        homeFragment.getDataFromDb(languageId, categoryId);
    }




}
