package com.pratilipi.android.pratilipi_and.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pratilipi.android.pratilipi_and.CardListActivity;
import com.pratilipi.android.pratilipi_and.R;
import com.pratilipi.android.pratilipi_and.data.PratilipiContract;
import com.pratilipi.android.pratilipi_and.util.CategoryUtil;

/**
 * Created by Rahul Ranjan on 8/25/2015.
 */
public class HomeFragmentAdapter extends CursorAdapter {

    private static final String LOG_TAG = HomeFragmentAdapter.class.getSimpleName();

    private Context mContext;
    private CardViewAdapter mCardViewAdapter;


    public HomeFragmentAdapter( Context context, Cursor cursor, int flags, CardViewAdapter cardViewAdapter ){
        super(context, cursor, flags);
        mContext = context;
        mCardViewAdapter = cardViewAdapter;
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
    public void bindView(View view, final Context context, final Cursor cursor) {

        Log.e(LOG_TAG, "bindView() function called");
        TextView title = (TextView) view.findViewById(R.id.homescreen_list_item_title_textview);
        TextView viewAll = (TextView) view.findViewById(R.id.homescreen_list_item_viewall_textview);

        title.setText(cursor.getString(CategoryUtil.COL_CATEGORY_NAME));
        viewAll.setTag(R.string.category_id_tag, cursor.getString(CategoryUtil.COL_CATEGORY_ID));
        viewAll.setTag(R.string.category_name_tag, cursor.getString(CategoryUtil.COL_CATEGORY_NAME));
        viewAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(LOG_TAG, "View All clicked. Category Id : " + v.getTag(R.string.category_id_tag));
                Log.e(LOG_TAG, "View All clicked. Category Name : " + v.getTag(R.string.category_name_tag));
                Intent categoryListActivityIntent = new Intent(context, CardListActivity.class);
                categoryListActivityIntent.putExtra(CardListActivity.INTENT_EXTRA_ID, (String) v.getTag(R.string.category_id_tag));
                categoryListActivityIntent.putExtra(CardListActivity.INTENT_EXTRA_TITLE, (String) v.getTag(R.string.category_name_tag));
                context.startActivity(categoryListActivityIntent);
            }
        });

        Log.e(LOG_TAG, "Category Name : " + cursor.getString(CategoryUtil.COL_CATEGORY_NAME));

        String categoryId = cursor.getString(CategoryUtil.COL_CATEGORY_ID);

        getListDataFromDatabase( categoryId );
    }

    private void getListDataFromDatabase( String categoryId ){
        Uri uri = PratilipiContract.HomeScreenBridgeEntity.getPratilipiIdListByCategoryUri( categoryId );
        Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);

        Log.e(LOG_TAG, "Number of Cards : " + cursor.getCount());

        mCardViewAdapter.swapCursor(cursor);
        mCardViewAdapter.notifyDataSetChanged();
    }


}
