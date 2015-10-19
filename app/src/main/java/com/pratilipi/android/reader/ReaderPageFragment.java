package com.pratilipi.android.reader;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pratilipi.android.pratilipi_and.R;

public class ReaderPageFragment extends Fragment {

    private Activity activity;
    private PratilipiData.Pagelet[] pagelets;
    private LinearLayout view;

    public void setPagelets( PratilipiData.Pagelet[] pagelets ) {
        this.pagelets = pagelets;

        if( view == null )
            return;

        view.removeAllViews();

        for( int i = 0; i < pagelets.length; i++ ) {
            TextView textView = new TextView( activity );
            textView.setText( pagelets[i].getData() );
            view.addView(textView);
        }
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {

        activity = getActivity();

        if( view == null )
            view = (LinearLayout) inflater.inflate( R.layout.reader_fragment, container, false );

        if( pagelets != null )
            setPagelets( pagelets );

        return view;

    }

}
