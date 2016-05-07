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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pratilipi.android.pratilipi_and.R;
import com.pratilipi.android.pratilipi_and.Widget.MyViewPager;
import com.pratilipi.android.pratilipi_and.datafiles.Pratilipi;
import com.pratilipi.android.pratilipi_and.util.AppUtil;
import com.pratilipi.android.pratilipi_and.util.ShelfUtil;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rahul Ranjan on 2/29/2016.
 */
public class ReaderActivity extends AppCompatActivity  {

    private static final String LOG_TAG = ReaderActivity.class.getSimpleName();
    public static final String PRATILIPI = "pratilipi";
    public static final String PARENT_ACTIVITY_CLASS_NAME = "parentActivityClassName";

    private Context mContext;
    private ActionBar mActionBar;
    private LinearLayout mSeekBarLayout;
    private SeekBar mSeekBar;
    private TextView mSeekBarTextView;
    private ProgressBar mProgressBar;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private MyListViewAdapter mDrawerListAdapter;

    private MyViewPager mMyViewPage;
    private ReaderAdapter mReaderAdapter;

    private Pratilipi mPratilipi;

    private String mParentActivityClassName;

    private int mInternetConnectionErrorCounter;
    private int mSeekBarLayoutPosition;
    private int mIndexSize;
    private List<String> mTitles;

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
        mIndexSize = getChapterCount();

        setTitle(mPratilipi.getTitle());

        //Setting actionbar.
        mActionBar.setElevation(1f);
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setIcon(R.drawable.ic_logo);

        mMyViewPage = (MyViewPager) findViewById(R.id.reader_viewPager);
        mReaderAdapter = new ReaderAdapter(getSupportFragmentManager(), mContext);
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
                        int viewPagerSize = mMyViewPage.getAdapter().getCount();
                        if (!isSwiping && delta > 30 && event.getX() > startX) {
                            /**Add previous chapter to start of list when user swipes right
                             * and current item is first item on list
                             * 'isSwiping' is used to prevent multiple times execution of following code.
                             * 'delta' is used to distinguish between swipe and click event.
                             */
                            isSwiping = true;
                            if(currentItem > 0) {
                                mReaderAdapter.setCurrentChapter(currentItem - 1);
                                Log.e(LOG_TAG, "SWIPING RIGHT. Current Item / Current Chapter : " + currentItem + "/" + mReaderAdapter.getCurrentChapter());
//                                setSeekBarTextView();
                                mReaderAdapter.getPreviousChapter();
                            }
                            //TODO : IF 1ST FRAGMENT IS VISIBLE, call getDataFromDb for previous chapter

                        } else if (delta > 30 && !isSwiping && event.getX() < startX) {
                            isSwiping = true;
                            if(currentItem < viewPagerSize-1) {
                                mReaderAdapter.setCurrentChapter(currentItem + 1);
                                Log.e(LOG_TAG, "SWIPING LEFT. Current Item / Total Size : " + currentItem + "/" + mReaderAdapter.getCurrentChapter());
//                                setSeekBarTextView();
                                mReaderAdapter.getNextChapter();
                            }
                            //TODO : IF LAST FRAGMENT IS VISIBLE, call getDataFromDb for next chapter
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


        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mSeekBar.setMax(getChapterCount() - 1);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //'progress' ranges - from 0 to chapterCount-1.
                mReaderAdapter.setContent(mPratilipi, getChapterCount(), progress + 1);
                setSeekBarTextView();
                mDrawerListAdapter.setSelectedItem(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mSeekBarTextView = (TextView) findViewById(R.id.seekBar_text);

        //SETTING DRAWER IN READ ACTIVITY.
        mDrawerLayout = (DrawerLayout) findViewById(R.id.reader_drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.reader_right_drawer);
        mDrawerList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        setIndexList(); //Setting Index table content
        //Index item onClickEvent.
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDrawerListAdapter.setSelectedItem(position);
                mReaderAdapter.setContent(mPratilipi, mIndexSize, position + 1);
                mMyViewPage.setCurrentItem(0);  //Set first fragments
                mSeekBar.setProgress(position); //update seek bar position
                setSeekBarTextView();   //update seek bar text
                mDrawerLayout.closeDrawer(Gravity.RIGHT);
            }
        });

    }

    @Nullable
    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    @Override
    protected void onResume() {
        int[] readingLocation = ShelfUtil.getReadingLocation(mContext, mPratilipi.getPratilipiId());
        if(readingLocation == null){
            mReaderAdapter.setContent(mPratilipi, getChapterCount(), 1);
            mMyViewPage.setCurrentItem(0);
            mSeekBar.setProgress(0); //Set seek bar position
            mDrawerListAdapter.setSelectedItem(0);
        } else {
            int currentChapter = readingLocation[0];
            int currentFragment = readingLocation[1];
            mReaderAdapter.setContent(mPratilipi, getChapterCount(), currentChapter);
            mMyViewPage.setCurrentItem(currentFragment);
            mSeekBar.setProgress(currentChapter - 1); //Set seek bar position
            mDrawerListAdapter.setSelectedItem(currentChapter - 1);
        }
        setSeekBarTextView();   //set seek bar text for first time.

        //OnPageChangeListener
        mMyViewPage.addOnPageChangeListener(new MyViewPager.OnPageChangeListener() {
            int lastPosition;
            int currentPosition;

            @Override
            public void onPageSelected(int position) {
                //Called when a page is selected.
                setSeekBarTextView();
                currentPosition = position;
                Log.e(LOG_TAG, "Last Position / Current Position : " + lastPosition + " / " + currentPosition);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Called when page is scrolled
                lastPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //Called when scroll state of a page is changed
            }
        });

        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reader, menu);
        if (mIndexSize <= 1) {
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
    protected void onPause() {
        super.onPause();
        //SAVE CURRENT CHAPTER AND FRAGMENT
        int currentChapter = mReaderAdapter.getCurrentChapter();
        int currentFragment = mReaderAdapter.getCurrentFragment(mMyViewPage.getCurrentItem());
        ShelfUtil.createOrUpdateReadingLocation(mContext, mPratilipi.getPratilipiId(), currentChapter, currentFragment);
        mMyViewPage.clearOnPageChangeListeners();
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

    private void setSeekBarTextView(){
        int chapterCount = getChapterCount();
        int currentChapter = mReaderAdapter.getCurrentChapter();
        String text = "Chapter " + currentChapter + " of " + chapterCount;
        mSeekBarTextView.setText(text);
    }

    private int getChapterCount(){
        String indexString = mPratilipi.getIndex();
        int chapterCount = 0;
        if(indexString == null)
            return 1;

        try{
            JSONArray indexArray = new JSONArray(indexString);
            Log.i(LOG_TAG, "Array Length : " + indexArray.length());
            chapterCount = indexArray.length();
        } catch(JSONException e){
            e.printStackTrace();
        }
        return chapterCount;
    }

    private void setIndexList(){

        //Setting Reader Content Table
        Gson gson = new GsonBuilder().create();
        JsonArray indexArr = null;
        String indexString = mPratilipi.getIndex();
        if(indexString != null)
            indexArr = gson.fromJson(indexString, JsonElement.class).getAsJsonArray();
        mTitles = new ArrayList<>();
        if (null != indexArr) {
            mIndexSize = indexArr.size();
            for (int i = 0; i < mIndexSize; i++) {
                JsonObject jsonObject = indexArr.get(i).getAsJsonObject();
                String title = jsonObject.get("title").toString();

                mTitles.add(i, title.substring(1, title.length() - 1));
            }
        }

        mDrawerListAdapter = new MyListViewAdapter(this, mTitles);
        mDrawerList.setAdapter(mDrawerListAdapter);

    }
}
