package com.pratilipi.android.reader;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.pratilipi.android.pratilipi_and.R;
import com.pratilipi.android.pratilipi_and.Widget.MyViewPager;
import com.pratilipi.android.pratilipi_and.datafiles.Pratilipi;
import com.pratilipi.android.pratilipi_and.util.AppUtil;

/**
 * Created by Rahul Ranjan on 2/29/2016.
 */
public class ReaderActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener  {

    private static final String LOG_TAG = ReaderActivity.class.getSimpleName();
    public static final String PRATILIPI = "pratilipi";
    public static final String PARENT_ACTIVITY_CLASS_NAME = "parentActivityClassName";

    private Context mContext;
    private ActionBar mActionBar;
    private LinearLayout mSeekBarLayout;
    private ProgressBar mProgressBar;
    private DrawerLayout mDrawerLayout;

    private MyViewPager mMyViewPage;
    private ReaderAdapter mReaderAdapter;

    private Pratilipi mPratilipi;

    private String mParentActivityClassName;

    private int mInternetConnectionErrorCounter;
    private int mSeekBarLayoutPosition;
    private int mIndexSize;

    //Device Details.
    float mDeviceWidth;
    float mDeviceHeight;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(LOG_TAG, "THIS IS NEW READER");
        setContentView(R.layout.reader_activity);

        mInternetConnectionErrorCounter = 0;

        mContext = this;
        mActionBar = getSupportActionBar();
        mSeekBarLayout = (LinearLayout) findViewById(R.id.reader_seekbar_layout);

        mProgressBar = (ProgressBar) findViewById( R.id.reader_progress_bar );

        Display display = getWindowManager().getDefaultDisplay();
        int height = display.getHeight();
        mSeekBarLayoutPosition = height + mSeekBarLayout.getHeight();

        //Setting y-axis of seek bar layout. Applicable for API level 11 and above.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            mSeekBarLayout.setY(mSeekBarLayoutPosition);

        hideActionBar();
        //Make Content Appear Behind Status Bar and navigation bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        mPratilipi = (Pratilipi) getIntent().getSerializableExtra(PRATILIPI);
        mParentActivityClassName = getIntent().getStringExtra(PARENT_ACTIVITY_CLASS_NAME);

        setTitle(mPratilipi.getTitle());

        //Setting actionbar.
        mActionBar.setElevation(1f);
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setIcon(R.drawable.ic_logo);

        mMyViewPage = (MyViewPager) findViewById(R.id.reader_viewPager);
        mReaderAdapter = new ReaderAdapter(getSupportFragmentManager(), mContext, mPratilipi, 1);
        mMyViewPage.setAdapter(mReaderAdapter);

        //SWIPE LEFT AND RIGHT
        mMyViewPage.setOnTouchListener(new View.OnTouchListener() {
            float startX;
            boolean performClick = false;
            boolean isSwiping = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX();


                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        startX = x;
                        performClick = true;
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        float delta = Math.abs(startX - event.getX());
                        int currentItem = mMyViewPage.getCurrentItem();
                        int currentChapter = mReaderAdapter.getCurrentChapter(currentItem);
                        if (currentItem == 0 && !isSwiping && delta > 10 && event.getX() > startX) {
                            /**Add previous chapter to start of list when user swipes right
                             * and current item is first item on list
                             * 'isSwiping' is used to prevent multiple times execution of following code.
                             * 'delta' is used to distinguish between swipe and click event.
                             */
                            isSwiping = true;
                            mReaderAdapter.getContentFromDb(currentChapter-1, ReaderAdapter.PREVIOUS_CHAPTER);

                        } else if (delta > 1 && event.getX() < startX) {
//                            Log.i(LOG_TAG, "SWIPING LEFT");
                        }

                        //To capture click event.
                        if (delta > 1)
                            performClick = false;
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
//                        Log.e(LOG_TAG, "Current Item : " + viewPager.getCurrentItem());
                        if (performClick) {
                            if (mActionBar.isShowing())
                                hideActionBar();
                            else
                                showActionBar();
                        }
                        isSwiping = false;
                        break;
                    }
                }

                return false;
            }
        });

    }

    @Nullable
    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reader, menu);
        if (mIndexSize < 1) {
            menu.findItem(R.id.action_index).setVisible(false);
//            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        int id = item.getItemId();
        ReaderFragment readerFragment = (ReaderFragment) getSupportFragmentManager()
                .findFragmentByTag(ReaderPageFragment_old.READER_FRAGMENT_TAG);
        if(id == R.id.action_font_size_decrease){
            float currentFontSize = AppUtil.getReaderFontSize(mContext);
            //TODO : Case missing - when font size is not present in shared preference.
            if( currentFontSize <= 30f )
                return false;

            //STORE FONT SIZE IN SHARED PREFERENCE
            AppUtil.updateReaderFontSize(this, currentFontSize - 5f);
            //Notify Adaptor about the change
            mReaderAdapter.setFontSizeChangeFlag(true);
        }

        if(id == R.id.action_font_size_increase){
            float currentFontSize = AppUtil.getReaderFontSize(mContext);
            //TODO : Case missing - when font size is not present in shared preference.
            if( currentFontSize >= 50f )
                return false;

            //STORE FONT SIZE IN SHARED PREFERENCE
            AppUtil.updateReaderFontSize(this, currentFontSize + 5f);
            //Notify Adaptor about the change
            mReaderAdapter.setFontSizeChangeFlag(true);
        }

        if(id == R.id.action_index){
            if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                mDrawerLayout.closeDrawer(Gravity.RIGHT);
            } else {
                mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private void hideActionBar(){
        if (Build.VERSION.SDK_INT >= 16) {
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        }
        mActionBar.hide();
        //Scroll away SeekBarLayout.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
            //Applicable for API level 12 and above
            mSeekBarLayout.animate()
                    .translationYBy(mSeekBarLayout.getHeight())
                    .translationY(mSeekBarLayoutPosition)
                    .setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
        else
            mSeekBarLayout.setVisibility(View.INVISIBLE);

    }

    private void showActionBar(){
        if (Build.VERSION.SDK_INT >= 16) {
            View decorView = getWindow().getDecorView();
            // Show the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
            decorView.setSystemUiVisibility(uiOptions);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        mActionBar.show();
        //Scroll away SeekBarLayout.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
            //Applicable for API level 12 and above
            mSeekBarLayout.animate()
                    .translationYBy(mSeekBarLayout.getHeight())
                    .translationY(0)
                    .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
        else
            mSeekBarLayout.setVisibility(View.VISIBLE);

    }

    private void updateCurrentChapter(){
        int currentItemIndex = mMyViewPage.getCurrentItem();
        int currentChapter = mReaderAdapter.setCurrentChapter(currentItemIndex);

        //TODO : UPDATE SEEK BAR AND INDEX TABLE SELECTED ITEM.
    }
}
