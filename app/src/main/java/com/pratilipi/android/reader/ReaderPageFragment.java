package com.pratilipi.android.reader;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pratilipi.android.pratilipi_and.R;
import com.pratilipi.android.pratilipi_and.util.AppUtil;

public class ReaderPageFragment extends Fragment {

    public static String READER_FRAGMENT_TAG = "reader_fragment_tag";
    public final String LOG_TAG = ReaderPageFragment.class.getSimpleName();

    private Activity activity;
    private TextView mTextView;
    private PratilipiData.Pagelet[] pagelets;
    private LinearLayout view;

    public void setPagelets( PratilipiData.Pagelet[] pagelets ) {
        this.pagelets = pagelets;

        if( view == null )
            return;

        view.removeAllViews();

        for( int i = 0; i < pagelets.length; i++ ) {
            mTextView = new TextView( activity );
            mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, AppUtil.getReaderFontSize(activity));
            mTextView.setText(pagelets[i].getData());
            view.addView(mTextView);
        }
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {

        activity = getActivity();

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .add(this, READER_FRAGMENT_TAG);

        if( view == null )
            view = (LinearLayout) inflater.inflate( R.layout.reader_fragment, container, false );

        if( pagelets != null )
            setPagelets( pagelets );

        return view;
    }

    @Override
    public void onStop() {
        Log.e(LOG_TAG, "Reader Fragment stoped");
        super.onStop();
    }

    public void setFontSize(float fontSize){
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
    }

    public float getFontSize(){
        return mTextView.getTextSize();
    }
}
