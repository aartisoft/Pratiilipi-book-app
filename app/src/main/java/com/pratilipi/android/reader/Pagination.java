package com.pratilipi.android.reader;

import android.content.Context;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.pratilipi.android.pratilipi_and.util.AppUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rahul Ranjan on 3/14/2016.
 * As name suggests, this class is used to split content into different pages.
 * Function doPagination(String content) returns integer array containing starting position of each
 * screen or page.
 */
public class Pagination {

    private final static String LOG_TAG = Pagination.class.getSimpleName();

    private Context mContext;

    private final boolean mIncludePad;
    private final int mWidth;
    private final int mHeight;
    private final float mSpacingMult;
    private final float mSpacingAdd;
    private final TextPaint mTextPaint;

    public Pagination(Context context, int width, int height, TextPaint textPaint, float spacingAdd, float spacingMult,  boolean includePad){
        this.mContext = context;
        this.mWidth = width;
        this.mHeight = height;
        this.mTextPaint = textPaint;
        this.mSpacingAdd = spacingAdd;
        this.mSpacingMult = spacingMult;
        this.mIncludePad = includePad;
    }

    public List<CharSequence> doPagination(String content){
        List<CharSequence> pageContents = new ArrayList<CharSequence>();
        mTextPaint.setTextSize(AppUtil.getReaderFontSize(mContext));
        final StaticLayout layout =
                new StaticLayout(
                        content,
                        mTextPaint,
                        mWidth,
                        Layout.Alignment.ALIGN_NORMAL,
                        mSpacingMult,
                        mSpacingAdd,
                        mIncludePad
                );

        int lines = layout.getLineCount();
        CharSequence text = layout.getText();
        int startOffset = 0, endIndex = 0;
        //40% space is reserved for TextView of containing chapter title.
        int height = (int) Math.floor((double)mHeight * 0.6);

        for(int i=0;i<lines; ++i){
            if(height < layout.getLineBottom(i)){
                //When layout height exceeds
                int start = layout.getLineStart(i-1);
                int end = layout.getLineStart(i);
                String test = content.substring(start, end);
                if(test.contains("\n")){
                    /**
                     * If last line of a paragraph is last line of screen, '\n' character is removed
                     * to avoid empty line at bottom of the screen.
                     */
                    String temp = (String) text.subSequence(startOffset, layout.getLineStart(i));
                    endIndex = startOffset + temp.lastIndexOf('\n');
                    pageContents.add(text.subSequence(startOffset, endIndex));
                    endIndex = layout.getLineStart(i);
                }
                else {
                    //Taking sub sequence till last space character present in last visible line on screen.
                    String temp = (String) text.subSequence(startOffset, layout.getLineStart(i));
                    if(temp.lastIndexOf(" ") != -1)
                        endIndex = startOffset + temp.lastIndexOf(" ");
                    else
                        endIndex = layout.getLineStart(i);
                    pageContents.add(text.subSequence(startOffset, endIndex));
                }
                startOffset = endIndex;
                height = layout.getLineTop(i) + mHeight;
            }

            if (i == lines - 1) {
                // Put the rest of the text into the last page
                pageContents.add(text.subSequence(startOffset, layout.getLineEnd(i)));
            }
        }
        return pageContents;
    }
}
