package com.pratilipi.android.reader;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.pratilipi.android.pratilipi_and.R;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class ReaderActivity extends FragmentActivity implements SeekBar.OnSeekBarChangeListener {

    private static final String LOG_TAG = ReaderActivity.class.getSimpleName();

    private PratilipiData pratilipiData;
    private List<int[]> pageFragmentInfoList; // List<[chapterNo, pageNo, pageletStart, pageletCount]>

    private ViewPager viewPager;
    private SeekBar seekBar;


    @Override
    protected void onCreate( Bundle savedInstanceState ) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.reader_activity);



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
        viewPager = (ViewPager) findViewById( R.id.viewPager );
        viewPager.setAdapter( new PagerAdapter( getSupportFragmentManager() ) );
        viewPager.setCurrentItem( 0 ); // This will trigger ViewPager.startUpdate and update the SeekBar

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



    // Option Menu

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        getMenuInflater().inflate( R.menu.menu_main, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        int id = item.getItemId();

//        if( id == R.id.action_settings ) {
            // Do Nothing !
//        }

        return super.onOptionsItemSelected(item);
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
            ReaderPageFragment pageFragment = positionFragmentMap.get( position );
            if( pageFragment == null ) {
                int[] pageFragmentInfo = pageFragmentInfoList.get( position );
                pageFragment = new ReaderPageFragment();
                pageFragment.setPagelets(pratilipiData.getChapter(pageFragmentInfo[0])
                        .getPage(pageFragmentInfo[1])
                        .getPagelets(pageFragmentInfo[2], pageFragmentInfo[3]));
                positionFragmentMap.put( position, pageFragment );
            }
            return pageFragment;
        }

        @Override
        public void startUpdate( ViewGroup container ) {
            int chapterNo = pageFragmentInfoList.get( viewPager.getCurrentItem() )[0];
            if( seekBar.getProgress() != chapterNo )
                seekBar.setProgress( chapterNo );
        }

        public synchronized  void finishUpdate( ViewGroup container ) {
            super.finishUpdate(container);

            ViewPager viewPager = (ViewPager) container;
            int position = viewPager.getCurrentItem();

            ReaderPageFragment fragment = (ReaderPageFragment) getItem( position );
            LinearLayout view = (LinearLayout) fragment.getView();

            if( view == null ) {
                Log.w(LOG_TAG, "View is null.");
                return;
            }

            int screenHeight = view.getHeight();
            if( screenHeight == 0 ) {
                Log.w(LOG_TAG, "Screen height is 0 at position " + position + ".");
                return;
            }

            int viewHeight = 0;
            int viewCount = 0;
            for( int i = 0; i < view.getChildCount(); i++ ) {
                View childView = view.getChildAt( i );
                if( viewHeight + childView.getHeight() < screenHeight ) {
                    viewCount++;
                    viewHeight = viewHeight + childView.getHeight();
                } else {
                    break;
                }
            }

            if( viewCount < view.getChildCount() ) {
                int[] pageFragmentInfo = pageFragmentInfoList.get( position );
                int[] newPageFragmentInfo = new int[] {
                    pageFragmentInfo[0],
                    pageFragmentInfo[1],
                    pageFragmentInfo[2] + viewCount,
                    pageFragmentInfo[3] - viewCount
                };
                pageFragmentInfo[3] = viewCount;
                Log.w("Exiting", pageFragmentInfo[0] + "..." + pageFragmentInfo[1] + "..." + pageFragmentInfo[2] + "..." + pageFragmentInfo[3] + "...");
                Log.w("New", newPageFragmentInfo[0] + "..." + newPageFragmentInfo[1] + "..." + newPageFragmentInfo[2] + "..." + newPageFragmentInfo[3] + "...");
                view.removeViews( viewCount, view.getChildCount() - viewCount );
                pageFragmentInfoList.add( position + 1, newPageFragmentInfo );
                notifyDataSetChanged();
            }

        }

        public void notifyDataSetChanged() {
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


}
