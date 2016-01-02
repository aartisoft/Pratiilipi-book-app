package com.pratilipi.android.reader;

import android.app.Activity;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pratilipi.android.pratilipi_and.R;
import com.pratilipi.android.pratilipi_and.util.AppUtil;

public class ReaderPageFragment extends Fragment {

    public static String READER_FRAGMENT_TAG = "reader_fragment_tag";
    public final String LOG_TAG = ReaderPageFragment.class.getSimpleName();
    public static final String START_INDEX = "startIndex";
    public static final String END_INDEX = "endIndex";
    public static final String CONTENT_STRING = "contentString";
    public static final String LANGUAGE = "language";

    private Activity mActivity;
    private int mStartIndex = -1;
    private int mEndIndex = -1;
    private String mContentString;
    private TextView mLoadingTextView;
    private TextView mTextView;
    private String mLanguage;
    private PratilipiData.Pagelet[] pagelets;
    private LinearLayout view;

    public static ReaderPageFragment newInstance(String language, String contentString, int startIndex, int endIndex){
        ReaderPageFragment fragment = new ReaderPageFragment();
        Bundle args = new Bundle();
        args.putString(LANGUAGE, language);
        args.putString(CONTENT_STRING, contentString);
        args.putString(START_INDEX, String.valueOf(startIndex));
        args.putString(END_INDEX, String.valueOf(endIndex));

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
//        Log.e(LOG_TAG, "setArguments function");
        mLanguage = args.getString(LANGUAGE);
        mStartIndex = Integer.parseInt(args.getString(START_INDEX));
//        getInt(END_INDEX) returns 0 instead of -1. Hence getString is used.
        mEndIndex = Integer.parseInt(args.getString(END_INDEX));
        mContentString = args.getString(CONTENT_STRING);
    }

    public void setPage(){
        if(view == null)
            return;
//        Log.e(LOG_TAG, "view is not null");

        mTextView.setGravity(View.TEXT_ALIGNMENT_CENTER);
        setFontSize(AppUtil.getReaderFontSize(mActivity));
        ViewTreeObserver vto = mTextView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
//                Log.e(LOG_TAG, "onGlobalLayout() function called");

                //DO NOTHING WHEN LAYOUT IS NULL
                if (mTextView.getLayout() == null) {
//                    Log.e(LOG_TAG, "mTextView.getLayout() is null");
                    return;
                }

//                Log.e(LOG_TAG, "mTextView.getLayout() is not null");
                setFont(mLanguage);
                if (mEndIndex == -1) {
//                    Log.e(LOG_TAG, "Calculating mEndIndex");
                    mEndIndex = findEndIndex();
                    setPage();
                }
            }
        });

        setText();
//        view.addView(mTextView);

    }

    public void setPage(String contentString, int startIndex, int endIndex){
//        Log.e(LOG_TAG, "setPage(arg1, arg2, arg3) function called");
        mContentString = contentString;
        mStartIndex = startIndex;
        mEndIndex = endIndex;
        setPage();
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {

//        Log.e(LOG_TAG, "onCreateView function");
        mActivity = getActivity();

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .add(this, READER_FRAGMENT_TAG);

        if( view == null )
            view = (LinearLayout) inflater.inflate( R.layout.reader_fragment, container, false );

        mLoadingTextView = (TextView) view.findViewById(R.id.reader_fragment_loading_textview);
        mTextView = (TextView) view.findViewById(R.id.reader_fragment_content_textview);

        if(mContentString != null && !mContentString.isEmpty() ) {
            setPage();
        } else
            Log.e(LOG_TAG, "setPage Function not called");

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void setFontSize(float fontSize){
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
    }

    public float getFontSize(){
        return mTextView.getTextSize();
    }

    public int getLineBounds(int lineNumber){
        Rect bounds = new Rect();
        mTextView.getLineBounds(lineNumber, bounds);
        return bounds.bottom;
    }

    public int getLineCount(){
        return mTextView.getLineCount();
    }

    public int getEndIndex(){
        return mEndIndex;
    }

    private int getLineStart(int lineNumber){
//        Log.e(LOG_TAG, "Line Number : " + lineNumber);
//        Log.e(LOG_TAG, "Line Start : " + mTextView.getLayout().getLineStart(lineNumber));
        return mTextView.getLayout().getLineStart(lineNumber);
    }

    private int getLineEnd(int lineNumber){
        if(mTextView.getLayout() == null)
            return -1;
        else
            return mTextView.getLayout().getLineEnd(lineNumber);
    }

    public String getTextSubsequence(int start, int end){
        int startIndex = getLineStart(start);
        int endIndex = getLineEnd(end);
        String text = mTextView.getText().subSequence(startIndex, endIndex).toString();
        return text;
    }

    private void setText(){
        String subString;
//        Log.e(LOG_TAG, "Start Index / End Length : " + mStartIndex + " / " + mEndIndex);
        if(mStartIndex != -1) {
            if(mEndIndex == -1)
                subString = mContentString.substring(mStartIndex);
            else {
//                Log.e(LOG_TAG, "End Index / Total Length : " + mEndIndex + " / " + mContentString.length());
                subString = mContentString.substring(mStartIndex, mEndIndex);
            }
        } else
            subString = mContentString;
        mTextView.setText(Html.fromHtml(subString));
        if(mLoadingTextView.getVisibility() != View.GONE)
            mLoadingTextView.setVisibility(View.GONE);
        mTextView.setVisibility(View.VISIBLE);
    }

    private int findEndIndex(){
        int endLine = 0;
        int totalLines = getLineCount();
        int screenBaseLine = view.getBottom() - view.getPaddingBottom() - view.getPaddingTop();
        for(int i = 0; i<totalLines; ++i){
            endLine = i;
            int lineBase = getLineBounds(endLine);
            if(lineBase >= screenBaseLine ) {
                endLine -= 1;
                break;
            }
        }
        if(getLineEnd(endLine) != -1) {
            int lineEnd = mTextView.getLayout().getLineEnd(endLine);
            if(endLine != totalLines-1) {
                //when endLine is not last line, find last index of space to prevent word cutting.
                String subString = mContentString.substring(mStartIndex, mStartIndex + lineEnd);
                int lastSpaceIndex = subString.lastIndexOf(" ");
                //neglect when no space is present.
                if(lastSpaceIndex != -1)
                    lineEnd = lastSpaceIndex;
            }
            mEndIndex = mStartIndex + lineEnd;
        }
        else
            mEndIndex = -1;
        return mEndIndex;
    }

    private void setFont(String lan){
        Typeface typeFace = null;
        if (lan.equalsIgnoreCase("hindi"))
            typeFace = Typeface.createFromAsset(mActivity.getAssets(), "fonts/devanagari.ttf");
        else if (lan.equalsIgnoreCase("tamil"))
            typeFace = Typeface.createFromAsset(mActivity.getAssets(), "fonts/tamil.ttf");
        else if (lan.equalsIgnoreCase("gujarati"))
            typeFace = Typeface.createFromAsset(mActivity.getAssets(), "fonts/gujarati.ttf");

        mTextView.setTypeface(typeFace);
    }
}
