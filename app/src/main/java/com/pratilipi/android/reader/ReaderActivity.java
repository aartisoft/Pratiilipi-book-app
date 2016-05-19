package com.pratilipi.android.reader;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import com.pratilipi.android.pratilipi_and.util.ContentUtil;
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
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private MyListViewAdapter mDrawerListAdapter;
    private ProgressBar mProgressBar;

    private MyViewPager mMyViewPage;
    private ReaderAdapter mReaderAdapter;

    private Pratilipi mPratilipi;

    private String mParentActivityClassName;

    private int mInternetConnectionErrorCounter;
    private int mSeekBarLayoutPosition;
    private int mIndexSize;
    private List<String> mTitles;

//    private ShareActionProvider mShareActionProvider;

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

        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mSeekBar.setMax(getChapterCount() - 1);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //'progress' ranges - from 0 to chapterCount-1.
                //call setContent function only when change in progress is initiated by the user
                if(fromUser) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mReaderAdapter.setContent(mPratilipi, getChapterCount(), progress + 1);
                    mMyViewPage.setCurrentItem(0);
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
                    updateSeekBar();
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
//                mSeekBar.setProgress(position); //update seek bar position
                updateSeekBar();   //update seek bar text
                mDrawerLayout.closeDrawer(Gravity.RIGHT);
            }
        });

        //View pager and reader adapter initialization
        mMyViewPage = (MyViewPager) findViewById(R.id.reader_viewPager);
        mReaderAdapter = new ReaderAdapter(getSupportFragmentManager(), mContext, mTitles);
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
                            if (currentItem > 0) {
                                mReaderAdapter.setCurrentChapter(currentItem - 1);
                                ContentUtil contentUtil = new ContentUtil();
                                mReaderAdapter.getContentFromServer(contentUtil, mReaderAdapter.getCurrentChapter()-1, ReaderAdapter.PREVIOUS_CHAPTER);
                                updateSeekBar();
                                mDrawerListAdapter.setSelectedItem(mReaderAdapter.getCurrentChapter() - 1);
                                //Fetch previous chapter content from server if not present locally
                                mReaderAdapter.getPreviousChapter();
                            } else {
                                //IF 1ST FRAGMENT IS VISIBLE, call getDataFromDb for previous chapter
                                mProgressBar.setVisibility(View.VISIBLE);
                                //Fetch previous chapter from DB and set it in ViewPager
                                final int fragmentCount = mReaderAdapter.setPreviousChapter();
                                Log.e(LOG_TAG, "Touch event handler. Fragment Count : " + fragmentCount);
                                if(fragmentCount>0){
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            mMyViewPage.setCurrentItem(fragmentCount-1);
                                            Log.e(LOG_TAG, "Touch Event. ViewPager Current Item : " + fragmentCount);
                                            mReaderAdapter.setCurrentChapter(fragmentCount - 1);
                                            updateSeekBar();
                                            mDrawerListAdapter.setSelectedItem(mReaderAdapter.getCurrentChapter()-1);
                                        }
                                    }, 500);
                                    Log.e(LOG_TAG, "After delay");
                                }
                                mProgressBar.setVisibility(View.INVISIBLE);
                            }

                        } else if (delta > 30 && !isSwiping && event.getX() < startX) {
                            isSwiping = true;
                            if (currentItem < viewPagerSize - 1) {
                                mReaderAdapter.setCurrentChapter(currentItem + 1);
                                updateSeekBar();
                                mDrawerListAdapter.setSelectedItem(mReaderAdapter.getCurrentChapter() - 1);
                                mReaderAdapter.getNextChapter();
                            } else{
                                //IF LAST FRAGMENT IS VISIBLE, call getDataFromDb for next chapter
                                mProgressBar.setVisibility(View.VISIBLE);
                                mReaderAdapter.getNextChapter();
                                viewPagerSize = mMyViewPage.getChildCount();
                                if(currentItem < viewPagerSize-1){
                                    mMyViewPage.setCurrentItem(currentItem+1);
                                    mReaderAdapter.setCurrentChapter(currentItem+1);
                                    updateSeekBar();
                                    mDrawerListAdapter.setSelectedItem(mReaderAdapter.getCurrentChapter()-1);
                                }
                                mProgressBar.setVisibility(View.INVISIBLE);
                            }
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
    protected void onResume() {
        int[] readingLocation = ShelfUtil.getReadingLocation(mContext, mPratilipi.getPratilipiId());
        int currentChapter, currentFragment;
        if(readingLocation == null){
            currentChapter = 1;
            currentFragment = 0;
        } else {
            currentChapter = readingLocation[0];
            currentFragment = readingLocation[1];
        }
        mProgressBar.setVisibility(View.VISIBLE);
        mReaderAdapter.setContent(mPratilipi, getChapterCount(), currentChapter);
        mMyViewPage.setCurrentItem(currentFragment);
        mDrawerListAdapter.setSelectedItem(currentChapter - 1);
        updateSeekBar();   //set seek bar text for first time.
        mProgressBar.setVisibility(View.INVISIBLE);

        //OnPageChangeListener
        mMyViewPage.addOnPageChangeListener(new MyViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                //Called when a page is selected.
                updateSeekBar();
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Called when page is scrolled
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

//        MenuItem menuItem = menu.findItem(R.id.action_share);
//        mShareActionProvider = new ShareActionProvider(mContext);
//        mShareActionProvider.setShareIntent(createShareForecastIntent());
//        MenuItemCompat.setActionProvider(menuItem, mShareActionProvider);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        int id = item.getItemId();
        if(id == R.id.action_font_size_decrease){
            float currentFontSize = AppUtil.getReaderFontSize(mContext);
            //TODO : Case missing - when font size is not present in shared preference.
            if( currentFontSize <= 60f )
                return false;

            //STORE FONT SIZE IN SHARED PREFERENCE
            AppUtil.updateReaderFontSize(this, currentFontSize - 2f);
            //Notify Adaptor about the change
            mReaderAdapter.setFontSizeChangeFlag(true);
        }

        if(id == R.id.action_font_size_increase){
            float currentFontSize = AppUtil.getReaderFontSize(mContext);
            //TODO : Case missing - when font size is not present in shared preference.
            if( currentFontSize >= 70f )
                return false;

            //STORE FONT SIZE IN SHARED PREFERENCE
            AppUtil.updateReaderFontSize(this, currentFontSize + 2f);
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

    private Intent createShareForecastIntent(){
        Intent shareIntent = new Intent( Intent.ACTION_SEND );
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        String share = "You might like this book." + mPratilipi.getPageUrl();
        shareIntent.putExtra(Intent.EXTRA_TEXT,share);

        return  shareIntent;
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

    private void updateSeekBar(){
        int chapterCount = getChapterCount();
        int currentChapter = mReaderAdapter.getCurrentChapter();
        String text = "Chapter " + currentChapter + " of " + chapterCount;
        mSeekBarTextView.setText(text);
        mSeekBar.setProgress(currentChapter-1);
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
