package com.pratilipi.android.pratilipi_and;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.pratilipi.android.pratilipi_and.data.PratilipiContract;
import com.pratilipi.android.pratilipi_and.data.PratilipiDbHelper;
import com.pratilipi.android.pratilipi_and.datafiles.Pratilipi;
import com.pratilipi.android.pratilipi_and.datafiles.User;
import com.pratilipi.android.pratilipi_and.service.DownloadService;
import com.pratilipi.android.pratilipi_and.util.PratilipiUtil;
import com.pratilipi.android.pratilipi_and.util.ShelfUtil;
import com.pratilipi.android.pratilipi_and.util.UserUtil;
import com.pratilipi.android.reader.ReaderActivity;

import org.json.JSONArray;
import org.json.JSONException;

public class DetailActivity extends AppCompatActivity {

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    private Context mContext;
    private Pratilipi mPratilipi;
    private String mPratilipiId;
    private String mParentActivityClassName;
    private JSONArray mIndexJsonArray;
    private int mChapterCount;
    private int mChapterNumber;
    private int mPageCount;
    private int mPageNumber;
    Button addToShelfButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mContext = this;

        getSupportActionBar().setElevation(1f);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_logo);

        mPratilipi = (Pratilipi) getIntent().getSerializableExtra(ReaderActivity.PRATILIPI);
        mParentActivityClassName = getIntent().getStringExtra(ReaderActivity.PARENT_ACTIVITY_CLASS_NAME);
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

        Button readButton = (Button) findViewById(R.id.detail_read_button);
        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent( DetailActivity.this, ReaderActivity.class );
                i.putExtra(ReaderActivity.PRATILIPI, mPratilipi);
                i.putExtra(ReaderActivity.PARENT_ACTIVITY_CLASS_NAME, mContext.getClass().getSimpleName());
                startActivity(i);
            }
        });

        addToShelfButton = (Button) findViewById(R.id.detail_add_to_shelf_button);
        addToShelfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShelfUtil.addPratilipiToShelf(mContext, mPratilipi, new GetCallback() {
                    @Override
                    public void done(boolean isSuccessful, String data) {
                        Log.e(LOG_TAG, "Add To Shelf Callback");
                        Log.e(LOG_TAG, "Response Data : " + data);
                        if (isSuccessful) {
                            User user = UserUtil.getLoggedInUser(mContext);
                            UserUtil.incrementContentInShelfCount(mContext, user.getEmail());
                            if (mPratilipi.getDownloadStatus() != 1) {
                                downloadContent(mPratilipiId);
                                addToShelfButton.setVisibility(View.GONE);

                            }
                        }
                        Toast.makeText(getBaseContext(), "Is Add To shelf Successful : " + isSuccessful, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        User userLog = UserUtil.getLoggedInUser(mContext);
        if(userLog == null) {
            addToShelfButton.setVisibility(View.VISIBLE);
        }else{
            queryForBookId(mContext, mPratilipiId);
        }

    }

    public int queryForBookId(Context context, String mPratilipiId){

        String query = "SELECT * FROM " +
                PratilipiContract.ShelfEntity.TABLE_NAME +
                " WHERE " +
                PratilipiContract.ShelfEntity.COLUMN_PRATILIPI_ID + " =?";
        String[] selectionArgs = new String[]{mPratilipiId};

        //TODO : FIND WORK AROUND FOR TO REPLACE rawQuery FUNCTION
        Cursor cursor = new PratilipiDbHelper(context)
                .getReadableDatabase()
                .rawQuery(query, selectionArgs);

        if( cursor != null && cursor.moveToFirst()){
            addToShelfButton.setVisibility(View.GONE);
            return cursor.getInt(0);
        }

        return 0;
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
        if( mParentActivityClassName.equals(CardListActivity.class.getSimpleName()))
            parentIntent = new Intent( this, CardListActivity.class);
        else if(mParentActivityClassName.equals(MainActivity.class.getSimpleName()))
            parentIntent = new Intent( this, MainActivity.class);

        parentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        return parentIntent;
    }

    private boolean downloadContent(String pratilipiId){
        mPratilipiId = pratilipiId;
        Uri uri = PratilipiContract.PratilipiEntity.getPratilipiByIdUri(pratilipiId);
        Log.e(LOG_TAG, "Get Pratilipi By Id URI : " + uri.toString());
        Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
         if(cursor.moveToFirst()){
            String contentType = cursor.getString(cursor.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_CONTENT_TYPE));
            if(contentType.equalsIgnoreCase(DownloadService.TEXT_CONTENT_TYPE)) {
                String indexString = cursor.getString(cursor.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_INDEX));
                mPageCount = cursor.getInt(cursor.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_PAGE_COUNT));
                mPageNumber = 1;
                Log.e(LOG_TAG, "Is index present : " + (indexString));
                if (indexString == null || indexString.isEmpty()) {
                    //WHEN INDEX IS NULL WHOLE CONTENT SHOULD BE UNDER CHAPTER 1
                    mChapterCount = 1;
                    mChapterNumber = 1;
                    startDownloadService( DownloadService.TEXT_CONTENT_TYPE, pratilipiId, 0 );
                } else{
                    //WHEN INDEX IS NOT NULL
                    try {
                        mIndexJsonArray = new JSONArray(indexString);
                        mChapterCount = mIndexJsonArray.length();
                        mChapterNumber = 0;

                        int chapterStartPageNo = mIndexJsonArray.getJSONObject(mChapterNumber).getInt("pageNo");
                        Log.e(LOG_TAG, "Chapter Number / Page Number : " + (mChapterNumber + 1) + " / " + chapterStartPageNo);

                        startDownloadService( DownloadService.TEXT_CONTENT_TYPE, pratilipiId, chapterStartPageNo );

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else{
                //CONTENT_TYPE = IMAGE
                mIndexJsonArray = null;
                mChapterCount = 0;
                startDownloadService( DownloadService.TEXT_CONTENT_TYPE, pratilipiId, 0 );
            }

        }

        return false;
    }

    private class DownloadReceiver extends ResultReceiver {
        public DownloadReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == DownloadService.STATUS_CODE_SUCCESS) {
                //Make request for next page
                if( mPageCount > mPageNumber ){
                    float completedPercentage = ((float)mPageNumber/(float)mPageCount) *100;
                    Toast.makeText(mContext, (int)(double) completedPercentage + "% Completed", Toast.LENGTH_SHORT)
                            .show();
                    mPageNumber++;
                    if(mIndexJsonArray == null){
                        try{
                            int chapterStartPageNo =  mIndexJsonArray.getJSONObject(mChapterNumber).getInt("pageNo");

                            startDownloadService( DownloadService.TEXT_CONTENT_TYPE, mPratilipiId, chapterStartPageNo );

                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                    } else{
                        startDownloadService( DownloadService.TEXT_CONTENT_TYPE, mPratilipiId, 0 );
                    }

                } else{
                    //UPDATE PRATILIPI ENTITY is_downloaded = true
                    PratilipiUtil.updatePratilipiDownloadStatus(
                            mContext,
                            mPratilipiId,
                            PratilipiContract.PratilipiEntity.CONTENT_DOWNLOADED
                    );
                    Toast.makeText(mContext, "Download Completed", Toast.LENGTH_LONG)
                            .show();
                }
            } else{
                Toast.makeText(mContext, "Error Occured while downloading file.", Toast.LENGTH_LONG);
            }
        }
    }

    private void startDownloadService(String contentType,
                                      String pratilipiId,
                                      Integer chapterStartPageNumber){

        //increment chapter number only for text content
        if (contentType.equals(DownloadService.TEXT_CONTENT_TYPE)
                && mPageNumber >= chapterStartPageNumber
                && mChapterNumber < mChapterCount)
            mChapterNumber++;

        Log.e(LOG_TAG, "Page Number : " + mPageNumber);
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(DownloadService.INTENT_EXTRA_CONTENT_TYPE, contentType);
        intent.putExtra(DownloadService.INTENT_EXTRA_CHAPTER_NUMBER, mChapterNumber);
        intent.putExtra(DownloadService.INTENT_EXTRA_PRATILIPI_ID, pratilipiId);
        intent.putExtra(DownloadService.INTENT_EXTRA_PAGE_NUMBER, mPageNumber);
        intent.putExtra(DownloadService.INTENT_EXTRA_RECEIVER, new DownloadReceiver(new Handler()));
        startService(intent);
    }
}
