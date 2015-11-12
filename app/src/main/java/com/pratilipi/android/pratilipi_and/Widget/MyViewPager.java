package com.pratilipi.android.pratilipi_and.Widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Rahul Ranjan on 11/6/2015.
 */
public class MyViewPager extends ViewPager {

    private OnClickListener mOnClickListener;

    public MyViewPager(Context context) {
        super(context);

        setup();
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        setup();
    }

    private void setup() {
        final GestureDetector tapGestureDetector = new GestureDetector(getContext(), new TapGestureListener());

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                tapGestureDetector.onTouchEvent(event);

                return false;
            }
        });
    }

    public void setOnViewPagerClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public interface OnClickListener {
        void onViewPagerClick(ViewPager viewPager);
    }

    private class TapGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if(mOnClickListener != null) {
                mOnClickListener.onViewPagerClick(MyViewPager.this);
            }

            return true;
        }
    }
}
