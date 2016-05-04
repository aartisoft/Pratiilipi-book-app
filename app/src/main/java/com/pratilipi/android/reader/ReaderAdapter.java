package com.pratilipi.android.reader;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
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
    private List<String[]> mPageContentList;    //{screenNumber, chapterNumber, text}
    private FragmentManager mFragmentManager;
    private Pratilipi mPratilipi;

    private int mCurrentChapter;
    private int mChapterCount;

    private boolean mFontSizeChangedFlag;

    public ReaderAdapter(FragmentManager fm, Context context){
        super(fm);
        this.mContext = context;
        this.mFragmentManager = fm;
        this.mContentMapList = new TreeMap<Integer, String>();
        this.mPageContentList = new ArrayList<>();
        this.mFontSizeChangedFlag = false;
    }

    @Override
    public Fragment getItem(int position) {
        String language =
                mPratilipi.getLanguageName() != null ? mPratilipi.getLanguageName() : mPratilipi.getLanguageId();
        String[] pageContent = mPageContentList.get(position);

        return ReaderFragment.newInstance(language, pageContent[2]);
    }

    @Override
    public int getCount() {
        return mPageContentList.size();
    }

    @Override
    public int getItemPosition(Object object) {
        /**
         * Returns POSITION_NONE when font size is changed
         */
        if(mFontSizeChangedFlag)
            return POSITION_NONE;
        else
            return super.getItemPosition(object);
    }

    public void setContent(Pratilipi pratilipi, int currentChapter){
        this.mPratilipi = pratilipi;
        this.mCurrentChapter = currentChapter;
        getContentFromDb(mCurrentChapter, RANDOM_CHAPTER);
    }

    public void setFontSizeChangeFlag(boolean isFontSizeChanged){
        this.mFontSizeChangedFlag = isFontSizeChanged;
        mPageContentList.clear();
        getContentFromDb(mCurrentChapter, RANDOM_CHAPTER);
        notifyDataSetChanged();
        this.mFontSizeChangedFlag = false;
    }

    public void addChapter(Content content, int chapterType){
        switch (chapterType){
            case RANDOM_CHAPTER:
                mContentMapList.clear();
        }

        mContentMapList.put(Integer.valueOf(content.getChapterNo()), content.getTextContent());
        //Perform pagination for added content
        List<Content> tempList = new ArrayList<>();
        tempList.add(content);
        performPagination(tempList, chapterType);
        notifyDataSetChanged();
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
                for(Content content : contentList) {
                    /**
                     * Add page contents returned by Pagination class at start of 
                     */
                    List<CharSequence> pageContentList = pagination.doPagination(content.getTextContent());
                    int i = 0;
                    for (CharSequence sequence : pageContentList) {
                        String[] temp = {content.getChapterNo(), i + "", sequence.toString()};
                        //Add page details at end of the list.
                        mPageContentList.add(i, temp);
                        i += 1;
                    }
                    notifyDataSetChanged();
                }
            }
            case RANDOM_CHAPTER : {
                for(Content content : contentList) {
                    /**
                     * Clear existing pages and add fresh fragments.
                     */
                    mPageContentList.clear();
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
            }
            case NEXT_CHAPTER : {
                for(Content content : contentList) {
                    //Add fresh fragments at end of the list.
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
            }
        }

    }

    public int setCurrentChapter(int currentItemIndex){
        String[] currentItem = mPageContentList.get(currentItemIndex);
        this.mCurrentChapter = Integer.parseInt(currentItem[0]);
        return mCurrentChapter;
    }

    public int getCurrentChapter(int currentItemIndex){
        String[] currentItem = mPageContentList.get(currentItemIndex);
        return Integer.parseInt(currentItem[0]);
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
        if(!cursor.moveToFirst())
            getContentFromServer(contentUtil, chapterNo, chapterType);
        else {
            //PAGINATION AND FRAGMENT VIEW CREATION.
            List<Content> contentList = contentUtil.createContentList(cursor);
            cursor.close();
            performPagination(contentList, chapterType);
        }
    }

    private void getPreviousChapter(){
        if(mCurrentChapter > 1)
            getContentFromDb(mCurrentChapter - 1, PREVIOUS_CHAPTER);
    }

    private void getNextChapter(){
        if(mCurrentChapter < mChapterCount)
            getContentFromDb(mCurrentChapter + 1, NEXT_CHAPTER);
    }

    private void getContentFromServer(ContentUtil contentUtil, final int chapterNo, final int chapterType){
        if(!AppUtil.isOnline(mContext)) {
            Toast.makeText(mContext, "Device is not connected to internet", Toast.LENGTH_SHORT).show();
            return;
        }

        contentUtil.fetchChapterFromServer(mContext, mPratilipi, chapterNo, new GetCallback() {
            @Override
            public void done(boolean isSuccessful, String data) {
                if (isSuccessful) {
                    //TODO : FETCH DATA FROM LOCAL DB, INITIATE PAGINATION AND FRAGMENT CREATION.
                    getContentFromDb(chapterNo, chapterType);
                } else{
                    Toast.makeText(mContext, "Error while fetching content from server. Try again later", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
