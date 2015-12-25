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
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
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

    private Context mContext;
    private List<int[]> pageFragmentInfoList; // List<[chapterNo, startIndex, endIndex]>

    private ActionBar mActionBar;

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

        mPratilipi = (Pratilipi) getIntent().getSerializableExtra(DetailActivity.PRATILIPI);
        mParentActivityClassName = getIntent().getStringExtra(DetailActivity.PARENT_ACTIVITY_CLASS_NAME);

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
                Log.d("TITLE", title);
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
                Log.e(LOG_TAG, "Chapter Clicked : " + mTitles.get(position));
                Log.e(LOG_TAG, "Chapter Clicked : " + mTitleChapters.get(position));
                setContent(mTitleChapters.get(position));
            }
        });


        viewPager = (MyViewPager) findViewById( R.id.viewPager );

        mContentList = new ArrayList<>();
        //FETCHING 1ST CHAPTER
        setContent(1);


        viewPager.setOnViewPagerClickListener(new MyViewPager.OnClickListener() {
            @Override
            public void onViewPagerClick(ViewPager viewPager) {

                if (actionBar.isShowing())
                    hideActionBar();
                else
                    showActionBar();

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
            return pageFragmentInfoList.size();
        }

        @Override
        public Fragment getItem( int position ) {
//            Log.e(LOG_TAG, "PagerAdapter getItem function. Position : " + position);
            ReaderPageFragment pageFragment = positionFragmentMap.get( position );


            if( pageFragment == null ) {
                int[] pageFragmentInfo = pageFragmentInfoList.get( position );
                pageFragment = ReaderPageFragment.newInstance(
                        mPratilipi.getLanguageName(),
                        mContentList.get(pageFragmentInfo[0]-1).getTextContent(),
                        pageFragmentInfo[1],
                        -1
                );

                positionFragmentMap.put( position, pageFragment );
            }
            return pageFragment;
        }

        @Override
        public void startUpdate( ViewGroup container ) {
            int chapterNo = pageFragmentInfoList.get( viewPager.getCurrentItem() )[0];
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
//                Log.e(LOG_TAG, "Previous / Current / Next : " + previousChapter + "/" + currentChapter + "/" + nextChapter);
                //fetch previous chapter. ASYNC TASK
                if(currentChapter > 1 && previousChapter == currentChapter) {
                    Log.e(LOG_TAG, "Fetching previous chapter");
                    preFetchChapter(mContext, currentChapter - 1);
                }
                //fetch next chapter. ASYNC TASK
                if(currentChapter < getChapterCount()-1 && nextChapter == currentChapter) {
                    Log.e(LOG_TAG, "Fetching next chapter");
                    preFetchChapter(mContext, currentChapter + 1);
                }

                int endIndex = fragment.getEndIndex();
                if(endIndex == -1)
                    return;
                pageFragmentInfo[2] = endIndex;
                int contentLength = mContentList.get(pageFragmentInfo[0]-1).getTextContent().length();
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
                        int[] newPageFragmentInfo = {pageFragmentInfo[0], pageFragmentInfo[2] + 1, -1};
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
                int chapterNo = pageFragmentInfo[0];
                String contentString = mContentList.get(chapterNo-1).getTextContent();
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

    private void setContent(int chapter){
        mContentList = getContentFromDb(chapter, null);
        if(mContentList == null || mContentList.size() == 0)
            getContentFromServer(this, chapter);
        else{
            int contentListSize = mContentList.size();
            pageFragmentInfoList = new LinkedList<>();
            for( int i = 1; i <= contentListSize; i++ )
                pageFragmentInfoList.add(new int[]{i, 0, -1});

            // ViewPager & PagerAdapter
            mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
            viewPager.setAdapter(mPagerAdapter);
            viewPager.setCurrentItem(0); // This will trigger ViewPager.startUpdate and update the SeekBar
        }
    }

    private List<Content> getContentFromDb(Integer chapterNo, Integer pageNo){
        Log.e(LOG_TAG, "getContentFromDb() called");
        ContentUtil contentUtil = new ContentUtil();
        Cursor cursor = contentUtil.getContentfromDb(this, mPratilipi, chapterNo, pageNo);
        Log.e(LOG_TAG, "Result Count : " + cursor.getCount());
        return contentUtil.createContentList(cursor);
    }

    private void getContentFromServer(final Context context, final Integer chapterNo){
        if(!AppUtil.isOnline(context))
            return;

        Log.e(LOG_TAG, "getContentFromServer() called");
        ContentUtil contentUtil = new ContentUtil();
        contentUtil.fetchChapterFromServer(context, mPratilipi, chapterNo, new GetCallback() {
            @Override
            public void done(boolean isSuccessful, String data) {
                Log.e(LOG_TAG, "this callback function. Is server call successful? " + isSuccessful);
                if (isSuccessful) {
                    List<Content> tempContentList = getContentFromDb(chapterNo, null);
                    if(tempContentList != null)
                        //change content of reader only when new content is fetched from server.
                        mContentList = tempContentList;
                    else {
                        Toast.makeText(context, "Unable to fetch content from server", Toast.LENGTH_LONG);
                        return;
                    }

                    int contentListSize = mContentList == null ? 0 : mContentList.size();
                    pageFragmentInfoList = new LinkedList<>();
                    for (int i = 1; i <= contentListSize; i++)
                        pageFragmentInfoList.add(new int[]{i, 0, -1});

                    // ViewPager & PagerAdapter
                    mPagerAdapter = new PagerAdapter(getSupportFragmentManager());

                    viewPager.setAdapter(mPagerAdapter);
                    viewPager.setCurrentItem(0); // This will trigger ViewPager.startUpdate and update the SeekBar

                } else {
                    Toast.makeText(context, "Error while downloading content", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void preFetchChapter(final Context context, final int chapter){
        if(!AppUtil.isOnline(context))
            return;

        List<Content> nextChapterContentList = getContentFromDb(chapter, null);
        if( nextChapterContentList != null && nextChapterContentList.size()>0){
            Log.e(LOG_TAG, "Next chapter is present in database");
            Content content = nextChapterContentList.get(0);
            mContentList.add(mContentList.size(), content);
            pageFragmentInfoList.add(pageFragmentInfoList.size(), new int[]{chapter, 0, -1});
            mPagerAdapter.notifyDataSetChanged();
        } else {
            ContentUtil contentUtil = new ContentUtil();
            contentUtil.fetchChapterFromServer(context, mPratilipi, chapter, new GetCallback() {
                @Override
                public void done(boolean isSuccessful, String data) {
                    Log.e(LOG_TAG, "Chapter is prefetched : " + isSuccessful);
                    if (isSuccessful) {
                        List<Content> nextChapterContent = getContentFromDb(chapter, null);
                        if(nextChapterContent != null && nextChapterContent.size()>0) {
                            Content content = nextChapterContent.get(0);
                            pageFragmentInfoList.add(pageFragmentInfoList.size(), new int[]{chapter, 0, -1});
                            mContentList.add(mContentList.size(), content);
                            mPagerAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e(LOG_TAG, "Unable to fetch next chapter");
                    }
                }
            });
        }
    }
}
