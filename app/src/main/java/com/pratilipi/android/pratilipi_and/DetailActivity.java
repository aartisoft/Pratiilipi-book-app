package com.pratilipi.android.pratilipi_and;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.pratilipi.android.pratilipi_and.datafiles.Pratilipi;

public class DetailActivity extends AppCompatActivity {

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();
    public static final String PRATILIPI = "pratilipi";
    public static final String PARENT_ACTIVITY_CLASS_NAME = "parentActivityClassName";

    private Pratilipi mPratilipi;
    private String mPratilipiId;
    private String mParentActivityClassName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        getSupportActionBar().setElevation(1f);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_logo);

        mPratilipi = (Pratilipi) getIntent().getSerializableExtra(PRATILIPI);
        mParentActivityClassName = getIntent().getStringExtra(PARENT_ACTIVITY_CLASS_NAME);
        Log.e(LOG_TAG, "Parent Activity Class Name : " + mParentActivityClassName);


        String lan = getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE).getString("selectedLanguage", "");
        Typeface typeFace = null;
        if(lan.equalsIgnoreCase("hi"))
            typeFace= Typeface.createFromAsset(getAssets(), "fonts/devanagari.ttf");
        else if(lan.equalsIgnoreCase("ta"))
            typeFace= Typeface.createFromAsset(getAssets(), "fonts/tamil.ttf");
        else if(lan.equalsIgnoreCase("gu"))
            typeFace= Typeface.createFromAsset(getAssets(), "fonts/gujarati.ttf");

        mPratilipiId = mPratilipi.getPratilipiId();

        ImageLoader imageLoader = AppController.getInstance().getImageLoader();
        NetworkImageView imageView = (NetworkImageView) findViewById(R.id.detail_cover_image);
        imageView.setImageUrl("http:" + mPratilipi.getCoverImageUrl(), imageLoader);

        TextView title = (TextView) findViewById(R.id.detail_title_textview);
        title.setTypeface(typeFace);
        title.setText(Html.fromHtml(mPratilipi.getTitle()));

        TextView authorName = (TextView) findViewById(R.id.detail_author_name_textview);
        authorName.setText(mPratilipi.getAuthorName());

        RatingBar ratingBar = (RatingBar) findViewById(R.id.detail_ratingBar);
        ratingBar.setRating(mPratilipi.getAverageRating());

        if( mPratilipi.getSummary() != null ) {
            TextView summaryTextView = (TextView) findViewById(R.id.detail_summary_textview);
            summaryTextView.setTypeface(typeFace);
            summaryTextView.setText(Html.fromHtml(mPratilipi.getSummary()));
        }
    }

    @Nullable
    @Override
    public Intent getSupportParentActivityIntent() {
        return getParentActivityIntentImpl();
    }

    @Nullable
    @Override
    public Intent getParentActivityIntent() {
        return getParentActivityIntentImpl();
    }

    private Intent getParentActivityIntentImpl(){
        Intent parentIntent = null;
        if( mParentActivityClassName.equals(CardListActivity.class.getSimpleName())){
            parentIntent = new Intent( this, CardListActivity.class);
            parentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        return parentIntent;
    }
}
