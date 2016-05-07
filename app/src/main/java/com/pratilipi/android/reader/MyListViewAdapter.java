package com.pratilipi.android.reader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pratilipi.android.pratilipi_and.R;

import java.util.List;

/**
 * Created by Rahul Ranjan on 5/7/2016.
 * ListView.setSelection(int position) does not work for ListView with adapters. Hence this custom
 * adapter is created to mimic setSelection function.
 */
public class MyListViewAdapter extends BaseAdapter {

    private LayoutInflater mInflator;
    private Context mContext;
    private int mSelectedItem;
    private List<String> mTitles;

    public MyListViewAdapter(Context context, List<String> titles){
        this.mContext = context;
        this.mTitles = titles;
        mInflator = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return mTitles.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflator.inflate(R.layout.drawer_list_item, null);
            holder = new ViewHolder();
            holder.tilte = (TextView) convertView.findViewById(R.id.list_textview);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // set selected item
        if (position == mSelectedItem){
            holder.tilte.setBackgroundResource(R.color.pressed_color);
        }
        else{
            holder.tilte.setBackgroundResource(R.color.default_color);
        }
        holder.tilte.setText(mTitles.get(position));
        return convertView;
    }

    public void setSelectedItem(int position) {
        mSelectedItem = position;
        notifyDataSetChanged();
    }

    public int getSelectedItem(){
        return mSelectedItem;
    }

     // ViewHolder is used to prevent repetition of list items.
    static class ViewHolder{
        TextView tilte;
    }
}
