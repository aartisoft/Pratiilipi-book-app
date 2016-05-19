package com.pratilipi.android.reader;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.pratilipi.android.pratilipi_and.GetCallback;
import com.pratilipi.android.pratilipi_and.datafiles.Content;
import com.pratilipi.android.pratilipi_and.datafiles.Pratilipi;
import com.pratilipi.android.pratilipi_and.util.AppUtil;
import com.pratilipi.android.pratilipi_and.util.ContentUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Rahul Ranjan on 3/10/2016.
 */
public class ReaderAdapter extends FragmentStatePagerAdapter {

    public static final String LOG_TAG = ReaderAdapter.class.getSimpleName();
    public static final int PREVIOUS_CHAPTER = 0;
    public static final int RANDOM_CHAPTER = 1;
    public static final int NEXT_CHAPTER = 2;

    private Context mContext;
    private Map<Integer, String> mContentMapList;
    private List<Content> mContentList;
    private List<String[]> mPageContentList;    //{chapterNumber, fragmentNumber, text, boolean}
    private FragmentManager mFragmentManager;
    private Pratilipi mPratilipi;
    private List<String> mTitles;

    private int mCurrentChapter;
    private int mChapterCount;

    private boolean mFragmentContentChanged;
    private boolean mFetchingNextChapter;
    private boolean mFetchingPreviousChapter;
    private int mPreviousChapterFragmentCount;

    public ReaderAdapter(FragmentManager fm, Context context, List<String> titles){
        super(fm);
        Log.e(LOG_TAG, "ReaderAdapter initialized");
        this.mContext = context;
        this.mFragmentManager = fm;
        this.mTitles = titles;
        this.mContentList = new ArrayList<>();
        this.mContentMapList = new TreeMap<Integer, String>();
        this.mPageContentList = new ArrayList<>();
        this.mFragmentContentChanged = false;
    }

    @Override
    public Fragment getItem(int position) {
        String language =
                mPratilipi.getLanguageName() != null ? mPratilipi.getLanguageName() : mPratilipi.getLanguageId();
        String[] pageContent = mPageContentList.get(position);
        int chapterNumber = Integer.parseInt(pageContent[0]);
        int fragmentNo = Integer.parseInt(pageContent[1]);
        if(fragmentNo == 0)
            return ReaderFragment.newInstance(language, pageContent[2], mTitles.get(chapterNumber-1), true);
        else
            return ReaderFragment.newInstance(language, pageContent[2], mTitles.get(chapterNumber-1), false);
    }

    @Override
    public int getCount() {
//        Log.e(LOG_TAG, "Count of Items in Adapter : " + mPageContentList.size());
        return mPageContentList.size();
    }

    @Override
    public int getItemPosition(Object object) {
        /**
         * Returns POSITION_NONE when font size is changed
         */
        if(mFragmentContentChanged)
            return POSITION_NONE;
        else
            return super.getItemPosition(object);
    }

    public void setContent(Pratilipi pratilipi, int chapterCount, int currentChapter){
        this.mPratilipi = pratilipi;
        this.mChapterCount = chapterCount;
        this.mCurrentChapter = currentChapter;
        this.mFragmentContentChanged = true;
        getContentFromDb(mCurrentChapter, RANDOM_CHAPTER);
    }



    public void setFontSizeChangeFlag(boolean isFontSizeChanged){
        this.mFragmentContentChanged = isFontSizeChanged;
        mPageContentList.clear();
//        getContentFromDb(mCurrentChapter, RANDOM_CHAPTER);
        performPagination(mContentList, RANDOM_CHAPTER);
        notifyDataSetChanged();
        this.mFragmentContentChanged = false;
    }

    public void performPagination(List<Content> contentList, int chapterType){
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        int width, height;
        if (Build.VERSION.SDK_INT >= 13) {
            display.getSize(size);
            width = size.x;
            height = size.y;
        } else{
            //Applicable for API SDK level less than 13.
            width = display.getWidth();
            height = display.getHeight();
        }
        ReaderTextView readerTextView = new ReaderTextView(mContext);
        width = width - 90;
        height = height - 120;

        Pagination pagination = new Pagination(mContext, width, height, readerTextView.getPaint(), 0f, 1f, false);

        switch (chapterType){
            case PREVIOUS_CHAPTER : {
                Content tempContent = new Content();
                for(Content content : contentList) {
                    /**
                     * Add page contents returned by Pagination class at start of 
                     */
                    tempContent = content;
                    Log.i(LOG_TAG, "Performing Pagination. Previous Chapter");
                    List<CharSequence> pageContentList = pagination.doPagination(content.getTextContent());
                    mPreviousChapterFragmentCount = pageContentList.size();
                    int i = 0;
                    for (CharSequence sequence : pageContentList) {
                        String[] temp = {content.getChapterNo(), i + "", sequence.toString()};
                        //Add page details at end of the list.
                        mPageContentList.add(i, temp);
                        i += 1;
                    }
                    notifyDataSetChanged();
                }
                Log.i(LOG_TAG, "performPagination. Number of fragments for previous Chapter : " + mPreviousChapterFragmentCount);
                mContentList.add(0, tempContent);
                break;
            }
            case RANDOM_CHAPTER : {
                mPageContentList.clear();
                List<Content> tempList = new ArrayList<>();
                for(Content content : contentList) {
                    /**
                     * Clear existing pages and add fresh fragments.
                     */
                    tempList.add(content);
                    Log.i(LOG_TAG, "Perform Pagination. Random chapter");
                    List<CharSequence> pageContentList = pagination.doPagination(content.getTextContent());
                    int i = 0;
                    for (CharSequence sequence : pageContentList) {
                        String[] temp = {content.getChapterNo(), i + "", sequence.toString()};
                        //Add page details at end of the list.
                        mPageContentList.add(mPageContentList.size(), temp);
                        i += 1;
                    }
                    notifyDataSetChanged();
                }
                mContentList = tempList;
                break;
            }
            case NEXT_CHAPTER : {
                Content tempContent = new Content();
                for(Content content : contentList) {
                    //Add fresh fragments at end of the list.
                    tempContent = content;
                    Log.i(LOG_TAG, "Perform Pagination. Next Chapter");
                    List<CharSequence> pageContentList = pagination.doPagination(content.getTextContent());
                    int i = 0;
                    for (CharSequence sequence : pageContentList) {
                        String[] temp = {content.getChapterNo(), i + "", sequence.toString()};
                        //Add page details at end of the list.
                        mPageContentList.add(mPageContentList.size(), temp);
                        i += 1;
                    }
                    notifyDataSetChanged();
                }
                mContentList.add(mContentList.size(), tempContent);
                break;
            }
        }

    }

    public int setCurrentChapter(int currentItemIndex){
        String[] currentItem = mPageContentList.get(currentItemIndex);
        this.mCurrentChapter = Integer.parseInt(currentItem[0]);
        return mCurrentChapter;
    }

    public int getCurrentChapter(){
        return mCurrentChapter;
    }

    public void setChapterCount(int chapterCount){
        this.mChapterCount = chapterCount;
    }

    public int getCurrentFragment(int currentItemIndex){
        String[] currentItem = mPageContentList.get(currentItemIndex);
        return Integer.parseInt(currentItem[1]);
    }

    public void getContentFromDb(int chapterNo, int chapterType){
        ContentUtil contentUtil = new ContentUtil();
        Cursor cursor = contentUtil.getContentfromDb(mContext, mPratilipi, chapterNo, null);
        if(!cursor.moveToFirst()) {
            Log.i(LOG_TAG, "Content not found in local DB. Making server call");
            getContentFromServer(contentUtil, chapterNo, chapterType);
        }
        else {
            //PAGINATION AND FRAGMENT VIEW CREATION.
            Log.i(LOG_TAG, "Content is found in local DB");
            List<Content> contentList = contentUtil.createContentList(cursor);
            cursor.close();
            performPagination(contentList, chapterType);
            mFetchingNextChapter = false;
            mFetchingPreviousChapter = false;
        }
    }

    public void getPreviousChapter(){
        mPreviousChapterFragmentCount = 0;
        if(mCurrentChapter > 1 && !mFetchingPreviousChapter) {
            String[] temp = mPageContentList.get(0);
            int smallestChapter = Integer.valueOf(temp[0]);
            if(smallestChapter == mCurrentChapter) {
                Log.e(LOG_TAG, "Fetching previous chapter. Chapter Number : " + (mCurrentChapter-1));
                mFetchingPreviousChapter = true;
                ContentUtil contentUtil = new ContentUtil();
                Cursor cursor = contentUtil.getContentfromDb(mContext, mPratilipi, mCurrentChapter-1, null);
                Log.e(LOG_TAG, "getPreviousChapter. Data fetched from db");
                if(cursor == null){
                    Log.e(LOG_TAG, "Cursor returned is null");
                    getContentFromServer(contentUtil, mCurrentChapter - 1, PREVIOUS_CHAPTER);
                } else if(!cursor.moveToFirst()) {
                    Log.e(LOG_TAG, "Cursor is empty");
                    getContentFromServer(contentUtil, mCurrentChapter - 1, PREVIOUS_CHAPTER);
                }
            }
        }
    }

    public int setPreviousChapter(){
        if(mCurrentChapter > 1)
            getContentFromDb(mCurrentChapter-1, PREVIOUS_CHAPTER);
        return mPreviousChapterFragmentCount;
    }

    public void getNextChapter(){
        if(mCurrentChapter < mChapterCount && !mFetchingNextChapter) {
            String[] temp = mPageContentList.get(mPageContentList.size()-1);
            int largestChapter = Integer.valueOf(temp[0]);
            if(mCurrentChapter == largestChapter) {
                Log.i(LOG_TAG, "Fetching next chapter");
                mFetchingNextChapter = true;
                getContentFromDb(mCurrentChapter + 1, NEXT_CHAPTER);
            }
        }
    }

    public void getContentFromServer(ContentUtil contentUtil, final int chapterNo, final int chapterType){
        if(!AppUtil.isOnline(mContext)) {
            Toast.makeText(mContext, "Device is not connected to internet", Toast.LENGTH_SHORT).show();
            return;
        }

        contentUtil.fetchChapterFromServer(mContext, mPratilipi, chapterNo, new GetCallback() {
            @Override
            public void done(boolean isSuccessful, String data) {
                if (isSuccessful) {
                    Log.i(LOG_TAG, "Async task callback received");
                    //FETCH DATA FROM LOCAL DB, INITIATE PAGINATION AND FRAGMENT CREATION.
                    if(chapterType != PREVIOUS_CHAPTER)
                        getContentFromDb(chapterNo, chapterType);
                } else{
                    Toast.makeText(mContext, "Error while fetching content from server. Try again later", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
