package com.pratilipi.android.reader;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.pratilipi.android.pratilipi_and.DetailActivity;
import com.pratilipi.android.pratilipi_and.MainActivity;
import com.pratilipi.android.pratilipi_and.R;
import com.pratilipi.android.pratilipi_and.Widget.MyViewPager;
import com.pratilipi.android.pratilipi_and.datafiles.Pratilipi;
import com.pratilipi.android.pratilipi_and.util.AppUtil;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class ReaderActivity extends ActionBarActivity implements SeekBar.OnSeekBarChangeListener {

    private static final String LOG_TAG = ReaderActivity.class.getSimpleName();

    private PratilipiData pratilipiData;
    private List<int[]> pageFragmentInfoList; // List<[chapterNo, pageNo, pageletStart, pageletCount]>

    private MyViewPager viewPager;
    private PagerAdapter mPagerAdapter;
    private SeekBar seekBar;
    private Pratilipi mPratilipi;
    private String mParentActivityClassName;
    private int mReaderHeight;
    private int mViewTopPadding = 35;


    @Override
    protected void onCreate( Bundle savedInstanceState ) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.reader_activity);

        final LinearLayout seekBarLayout = (LinearLayout) findViewById(R.id.reader_seekbar_layout);
//        seekBarLayout.setAlpha(0.4f);

        Display display = getWindowManager().getDefaultDisplay();
        int height = display.getHeight();
//        Log.e(LOG_TAG, "Display Height " + height);
//        Log.e(LOG_TAG, "Status bar Height " + getSupportActionBar().getHeight());
//        Log.e(LOG_TAG, "SeekBar Height : " + seekBarLayout.getHeight());
        mReaderHeight = height - mViewTopPadding - seekBarLayout.getHeight();
//        Log.e(LOG_TAG, "Reader Height " + mReaderHeight);

        hideActionBar(getSupportActionBar(), seekBarLayout);
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

//        ContentUtil.getContent(this, 4875536748773376L, 1, 1, "PRATILIPI", new GetCallback() {
//            @Override
//            public void done(boolean isSuccessful, String data) {
//                Log.e(LOG_TAG, "This is it");
//            }
//        });

        PratilipiData.Pagelet[] page1Pagelets = {
                new PratilipiData.Pagelet( "Page 1, Pagelet 1 ... For example, on a handset device it might be appropriate to display just one fragment at a time for a single-pane user interface. Conversely, you may want to set fragments side-by-side on a tablet which has a wider screen size to display more information to the user.", PratilipiData.PageletType.TEXT ),
                new PratilipiData.Pagelet( "Page 1, Pagelet 2 ... For example, on a handset device it might be appropriate to display just one fragment at a time for a single-pane user interface. Conversely, you may want to set fragments side-by-side on a tablet which has a wider screen size to display more information to the user." , PratilipiData.PageletType.TEXT ),
                new PratilipiData.Pagelet( "Page 1, Pagelet 3 ... For example, on a handset device it might be appropriate to display just one fragment at a time for a single-pane user interface. Conversely, you may want to set fragments side-by-side on a tablet which has a wider screen size to display more information to the user.", PratilipiData.PageletType.TEXT ),
                new PratilipiData.Pagelet( "Page 1, Pagelet 4 ... For example, on a handset device it might be appropriate to display just one fragment at a time for a single-pane user interface. Conversely, you may want to set fragments side-by-side on a tablet which has a wider screen size to display more information to the user.", PratilipiData.PageletType.TEXT ),
                new PratilipiData.Pagelet( "Page 1, Pagelet 5 ... For example, on a handset device it might be appropriate to display just one fragment at a time for a single-pane user interface. Conversely, you may want to set fragments side-by-side on a tablet which has a wider screen size to display more information to the user.", PratilipiData.PageletType.TEXT ),
                new PratilipiData.Pagelet( "Page 1, Pagelet 6 ... For example, on a handset device it might be appropriate to display just one fragment at a time for a single-pane user interface. Conversely, you may want to set fragments side-by-side on a tablet which has a wider screen size to display more information to the user.", PratilipiData.PageletType.TEXT ),
                new PratilipiData.Pagelet( "Page 1, Pagelet 7 ... For example, on a handset device it might be appropriate to display just one fragment at a time for a single-pane user interface. Conversely, you may want to set fragments side-by-side on a tablet which has a wider screen size to display more information to the user.", PratilipiData.PageletType.TEXT ),
                new PratilipiData.Pagelet( "Page 1, Pagelet 8 ... For example, on a handset device it might be appropriate to display just one fragment at a time for a single-pane user interface. Conversely, you may want to set fragments side-by-side on a tablet which has a wider screen size to display more information to the user.", PratilipiData.PageletType.TEXT ),
                new PratilipiData.Pagelet( "Page 1, Pagelet 9 ... For example, on a handset device it might be appropriate to display just one fragment at a time for a single-pane user interface. Conversely, you may want to set fragments side-by-side on a tablet which has a wider screen size to display more information to the user.", PratilipiData.PageletType.TEXT ),
                new PratilipiData.Pagelet( "Page 1, Pagelet 10 ... For example, on a handset device it might be appropriate to display just one fragment at a time for a single-pane user interface. Conversely, you may want to set fragments side-by-side on a tablet which has a wider screen size to display more information to the user.", PratilipiData.PageletType.TEXT ),
                new PratilipiData.Pagelet( "Page 1, Pagelet 11 ... For example, on a handset device it might be appropriate to display just one fragment at a time for a single-pane user interface. Conversely, you may want to set fragments side-by-side on a tablet which has a wider screen size to display more information to the user.", PratilipiData.PageletType.TEXT ),
                new PratilipiData.Pagelet( "Page 1, Pagelet 12 ... For example, on a handset device it might be appropriate to display just one fragment at a time for a single-pane user interface. Conversely, you may want to set fragments side-by-side on a tablet which has a wider screen size to display more information to the user.", PratilipiData.PageletType.TEXT ),
        };

        PratilipiData.Pagelet[] page2Pagelets = {
                new PratilipiData.Pagelet( "Page 2, Pagelet 1 ... For example, on a handset device it might be appropriate to display just one fragment at a time for a single-pane user interface. Conversely, you may want to set fragments side-by-side on a tablet which has a wider screen size to display more information to the user.", PratilipiData.PageletType.TEXT ),
                new PratilipiData.Pagelet( "Page 2, Pagelet 2 ... For example, on a handset device it might be appropriate to display just one fragment at a time for a single-pane user interface. Conversely, you may want to set fragments side-by-side on a tablet which has a wider screen size to display more information to the user.", PratilipiData.PageletType.TEXT ),
                new PratilipiData.Pagelet( "Page 2, Pagelet 3 ... For example, on a handset device it might be appropriate to display just one fragment at a time for a single-pane user interface. Conversely, you may want to set fragments side-by-side on a tablet which has a wider screen size to display more information to the user.", PratilipiData.PageletType.TEXT ),
                new PratilipiData.Pagelet( "Page 2, Pagelet 4 ... For example, on a handset device it might be appropriate to display just one fragment at a time for a single-pane user interface. Conversely, you may want to set fragments side-by-side on a tablet which has a wider screen size to display more information to the user.", PratilipiData.PageletType.TEXT ),
                new PratilipiData.Pagelet( "Page 2, Pagelet 5 ... For example, on a handset device it might be appropriate to display just one fragment at a time for a single-pane user interface. Conversely, you may want to set fragments side-by-side on a tablet which has a wider screen size to display more information to the user.", PratilipiData.PageletType.TEXT ),
                new PratilipiData.Pagelet( "Page 2, Pagelet 6 ... For example, on a handset device it might be appropriate to display just one fragment at a time for a single-pane user interface. Conversely, you may want to set fragments side-by-side on a tablet which has a wider screen size to display more information to the user.", PratilipiData.PageletType.TEXT ),
                new PratilipiData.Pagelet( "Page 2, Pagelet 7 ... For example, on a handset device it might be appropriate to display just one fragment at a time for a single-pane user interface. Conversely, you may want to set fragments side-by-side on a tablet which has a wider screen size to display more information to the user.", PratilipiData.PageletType.TEXT ),
                new PratilipiData.Pagelet( "Page 2, Pagelet 8 ... For example, on a handset device it might be appropriate to display just one fragment at a time for a single-pane user interface. Conversely, you may want to set fragments side-by-side on a tablet which has a wider screen size to display more information to the user.", PratilipiData.PageletType.TEXT ),
                new PratilipiData.Pagelet( "Page 2, Pagelet 9 ... For example, on a handset device it might be appropriate to display just one fragment at a time for a single-pane user interface. Conversely, you may want to set fragments side-by-side on a tablet which has a wider screen size to display more information to the user.", PratilipiData.PageletType.TEXT ),
                new PratilipiData.Pagelet( "Page 2, Pagelet 10 ... For example, on a handset device it might be appropriate to display just one fragment at a time for a single-pane user interface. Conversely, you may want to set fragments side-by-side on a tablet which has a wider screen size to display more information to the user.", PratilipiData.PageletType.TEXT ),
                new PratilipiData.Pagelet( "Page 2, Pagelet 11 ... For example, on a handset device it might be appropriate to display just one fragment at a time for a single-pane user interface. Conversely, you may want to set fragments side-by-side on a tablet which has a wider screen size to display more information to the user.", PratilipiData.PageletType.TEXT ),
                new PratilipiData.Pagelet( "Page 2, Pagelet 12 ... For example, on a handset device it might be appropriate to display just one fragment at a time for a single-pane user interface. Conversely, you may want to set fragments side-by-side on a tablet which has a wider screen size to display more information to the user.", PratilipiData.PageletType.TEXT ),
        };

        pratilipiData = new PratilipiData( new PratilipiData.Chapter[] {
                new PratilipiData.Chapter( "Chapter 1", new PratilipiData.Page[] {
                        new PratilipiData.Page( page1Pagelets )
                }),
                new PratilipiData.Chapter( "Chapter 2", new PratilipiData.Page[] {
                        new PratilipiData.Page( page2Pagelets )
                }),
                new PratilipiData.Chapter( "Chapter 3", new PratilipiData.Page[] {
                        new PratilipiData.Page( page1Pagelets )
                }),
                new PratilipiData.Chapter( "Chapter 4", new PratilipiData.Page[] {
                        new PratilipiData.Page( page2Pagelets )
                }),
        } );




        // Initializing Variables
        pageFragmentInfoList = new LinkedList<>();
        for( int i = 0; i < pratilipiData.getChapterCount(); i++ )
            pageFragmentInfoList.add( new int[] { i, 0, 0, pratilipiData.getChapter(i).getPage(0).getPageletCount() } );

        // ViewPager & PagerAdapter
        mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        viewPager = (MyViewPager) findViewById( R.id.viewPager );
        viewPager.setAdapter(mPagerAdapter);
        viewPager.setCurrentItem(0); // This will trigger ViewPager.startUpdate and update the SeekBar



        viewPager.setOnViewPagerClickListener(new MyViewPager.OnClickListener() {
            @Override
            public void onViewPagerClick(ViewPager viewPager) {

                if (actionBar.isShowing())
                    hideActionBar(actionBar, seekBarLayout);
                else
                    showActionBar(actionBar, seekBarLayout);

            }
        });

        // SeekBar
        seekBar = (SeekBar) findViewById( R.id.seekBar );
        seekBar.setMax( pratilipiData.getChapterCount() - 1 );
        seekBar.setOnSeekBarChangeListener( this );
    }

    @Override
    public void onBackPressed() {
        // TODO: Press back again to exit reader
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        Log.e(LOG_TAG, "Reader Activity stoped");
        super.onStop();
    }

// Option Menu

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        getMenuInflater().inflate(R.menu.menu_reader, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        int id = item.getItemId();
        ReaderPageFragment readerFragment = (ReaderPageFragment) getSupportFragmentManager()
                .findFragmentByTag(ReaderPageFragment.READER_FRAGMENT_TAG);
        if(id == R.id.action_font_size_decrease){
            float textSize = readerFragment.getFontSize();
            Log.e(LOG_TAG, "Decrease Font Size / Current Text Size : " + textSize);
            if( textSize <= 30f )
                return false;

            float newTextSize = textSize - 5f;
            //STORE FONT SIZE IN SHARED PREFERENCE
            AppUtil.updateReaderFontSize(this, newTextSize);
            Log.e(LOG_TAG, "New Text Size : " + newTextSize);
            readerFragment.setFontSize(newTextSize);
            mPagerAdapter.notifyDataSetChanged();
        }

        if(id == R.id.action_font_size_increase){
            float textSize = readerFragment.getFontSize();
            Log.e(LOG_TAG, "Increase Font Size / Current Text Size : " + textSize);
            if( textSize >= 50f )
                return false;

            float newTextSize = textSize + 5f;
            //STORE FONT SIZE IN SHARED PREFERENCE
            AppUtil.updateReaderFontSize(this, newTextSize);
            Log.e(LOG_TAG, "New Text Size : " + newTextSize);
            readerFragment.setFontSize(newTextSize);
            mPagerAdapter.notifyDataSetChanged();
        }

        return super.onOptionsItemSelected(item);
    }



    // PagerAdapter for ViewPager

    private class PagerAdapter extends FragmentStatePagerAdapter {

        private Map<Integer, ReaderPageFragment> positionFragmentMap;


        public PagerAdapter( FragmentManager fm ) {
            super( fm );
            positionFragmentMap = new HashMap<>();
            Log.e(LOG_TAG, "PagerAdapter Constructor");
        }

        @Override
        public int getCount() {
            return pageFragmentInfoList.size();
        }

        @Override
        public Fragment getItem( int position ) {
//            Log.e(LOG_TAG, "PagerAdapter getItem function");
            ReaderPageFragment pageFragment = positionFragmentMap.get( position );
            if( pageFragment == null ) {
                int[] pageFragmentInfo = pageFragmentInfoList.get( position );
                pageFragment = new ReaderPageFragment();
//                getSupportFragmentManager()
//                        .beginTransaction()
//                        .add(R.id.reader_fragment, pageFragment)
//                        .commit();
                pageFragment.setPagelets(pratilipiData.getChapter(pageFragmentInfo[0])
                        .getPage(pageFragmentInfo[1])
                        .getPagelets(pageFragmentInfo[2], pageFragmentInfo[3]));
                positionFragmentMap.put( position, pageFragment );
            }
            return pageFragment;
        }

        @Override
        public void startUpdate( ViewGroup container ) {
//            Log.e(LOG_TAG, "PagerAdapter startUpdate function");
            int chapterNo = pageFragmentInfoList.get( viewPager.getCurrentItem() )[0];
            if( seekBar.getProgress() != chapterNo )
                seekBar.setProgress( chapterNo );
        }

        public synchronized  void finishUpdate( ViewGroup container ) {
//            Log.e(LOG_TAG, "PagerAdapter finishUpdate function");
            super.finishUpdate(container);

            ViewPager viewPager = (ViewPager) container;
            int position = viewPager.getCurrentItem();

            ReaderPageFragment fragment = (ReaderPageFragment) getItem( position );
            LinearLayout view = (LinearLayout) fragment.getView();

            if( view == null ) {
                Log.e(LOG_TAG, "View is null.");
                return;
            }

            int screenHeight = view.getHeight() - mViewTopPadding;
//            Log.e(LOG_TAG, "Screen Height : " + screenHeight);
            if( screenHeight <= 0 ) {
//                Log.e(LOG_TAG, "Screen height is 0 at position " + position + ".");
                return;
            }



            int viewHeight = 0;
            int viewCount = 0;
            for( int i = 0; i < view.getChildCount(); i++ ) {
                View childView = view.getChildAt( i );
                if( viewHeight + childView.getHeight() < screenHeight ) {
                    viewCount++;
                    viewHeight = viewHeight + childView.getHeight();
//                    Log.e(LOG_TAG, "viewCount / viewHeight : " + viewCount + "/" + viewHeight);
                } else {
                    break;
                }
            }
//            Log.e(LOG_TAG, "viewCount / getChildCount() : " + viewCount + "/" + view.getChildCount());

            if( viewCount < view.getChildCount() ) {
                int[] pageFragmentInfo = pageFragmentInfoList.get( position );
                int[] newPageFragmentInfo = new int[] {
                    pageFragmentInfo[0],
                    pageFragmentInfo[1],
                    pageFragmentInfo[2] + viewCount,
                    pageFragmentInfo[3] - viewCount
                };
                pageFragmentInfo[3] = viewCount;
                Log.e("Exiting", pageFragmentInfo[0] + "..." + pageFragmentInfo[1] + "..." + pageFragmentInfo[2] + "..." + pageFragmentInfo[3] + "...");
                Log.e("New", newPageFragmentInfo[0] + "..." + newPageFragmentInfo[1] + "..." + newPageFragmentInfo[2] + "..." + newPageFragmentInfo[3] + "...");
                view.removeViews( viewCount, view.getChildCount() - viewCount );
                pageFragmentInfoList.add( position + 1, newPageFragmentInfo );
                notifyDataSetChanged();
            }

        }

        public void notifyDataSetChanged() {
            Log.e(LOG_TAG, "PagerAdapter notifyDataSetChanged function");
            super.notifyDataSetChanged();
            for( Map.Entry<Integer, ReaderPageFragment> positionFragmentEntry : positionFragmentMap.entrySet() ) {
                int[] pageFragmentInfo = pageFragmentInfoList.get( positionFragmentEntry.getKey() );
                ReaderPageFragment pageFragment = positionFragmentEntry.getValue();
                pageFragment.setPagelets( pratilipiData.getChapter( pageFragmentInfo[0] )
                        .getPage( pageFragmentInfo[1] )
                        .getPagelets( pageFragmentInfo[2], pageFragmentInfo[3] ));
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
        Log.e(LOG_TAG, "Parent Class Name : " + mParentActivityClassName);
        if( mParentActivityClassName.equals(DetailActivity.class.getSimpleName()))
            parentIntent = new Intent( this, DetailActivity.class);
        else if(mParentActivityClassName.equals(MainActivity.class.getSimpleName()))
            parentIntent = new Intent( this, MainActivity.class);

        parentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        return parentIntent;
    }

    private void hideActionBar(ActionBar actionBar, LinearLayout seekBarLayout){
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
        actionBar.hide();
        seekBarLayout.setVisibility(View.INVISIBLE);

    }

    private void showActionBar(ActionBar actionBar, LinearLayout seekBarLayout){
        if (Build.VERSION.SDK_INT >= 16) {
            View decorView = getWindow().getDecorView();
            // Show the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
            decorView.setSystemUiVisibility(uiOptions);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        actionBar.show();
        seekBarLayout.setVisibility(View.VISIBLE);
    }

}
