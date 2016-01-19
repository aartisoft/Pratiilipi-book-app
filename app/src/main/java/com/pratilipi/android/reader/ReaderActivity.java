package com.pratilipi.android.reader;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pratilipi.android.pratilipi_and.DetailActivity;
import com.pratilipi.android.pratilipi_and.GetCallback;
import com.pratilipi.android.pratilipi_and.MainActivity;
import com.pratilipi.android.pratilipi_and.R;
import com.pratilipi.android.pratilipi_and.Widget.MyViewPager;
import com.pratilipi.android.pratilipi_and.datafiles.Content;
import com.pratilipi.android.pratilipi_and.datafiles.Pratilipi;
import com.pratilipi.android.pratilipi_and.util.AppUtil;
import com.pratilipi.android.pratilipi_and.util.ContentUtil;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class ReaderActivity extends ActionBarActivity implements SeekBar.OnSeekBarChangeListener {

    private static final String LOG_TAG = ReaderActivity.class.getSimpleName();
    public static final String PRATILIPI = "pratilipi";
    public static final String PARENT_ACTIVITY_CLASS_NAME = "parentActivityClassName";

    private Context mContext;
    private List<int[]> pageFragmentInfoList;
    // List<[chapterNo, startIndex, endIndex, index]>
    // Index : represent position of content in mContentList.

    private ActionBar mActionBar;
    private ProgressBar mProgressBar;

    private MyViewPager viewPager;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private PagerAdapter mPagerAdapter;
    private LinearLayout mSeekBarLayout;
    private SeekBar seekBar;
    private Pratilipi mPratilipi;
    private String mParentActivityClassName;
    private List<Content> mContentList;
    private int mReaderHeight;
    private int mViewTopPadding = 35;

    private ArrayList<String> mTitles;
    private ArrayList<Integer> mTitleChapters;
    private int mIndexSize;
    private int mInternetConnectionErrorCounter;


    @Override
    protected void onCreate( Bundle savedInstanceState ) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.reader_activity);

        mInternetConnectionErrorCounter = 0;

        mContext = this;
        mActionBar = getSupportActionBar();
        mSeekBarLayout = (LinearLayout) findViewById(R.id.reader_seekbar_layout);
//        seekBarLayout.setAlpha(0.4f);

        mProgressBar = ( ProgressBar ) findViewById( R.id.reader_progress_bar );

        Display display = getWindowManager().getDefaultDisplay();
        int height = display.getHeight();
        mReaderHeight = height - mViewTopPadding - mSeekBarLayout.getHeight();

        hideActionBar();
        //Make Content Appear Behind Status Bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        mPratilipi = (Pratilipi) getIntent().getSerializableExtra(PRATILIPI);
        mParentActivityClassName = getIntent().getStringExtra(PARENT_ACTIVITY_CLASS_NAME);

        setTitle(mPratilipi.getTitle());

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setElevation(1f);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setIcon(R.drawable.ic_logo);

        //Setting Reader Content Table
        Gson gson = new GsonBuilder().create();
        JsonArray indexArr = null;
        String indexString = mPratilipi.getIndex();
        if(indexString != null)
            indexArr = gson.fromJson(indexString, JsonElement.class).getAsJsonArray();
        mTitles = new ArrayList<>();
        mTitleChapters = new ArrayList<>();
        if (null != indexArr) {
            mIndexSize = indexArr.size();
            for (int i = 0; i < mIndexSize; i++) {
                JsonObject jsonObject = indexArr.get(i).getAsJsonObject();
                String title = jsonObject.get("title").toString();
//                Log.d("TITLE", title);
                mTitles.add(i, title.substring(1, title.length() - 1));
                mTitleChapters.add(i, Integer.parseInt(jsonObject.get("pageNo").toString()));
            }
        }

        //SETTING DRAWER IN READ ACTIVITY
        mDrawerLayout = (DrawerLayout) findViewById(R.id.reader_drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.reader_right_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mTitles));
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setContent(position+1);
                mDrawerLayout.closeDrawer(Gravity.RIGHT);
            }
        });


        viewPager = (MyViewPager) findViewById( R.id.viewPager );

        mContentList = new ArrayList<>();
        //FETCHING 1ST CHAPTER
        setContent(1);

        //SWIPE LEFT AND RIGHT
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            float startX;
            boolean performClick = false;
            boolean isSwiping = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX();


                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN: {
                        startX = x;
                        performClick = true;
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        float delta = Math.abs(startX - event.getX());
                        int currentItem = viewPager.getCurrentItem();
                        int currentChapter = pageFragmentInfoList.get(currentItem)[0];
                        if (currentItem == 0 && !isSwiping && delta > 10 && event.getX() > startX) {
                            /**Add previous chapter to start of list when user swipes right
                             * and current item is first item on list
                             * 'isSwiping' is used to prevent multiple times execution of following code.
                             * 'delta' is used to distinguish between swipe and click event.
                             */
                            isSwiping = true;
                            List<Content> previousChapter = getContentFromDb(currentChapter-1, null);
                            if(previousChapter != null){
                                pageFragmentInfoList.add(0,new int[]{currentChapter-1, 0, -1, mContentList.size()});
                                mContentList.add(mContentList.size(), previousChapter.get(0));
                                mPagerAdapter.notifyDataSetChanged();
                            }
                        } else if (delta > 1 && event.getX() < startX) {
//                            Log.i(LOG_TAG, "SWIPING LEFT");
                        }

                        //To capture click event.
                        if(delta > 1)
                            performClick = false;
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        Log.e(LOG_TAG, "Current Item : " + viewPager.getCurrentItem());
                        if(performClick){
                            if (actionBar.isShowing())
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

        // SeekBar
        seekBar = (SeekBar) findViewById( R.id.seekBar );
        seekBar.setMax( getChapterCount()-1 );
        seekBar.setOnSeekBarChangeListener( this );
    }

    private int getChapterCount(){
        String indexString = mPratilipi.getIndex();
        int chapterCount = 0;
        if(indexString == null)
            return 1;

        try{
            JSONArray indexArray = new JSONArray(indexString);
            chapterCount = indexArray.length();
        } catch(JSONException e){
            e.printStackTrace();
        }
//        Log.e(LOG_TAG, "Chapter Count : " + chapterCount);
        return chapterCount;
    }

    @Override
    public void onBackPressed() {
        // TODO: Press back again to exit reader
        super.onBackPressed();
    }

    @Override
    protected void onStop()  {
        super.onStop();
    }

// Option Menu

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        getMenuInflater().inflate(R.menu.menu_reader, menu);
        if (mIndexSize < 1) {
            menu.findItem(R.id.action_index).setVisible(false);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        int id = item.getItemId();
        ReaderPageFragment readerFragment = (ReaderPageFragment) getSupportFragmentManager()
                .findFragmentByTag(ReaderPageFragment.READER_FRAGMENT_TAG);
        if(id == R.id.action_font_size_decrease){
            float textSize = readerFragment.getFontSize();
//            Log.e(LOG_TAG, "Current Font size : " + textSize);
            if( textSize <= 30f )
                return false;

            float newTextSize = textSize - 5f;
            //STORE FONT SIZE IN SHARED PREFERENCE
            AppUtil.updateReaderFontSize(this, newTextSize);
            updatePageFragmentInfo(viewPager.getCurrentItem());
            readerFragment.setFontSize(newTextSize);
//            Log.e(LOG_TAG, "Current Font size : " + readerFragment.getFontSize());
            mPagerAdapter.notifyDataSetChanged();
        }

        if(id == R.id.action_font_size_increase){
            float textSize = readerFragment.getFontSize();
            if( textSize >= 50f )
                return false;

            float newTextSize = textSize + 5f;
            //STORE FONT SIZE IN SHARED PREFERENCE
            AppUtil.updateReaderFontSize(this, newTextSize);
            readerFragment.setFontSize(newTextSize);
            updatePageFragmentInfo(viewPager.getCurrentItem());
            mPagerAdapter.notifyDataSetChanged();
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


    private void updatePageFragmentInfo(int position){
//        Log.e(LOG_TAG, "Current Position : " + position);
        int[] pageFragmentInfo = pageFragmentInfoList.get(position);
        /**
         * Updating pageFragmentInfo[2] or endIndex to -1 forces fragment to be re - rendered and
         * hence new endIndex for updated font size is calculated.
         */
        pageFragmentInfo[2] = -1;
    }

    // PagerAdapter for ViewPager

    private class PagerAdapter extends FragmentStatePagerAdapter {

        private Map<Integer, ReaderPageFragment> positionFragmentMap;


        public PagerAdapter( FragmentManager fm ) {
            super( fm );
            positionFragmentMap = new HashMap<>();
        }

        @Override
        public int getCount() {
            return pageFragmentInfoList == null ? 0 : pageFragmentInfoList.size();
        }

        @Override
        public Fragment getItem( int position ) {
//            Log.e(LOG_TAG, "PagerAdapter getItem function. Position : " + position);
            ReaderPageFragment pageFragment = positionFragmentMap.get( position );

            if( pageFragment == null ) {
                int[] pageFragmentInfo = pageFragmentInfoList.get( position );
                String language =
                        mPratilipi.getLanguageName() != null ? mPratilipi.getLanguageName() : mPratilipi.getLanguageId();
                pageFragment = ReaderPageFragment.newInstance(
                        language,
                        mContentList.get(pageFragmentInfo[3]).getTextContent(),
                        pageFragmentInfo[1],
                        -1
                );

                positionFragmentMap.put( position, pageFragment );
            }
            return pageFragment;
        }

        @Override
        public void startUpdate( ViewGroup container ) {
//            Log.e(LOG_TAG, "PageFragmentInfoList : " + pageFragmentInfoList.size());
//            Log.e(LOG_TAG, "Current Item Number : " + viewPager.getCurrentItem());
            int currentItem = viewPager.getCurrentItem();
            if(pageFragmentInfoList.size() == 1 && currentItem > 0) {
                Log.e(LOG_TAG, "Current Item in startUpdate function : " + currentItem);
                /**
                 * This is hack to remove indexOutOfBound exception which system was throwing when
                 * and drawer item is clicked. mPagerAdapter is reset but viewPager views are not
                 * refreshed.
                 * TODO: FIND PROPER WAY TO DEAL WITH THIS PROBLEM.
                 */
                currentItem = 0;
            }
            int chapterNo = pageFragmentInfoList.get(currentItem)[0];
            if( seekBar.getProgress() != chapterNo-1 )
                seekBar.setProgress( chapterNo-1 );
        }


        public synchronized  void finishUpdate( ViewGroup container ) {
            super.finishUpdate(container);

            ViewPager viewPager = (ViewPager) container;
            int position = viewPager.getCurrentItem();

            ReaderPageFragment fragment = (ReaderPageFragment) getItem( position );
            LinearLayout view = (LinearLayout) fragment.getView();

            if( view == null ) {
                return;
            }

            int[] pageFragmentInfo = pageFragmentInfoList.get( position );
            if(pageFragmentInfo[2] == -1) {
                //Pre-fetching next and previous chapter.
                int currentChapter = pageFragmentInfoList.get(position)[0];
                int previousChapter = pageFragmentInfoList.get(0)[0];
                int nextChapter = pageFragmentInfoList.get(pageFragmentInfoList.size()-1)[0];
//                Log.e(LOG_TAG, "Next Chapter / Previous Chapter : " + nextChapter + "/" + previousChapter);
//                Log.e(LOG_TAG, "Previous / Current / Next : " + previousChapter + "/" + currentChapter + "/" + nextChapter);
                //fetch previous chapter. ASYNC TASK
                if(currentChapter > 1 && previousChapter == currentChapter) {
//                    Log.e(LOG_TAG, "Fetching previous chapter");
                    preFetchChapter(mContext, currentChapter - 1, true);
                }
                //fetch next chapter. ASYNC TASK
                if(currentChapter < getChapterCount() && nextChapter == currentChapter) {
//                    Log.e(LOG_TAG, "Fetching next chapter");
                    preFetchChapter(mContext, currentChapter + 1, false);
                }

                int endIndex = fragment.getEndIndex();
                if(endIndex == -1)
                    return;
                pageFragmentInfo[2] = endIndex;
                int contentLength = mContentList.get(pageFragmentInfo[3]).getTextContent().length();
                int[] nextPageFragmentInfo = null;
                if(position < pageFragmentInfoList.size()-1)
                    nextPageFragmentInfo = pageFragmentInfoList.get(position + 1);

                if(nextPageFragmentInfo != null && nextPageFragmentInfo[0] == pageFragmentInfo[0]){
                    //USED WHEN FONT SIZE IS CHANGED
                    nextPageFragmentInfo[1] = pageFragmentInfo[2];
                    nextPageFragmentInfo[2] = -1;
                    notifyDataSetChanged();
                } else {
                    if (endIndex < contentLength) {
                        int[] newPageFragmentInfo = {pageFragmentInfo[0], pageFragmentInfo[2] + 1, -1,pageFragmentInfo[3]};
                        pageFragmentInfoList.add(position + 1, newPageFragmentInfo);
                        notifyDataSetChanged();
                    }
                }
            }

        }


        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            for( Map.Entry<Integer, ReaderPageFragment> positionFragmentEntry : positionFragmentMap.entrySet() ) {
                int[] pageFragmentInfo = pageFragmentInfoList.get(positionFragmentEntry.getKey());
                int contentIndex = pageFragmentInfo[3];
                String contentString = mContentList.get(contentIndex).getTextContent();
                ReaderPageFragment pageFragment = positionFragmentEntry.getValue();
                pageFragment.setPage(contentString, pageFragmentInfo[1], pageFragmentInfo[2]);
            }
        }

    }


    // SeekBar.OnSeekBarChangeListener

    @Override
    public void onProgressChanged( SeekBar seekBar, int i, boolean b ) {
/*        if( i == chapterCount - 1 && viewPager.getCurrentItem() >= chapterPositionArray[i] )
            return;

        if( viewPager.getCurrentItem() >= chapterPositionArray[i] && viewPager.getCurrentItem() < chapterPositionArray[i + 1] )
            return;

        viewPager.setCurrentItem( chapterPositionArray[i] );*/

//        if(viewPager.getCurrentItem() == i) {
//            Log.e(LOG_TAG, "SeekBar is not dragged");
//            return;
//        } else {
//            Log.e(LOG_TAG, "Chapter : " + i+1);
//            setContent(i + 1);
//        }
    }

    @Override
    public void onStartTrackingTouch( SeekBar seekBar ) {}

    @Override
    public void onStopTrackingTouch( SeekBar seekBar ) {}

    @Nullable
    @Override
    public Intent getParentActivityIntent() {
        return getParentActivityIntentImpl();
    }

    private Intent getParentActivityIntentImpl(){
        Intent parentIntent = null;
        Log.v(LOG_TAG, "Parent Class Name : " + mParentActivityClassName);
        if( mParentActivityClassName.equals(DetailActivity.class.getSimpleName()))
            parentIntent = new Intent( this, DetailActivity.class);
        else if(mParentActivityClassName.equals(MainActivity.class.getSimpleName()))
            parentIntent = new Intent( this, MainActivity.class);

        parentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        return parentIntent;
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
        mSeekBarLayout.setVisibility(View.VISIBLE);
    }

    private void setContent(int chapterNo){
        mProgressBar.setVisibility(View.VISIBLE);
        if(mContentList != null && !mContentList.isEmpty())
            mContentList.clear();
//        Log.e(LOG_TAG, "Content List Length : " + mContentList.size());
//        Log.e(LOG_TAG, "Chapter No : " + chapterNo);
        mContentList = getContentFromDb(chapterNo, null);
        if(mContentList == null || mContentList.size() == 0)
            getContentFromServer(this, chapterNo);
        else{
            pageFragmentInfoList = new LinkedList<>();
            pageFragmentInfoList.add(new int[]{chapterNo, 0, -1, 0});

            // ViewPager & PagerAdapter
            mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
            viewPager.setAdapter(mPagerAdapter);
            viewPager.setCurrentItem(0); // This will trigger ViewPager.startUpdate and update the SeekBar
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private List<Content> getContentFromDb(Integer chapterNo, Integer pageNo){
//        Log.e(LOG_TAG, "getContentFromDb() called");
        ContentUtil contentUtil = new ContentUtil();
        Cursor cursor = contentUtil.getContentfromDb(this, mPratilipi, chapterNo, pageNo);
        if(!cursor.moveToFirst())
            return null;
        List<Content> contentList = contentUtil.createContentList(cursor);
        cursor.close();
        //ASSUMING contentList SIZE = 1
        Content tempContent = contentList.get(0);
        String title = "<br/><h1>" + mTitles.get(chapterNo-1) + "</h1><br/>";
        Log.e(LOG_TAG, "Title String : " + title);
        String htmlEndTag = "</html>";
        String updatedContentString = title + tempContent.getTextContent();// + htmlEndTag;
        tempContent.setTextContent(updatedContentString);
        contentList.add(0,tempContent);
        //REMOVE EXISTING CONTENT OBJECT
        if(contentList.size()>1)
            contentList.remove(1);

        return contentList;
    }

    private void getContentFromServer(final Context context, final Integer chapterNo){
        if(!AppUtil.isOnline(context))
            return;

        ContentUtil contentUtil = new ContentUtil();
        contentUtil.fetchChapterFromServer(context, mPratilipi, chapterNo, new GetCallback() {
            @Override
            public void done(boolean isSuccessful, String data) {
                Log.e(LOG_TAG, "this callback function. Is server call successful? " + isSuccessful);
                if (isSuccessful) {
                    List<Content> tempContentList = getContentFromDb(chapterNo, null);
                    if(tempContentList != null && tempContentList.size() != 0)
                        //change content of reader only when new content is fetched from server.
                        mContentList = tempContentList;
                    else {
                        Toast.makeText(context, "Unable to fetch content from server", Toast.LENGTH_LONG);
                        return;
                    }

                    pageFragmentInfoList = new LinkedList<>();
                    pageFragmentInfoList.add(new int[]{chapterNo, 0, -1, 0});

                    // ViewPager & PagerAdapter
                    mPagerAdapter = new PagerAdapter(getSupportFragmentManager());

                    viewPager.setAdapter(mPagerAdapter);
                    viewPager.setCurrentItem(0); // This will trigger ViewPager.startUpdate and update the SeekBar

                } else {
                    Toast.makeText(context, "Error while downloading content", Toast.LENGTH_LONG).show();
                }
                mProgressBar.setVisibility(View.GONE);
            }
        });
    }

    private void preFetchChapter(final Context context, final int chapter, final boolean isPrevious){
        if(!AppUtil.isOnline(context))
            return;

        List<Content> newChapterContentList = getContentFromDb(chapter, null);

        if(newChapterContentList != null && newChapterContentList.size()>0){
            if(!isPrevious) {
                Content content = newChapterContentList.get(0);
                pageFragmentInfoList.add(pageFragmentInfoList.size(), new int[]{chapter, 0, -1, mContentList.size()});
                mContentList.add(mContentList.size(), content);
                mPagerAdapter.notifyDataSetChanged();
            }
        } else {
            ContentUtil contentUtil = new ContentUtil();
            contentUtil.fetchChapterFromServer(context, mPratilipi, chapter, new GetCallback() {
                @Override
                public void done(boolean isSuccessful, String data) {
                    /**+
                     * isPrevious - check request type, below code is applicable only for next chapter
                     * mContentList != null - mContentList is cleared when user drags seekbar,
                     * this handle situation when server call was made before user drags seekbar
                     * isSuccessful - check whether server call was successful was or not.
                     */
                    if (!isPrevious && mContentList != null && isSuccessful) {
                        List<Content> nextChapterContent = getContentFromDb(chapter, null);
                        if(nextChapterContent != null && nextChapterContent.size()>0) {
                            Content content = nextChapterContent.get(0);
                            pageFragmentInfoList.add(pageFragmentInfoList.size(), new int[]{chapter, 0, -1, mContentList.size()});
                            mContentList.add(mContentList.size(), content);
                            mPagerAdapter.notifyDataSetChanged();
                        }
                    } else {
//                        Log.e(LOG_TAG, "Unable to fetch next chapter");
                    }
                }
            });
        }
    }
}
