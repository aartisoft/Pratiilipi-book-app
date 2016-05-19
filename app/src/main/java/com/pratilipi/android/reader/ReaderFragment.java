package com.pratilipi.android.reader;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pratilipi.android.pratilipi_and.R;
import com.pratilipi.android.pratilipi_and.util.AppUtil;

/**
 * Created by Rahul Ranjan on 3/14/2016.
 */
public class ReaderFragment extends Fragment {

    private static final String LOG_TAG = ReaderFragment.class.getSimpleName();
    public static final String CONTENT_STRING = "contentString";
    public static final String LANGUAGE = "language";
    public static final String CHAPTER_TITLE = "title";
    public static final String IS_FIRST_FRAGMENT = "isFirstFragment";

    private Activity mActivity;
    private LinearLayout mLayout;
    private TextView mTitleTextView;
    private LinearLayout mTextViewLayout;
    private ReaderTextView mTextView;
    private String mLanguage;
    private CharSequence mContentString;

    private String mChapterTitle;
    private boolean mIsFirstFragment;


    public static ReaderFragment newInstance(String language, String contentString, String chapterTitle, boolean isFirstFragment){
        ReaderFragment fragment = new ReaderFragment();
        Bundle args = new Bundle();
        args.putString(LANGUAGE, language);
        args.putString(CONTENT_STRING, contentString);
        args.putString(CHAPTER_TITLE, chapterTitle);
        args.putString(IS_FIRST_FRAGMENT, String.valueOf(isFirstFragment));

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        mLanguage = args.getString(LANGUAGE);
        mContentString = args.getString(CONTENT_STRING);
        mChapterTitle = args.getString(CHAPTER_TITLE);
        mIsFirstFragment = Boolean.parseBoolean(args.getString(IS_FIRST_FRAGMENT));
    }


    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {

//        Log.e(LOG_TAG, "onCreateView function");
        mActivity = getActivity();

        if( mLayout == null )
            mLayout = (LinearLayout) inflater.inflate( R.layout.reader_fragment, container, false );

        mTextView = (ReaderTextView) mLayout.findViewById(R.id.reader_fragment_content_textview);
        if(mTextView != null) {
            if(mIsFirstFragment){
                mTitleTextView = (TextView) mLayout.findViewById(R.id.reader_fragment_title_textview);
                mTitleTextView.setText(mChapterTitle);
                mTitleTextView.setVisibility(View.VISIBLE);

                mTextViewLayout = (LinearLayout) mLayout.findViewById(R.id.reader_fragment_content_layout);
                mTextViewLayout.setGravity(Gravity.BOTTOM);
            }
            setFont(mLanguage);
            setFontSize();
            mTextView.setText(mContentString);
            mLayout.findViewById(R.id.reader_fragment_loading_textview).setVisibility(View.GONE);
            mTextView.setVisibility(View.VISIBLE);
        }

        return mLayout;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public float getFontSize(){
        return mTextView.getTextSize();
    }

    private void setFontSize(){
        float fontSize = AppUtil.getReaderFontSize(mActivity);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
    }

    private void setFont(String lan){
        if(lan == null){
            Log.e(LOG_TAG, "Language is not set for content ");
            return;
        }
        Typeface typeFace = null;
        //Old api sends languageId and new API sends language. Hence both are used.
        if (lan.equalsIgnoreCase("hindi") || lan.equals(AppUtil.HINDI_LANGUAGE_ID))
            typeFace = Typeface.createFromAsset(mActivity.getAssets(), "fonts/devanagari.ttf");
        else if (lan.equalsIgnoreCase("tamil") || lan.equals(AppUtil.TAMIL_LANGUAGE_ID))
            typeFace = Typeface.createFromAsset(mActivity.getAssets(), "fonts/tamil.ttf");
        else if (lan.equalsIgnoreCase("gujarati") || lan.equals(AppUtil.GUJARATI_LANGUAGE_ID))
            typeFace = Typeface.createFromAsset(mActivity.getAssets(), "fonts/gujarati.ttf");

        mTextView.setTypeface(typeFace);
    }

}
