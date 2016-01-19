package com.pratilipi.android.pratilipi_and.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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


    public HomeFragmentAdapter( Context context, Cursor cursor, int flags ){
        super(context, cursor, flags);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Log.e( LOG_TAG, "newView() function");
        View view = LayoutInflater.from(context).inflate(R.layout.homescreen_list_item, parent, false);

        return view;
    }

    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
//        Log.e( LOG_TAG, "bindView() function");
        mCardViewAdapter = new CardViewAdapter();

        TextView title = (TextView) view.findViewById(R.id.homescreen_list_item_title_textview);
        TextView viewAll = (TextView) view.findViewById(R.id.homescreen_list_item_viewall_textview);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.homescreen_card_list_view);

        title.setText(cursor.getString(CategoryUtil.COL_CATEGORY_NAME));
        viewAll.setTag(R.string.category_id_tag, cursor.getString(CategoryUtil.COL_CATEGORY_ID));
        viewAll.setTag(R.string.category_name_tag, cursor.getString(CategoryUtil.COL_CATEGORY_NAME));
        viewAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent categoryListActivityIntent = new Intent(context, CardListActivity.class);
                categoryListActivityIntent.putExtra(CardListActivity.INTENT_EXTRA_LAUNCHER, CardListActivity.LAUNCHER_CATEGORY);
                categoryListActivityIntent.putExtra(CardListActivity.INTENT_EXTRA_ID, (String) v.getTag(R.string.category_id_tag));
                categoryListActivityIntent.putExtra(CardListActivity.INTENT_EXTRA_TITLE, (String) v.getTag(R.string.category_name_tag));
                context.startActivity(categoryListActivityIntent);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mCardViewAdapter);

        String categoryId = cursor.getString(CategoryUtil.COL_CATEGORY_ID);
        if( categoryId != null ) {
            getListDataFromDatabase(categoryId);
        }
    }

    private void getListDataFromDatabase( String categoryId ){
        Uri uri = PratilipiContract.HomeScreenBridgeEntity.getPratilipiIdListByCategoryUri( categoryId );
        Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);

        mCardViewAdapter.swapCursor(cursor);
//        mCardViewAdapter.notifyDataSetChanged();
    }


}
