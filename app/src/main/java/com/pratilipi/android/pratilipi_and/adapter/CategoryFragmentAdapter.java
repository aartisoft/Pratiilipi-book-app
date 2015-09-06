package com.pratilipi.android.pratilipi_and.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pratilipi.android.pratilipi_and.CategoryFragment;
import com.pratilipi.android.pratilipi_and.R;

/**
 * Created by Rahul Ranjan on 9/5/2015.
 */
public class CategoryFragmentAdapter extends CursorAdapter {

    private static final String LOG_TAG = CategoryFragmentAdapter.class.getSimpleName();

    public CategoryFragmentAdapter(Context context, Cursor cursor, int flags){
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.category_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView textView = (TextView) view.findViewById(R.id.category_list_item_textview);
        textView.setText(cursor.getString(CategoryFragment.COL_CATEGORY_NAME));
    }
}
