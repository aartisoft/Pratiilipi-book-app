package com.pratilipi.android.pratilipi_and.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.pratilipi.android.pratilipi_and.AppController;
import com.pratilipi.android.pratilipi_and.DetailActivity;
import com.pratilipi.android.pratilipi_and.GetCallback;
import com.pratilipi.android.pratilipi_and.R;
import com.pratilipi.android.pratilipi_and.Widget.MySpinner;
import com.pratilipi.android.pratilipi_and.data.PratilipiContract;
import com.pratilipi.android.pratilipi_and.datafiles.Pratilipi;
import com.pratilipi.android.pratilipi_and.service.DownloadService;
import com.pratilipi.android.pratilipi_and.util.AppUtil;
import com.pratilipi.android.pratilipi_and.util.ContentUtil;
import com.pratilipi.android.pratilipi_and.util.PratilipiUtil;
import com.pratilipi.android.pratilipi_and.util.ShelfUtil;
import com.pratilipi.android.reader.ReaderActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rahul Ranjan on 9/24/2015.
 */
public class ShelfAdapter extends RecyclerView.Adapter<ShelfAdapter.DataViewHolder> {

    private static final String LOG_TAG = ShelfAdapter.class.getSimpleName();


    private Context mContext;
    private List<Pratilipi> mPratilipiList;
    private ViewGroup mViewGroup;
    private ImageLoader mImageLoader;
    private Integer mChapterCount;
    private Integer mChapterNumber;
    private JSONArray mIndexJsonArray;

    private final String REMOVE_FROM_SHELF = "Remove From Shelf";
    private final String DOWNLOAD_CONTENT = "Download Content";
    private final String DELETE_CONTENT = "Delete Content";
    private final String ABOUT = "About";

    public ShelfAdapter(){
        mPratilipiList = new ArrayList<>();
        mImageLoader = AppController.getInstance().getImageLoader();
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mPratilipiList.size();
    }

    @Override
    public DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.mViewGroup = parent;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.shelf_cardview, parent, false);
        DataViewHolder dataViewHolder = new DataViewHolder(v);

        return dataViewHolder;
    }

    @Override
    public void onBindViewHolder(final DataViewHolder holder, int position) {
        final Context context = mViewGroup.getContext();
        String lan = context.getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE).getString("selectedLanguage", "");
        Typeface typeFace = null;
        if(lan.equalsIgnoreCase(AppUtil.HINDI_LOCALE ))
            typeFace= Typeface.createFromAsset(context.getAssets(), "fonts/devanagari.ttf");
        else if(lan.equalsIgnoreCase( AppUtil.TAMIL_LOCALE ))
            typeFace= Typeface.createFromAsset(context.getAssets(), "fonts/tamil.ttf");
        else if(lan.equalsIgnoreCase( AppUtil.GUJARATI_LOCALE ))
            typeFace= Typeface.createFromAsset(context.getAssets(), "fonts/gujarati.ttf");

        final Pratilipi pratilipi = mPratilipiList.get(position);

        holder.bookTitle.setTypeface(typeFace);
        holder.authorName.setTypeface(typeFace);
        holder.bookTitle.setText(pratilipi.getTitle());
        holder.authorName.setText(pratilipi.getAuthorName());
        holder.bookCover.setImageUrl("http:" + pratilipi.getCoverImageUrl(), mImageLoader);
        holder.ratingBar.setRating(pratilipi.getAverageRating());

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(pratilipi.getContentType().toLowerCase().equals(DownloadService.IMAGE_COTENT_TYPE)){
                    Toast.makeText(context, "Development is not complete for this type of content", Toast.LENGTH_SHORT).show();
                } else {
                    //STARTING READER ACTIVITY
                    Intent i = new Intent(context, ReaderActivity.class);
                    i.putExtra(ReaderActivity.PRATILIPI, pratilipi);
                    i.putExtra(ReaderActivity.PARENT_ACTIVITY_CLASS_NAME, context.getClass().getSimpleName());
                    context.startActivity(i);
                }
            }
        });


        int downloadStatus = new ShelfUtil().getContentDownloadStatus(context, pratilipi.getPratilipiId());

        final List<String> menuItems = new ArrayList<>();
        menuItems.add(REMOVE_FROM_SHELF);
        if(downloadStatus == PratilipiContract.ShelfEntity.CONTENT_NOT_DOWNLOADED)
            menuItems.add(DOWNLOAD_CONTENT);
        else
            menuItems.add(DELETE_CONTENT);

        menuItems.add(ABOUT);
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, menuItems);

        final View parent = (View) holder.dropdown.getParent();
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        holder.dropdown.setAdapter(dataAdapter);
//        holder.dropdown.setSelection(0, false);
        holder.dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            int mCount = 0;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,final int position, long id) {
                mCount = mCount + 1;
                String selectedItemTitle = parent.getSelectedItem().toString();
                if (mCount > 1) {
                    // On selecting a spinner item
//                    String item = parent.getItemAtPosition(position).toString();
                    if (selectedItemTitle.equals(REMOVE_FROM_SHELF)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);

                        builder.setTitle("Confirm");
                        builder.setMessage("Do you want to remove " + pratilipi.getTitle() + " from shelf?");

                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeContentFromShelf(context, pratilipi);
                                dialog.dismiss();
                            }

                        });

                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                                dialog.dismiss();
                            }
                        });

                        AlertDialog alert = builder.create();
                        alert.show();

                    } else if (selectedItemTitle.equals(DELETE_CONTENT)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Confirm");
                        builder.setMessage("Are you sure you want to delete " + pratilipi.getTitle() + " from phone memory?");

                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteContent(context, pratilipi);
                                //Change dropdown item
                                menuItems.remove(position);
                                menuItems.add(position, DOWNLOAD_CONTENT);

                                dialog.dismiss();
                            }

                        });

                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                                dialog.dismiss();
                            }
                        });

                        AlertDialog alert = builder.create();
                        alert.show();
                    } else if (selectedItemTitle.equals(DOWNLOAD_CONTENT)) {
                        //DOWNLOAD CONTENT TO PHONE MEMORY
                        downloadContent(context, pratilipi);

                        //Change dropdown item
                        menuItems.remove(position);
                        menuItems.add(position, DELETE_CONTENT);
                    } else if (selectedItemTitle.equals(ABOUT)) {
                        //Start Detail Activity
                        Intent i = new Intent(context, DetailActivity.class);
                        i.putExtra(ReaderActivity.PRATILIPI, pratilipi);
                        i.putExtra(ReaderActivity.PARENT_ACTIVITY_CLASS_NAME, context.getClass().getSimpleName());
                        context.startActivity(i);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        parent.post(new Runnable() {
            @Override
            public void run() {
                final Rect hitRect = new Rect();
                parent.getHitRect(hitRect);
                hitRect.right = hitRect.right - hitRect.left;
                hitRect.bottom = hitRect.bottom - hitRect.top;
                hitRect.top = 0;
                hitRect.left = 0;
                parent.setTouchDelegate(new TouchDelegate(hitRect, holder.dropdown));
            }
        });

    }

    public class DataViewHolder extends RecyclerView.ViewHolder{

        CardView cardView;
        TextView bookTitle;
        TextView authorName;
        NetworkImageView bookCover;
        RatingBar ratingBar;
        MySpinner dropdown;
//        TextView ratingCount;
//        TextView averageRating;
//        ImageView imgOverflowButton ;

        public DataViewHolder(View itemView) {
            super(itemView);

            cardView = (CardView)itemView.findViewById(R.id.shelf_cardview);
            bookTitle = (TextView)itemView.findViewById(R.id.shelf_title_textview);
            authorName = (TextView)itemView.findViewById(R.id.shelf_author_name_textview);
            bookCover = (NetworkImageView)itemView.findViewById(R.id.shelf_cover_imageview);
            ratingBar = (RatingBar)itemView.findViewById(R.id.shelf_rating);
//            ratingCount = (TextView)itemView.findViewById(R.id.card_list_rating_count_textview);
//            averageRating = (TextView)itemView.findViewById(R.id.averageRatingTextView);
//            imgOverflowButton = (ImageView) itemView.findViewById(R.id.overflow_cardlist);
            dropdown = (MySpinner) itemView.findViewById(R.id.shelf_dropdown_menu);
        }
    }
    public void swapCursor( Cursor c ){
        mPratilipiList.clear();
        if( c == null ) {
            ShelfAdapter.this.notifyDataSetChanged();
            return;
        }

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            Pratilipi pratilipi = new Pratilipi();
            pratilipi.setPratilipiId(c.getString(c.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_PRATILIPI_ID)));
            if(c.getString(c.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_TITLE)).isEmpty())
                pratilipi.setTitle(c.getString(c.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_TITLE_EN)));
            else
                pratilipi.setTitle(c.getString(c.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_TITLE)));

            pratilipi.setType(c.getString(c.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_TYPE)));
            pratilipi.setAuthorName(c.getString(c.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_AUTHOR_NAME)));
            pratilipi.setLanguageId(c.getString(c.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_LANGUAGE_ID)));
            pratilipi.setLanguageName(c.getString(c.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_LANGUAGE_NAME)));
            pratilipi.setState(c.getString(c.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_STATE)));
            pratilipi.setSummary(c.getString(c.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_SUMMARY)));
            pratilipi.setIndex(c.getString(c.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_INDEX)));
            pratilipi.setContentType(c.getString(c.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_CONTENT_TYPE)));
            pratilipi.setRatingCount(c.getLong(c.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_RATING_COUNT)));
            pratilipi.setAverageRating(c.getFloat(c.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_AVERAGE_RATING)));
            pratilipi.setPrice(c.getDouble(c.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_PRICE)));
            pratilipi.setDiscountedPrice(c.getDouble(c.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_DISCOUNTED_PRICE)));
            pratilipi.setFontSize(c.getString(c.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_FONT_SIZE)));
            pratilipi.setCurrentChapter(c.getInt(c.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_CURRENT_CHAPTER)));
            pratilipi.setPageCount(c.getInt(c.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_PAGE_COUNT)));
            pratilipi.setCurrentPage(c.getInt(c.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_CURRENT_PAGE)));
            pratilipi.setCoverImageUrl(c.getString(c.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_COVER_IMAGE_URL)));
            pratilipi.setGenreList(c.getString(c.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_GENRE_NAME_LIST)));
            pratilipi.setCreationDate(c.getInt(c.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_CREATION_DATE)));

            mPratilipiList.add(pratilipi);
            ShelfAdapter.this.notifyDataSetChanged();
        }

        return;
    }

    private void removeContentFromShelf(final Context context, final Pratilipi pratilipi){
        //Remove Pratilipi From mPratilipiList
        final int position = mPratilipiList.indexOf(pratilipi);
        mPratilipiList.remove(position);
        notifyItemRemoved(position);
        ShelfUtil.removePratilipiFromShelf(context, pratilipi, new GetCallback() {
            @Override
            public void done(boolean isSuccessful, String data) {
                if (isSuccessful) {
                    onSuccess(context, data, pratilipi);
                } else {
                    onFailed(context, data);
                    //Add pratilipi back to mPratilipiList
                    mPratilipiList.add(position, pratilipi);
                    notifyItemInserted(position);
                }
            }
        });
    }

    private void onSuccess(Context context, String data, Pratilipi pratilipi){
        //delete content
        deleteContent(context, pratilipi);
    }

    private void onFailed(Context context, String data){
        try {
            JSONObject jsonObject = new JSONObject(data);
            Toast.makeText(context, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
        } catch ( JSONException e){
            e.printStackTrace();
        }
    }

    private void deleteContent(Context context, Pratilipi pratilipi){
        int contentDownloadStatus = new ShelfUtil().getContentDownloadStatus(context, pratilipi.getPratilipiId());
        if(contentDownloadStatus == PratilipiContract.ShelfEntity.CONTENT_NOT_DOWNLOADED){
            Log.e(LOG_TAG, "Content is NOT downloaded");
            return;
        }
        ContentUtil.delete(context, pratilipi);
    }

    /**
     * FOLLOWING FUNCTIONS ARE USED TO DOWNLOAD CONTENT. AND ARE DUPLICATE OF FUNCTIONS PRESENT IN
     * DetailActivity.
     * TODO : Move these functions to ContentUtil to remove duplicate code.
     */

    private boolean downloadContent(Context context, Pratilipi pratilipi){
        mContext = context;
        String pratilipiId = pratilipi.getPratilipiId();

        //UPDATE DOWNLOAD CONTENT STATUS
        new ShelfUtil().updateContentDownloadStatus(
                context,
                pratilipiId,
                PratilipiContract.ShelfEntity.CONTENT_DOWNLOADING
        );

        Uri uri = PratilipiContract.PratilipiEntity.getPratilipiByIdUri(pratilipiId);
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if(cursor.moveToFirst()){
            String contentType = cursor.getString(cursor.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_CONTENT_TYPE));
            if(contentType.equalsIgnoreCase(DownloadService.TEXT_CONTENT_TYPE)) {
                String indexString = cursor.getString(cursor.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_INDEX));
                if (indexString == null || indexString.isEmpty()) {
                    //WHEN INDEX IS NULL WHOLE CONTENT SHOULD BE UNDER CHAPTER 1
                    mChapterCount = 1;
                    mChapterNumber = 1;
                } else{
                    //WHEN INDEX IS NOT NULL
                    try {
                        mIndexJsonArray = new JSONArray(indexString);
                        mChapterCount = mIndexJsonArray.length();
                        mChapterNumber = 1;

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                startDownloadService(context, DownloadService.TEXT_CONTENT_TYPE, pratilipi);
            } else{
                //CONTENT_TYPE = IMAGE
//                mIndexJsonArray = null;
//                mChapterCount = 0;
//                startDownloadService( DownloadService.TEXT_CONTENT_TYPE, pratilipiId);
                Toast.makeText(context, "Development is not complete for this type of content", Toast.LENGTH_SHORT).show();
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
                String pratilipiId = resultData.getString(DownloadService.INTENT_EXTRA_PRATILIPI_ID);

                //check content_download_status. If User cancelled download
                int downloadStatus = new ShelfUtil().getContentDownloadStatus(mContext, pratilipiId);
                if(downloadStatus == PratilipiContract.ShelfEntity.CONTENT_NOT_DOWNLOADED) {
                    Log.i(LOG_TAG, "Download cancelled by user");
                    return;
                }

                //Make request for next page
                Pratilipi pratilipi = PratilipiUtil.getPratilipiById(mContext, pratilipiId);
                if( mChapterCount > mChapterNumber ){
                    //Show download status
                    String message = mChapterNumber + " out of " + mChapterCount + " chapters downloaded";
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT)
                            .show();
                    //Start downloading next chapter;
                    mChapterNumber++;
                    startDownloadService(mContext, DownloadService.TEXT_CONTENT_TYPE, pratilipi);

                } else{
                    //UPDATE PRATILIPI ENTITY is_downloaded = true
                    new ShelfUtil().updateContentDownloadStatus(
                            mContext,
                            pratilipiId,
                            PratilipiContract.ShelfEntity.CONTENT_DOWNLOADED
                    );
                    Toast.makeText(mContext, "Download Completed", Toast.LENGTH_LONG)
                            .show();
                }
            } else{
                Toast.makeText(mContext, "Error Occurred while downloading file.", Toast.LENGTH_LONG);
            }
        }
    }

    private void startDownloadService(Context context, String contentType, Pratilipi pratilipi){

        //CHECK WHETHER CONTENT IS PRESENT IN DB OR NOT.
        ContentUtil contentUtil = new ContentUtil();
        Cursor cursor = contentUtil.getContentfromDb(context, pratilipi, mChapterNumber, null);
        if(cursor != null && cursor.moveToNext()) {
            //if last chapter then do nothing
            if(mChapterCount == mChapterNumber)
                return;
            //Else start downloading next chapter.
            mChapterNumber++;
            new ShelfUtil().updateContentDownloadStatus(
                    mContext,
                    pratilipi.getPratilipiId(),
                    PratilipiContract.ShelfEntity.CONTENT_DOWNLOADED
            );
            startDownloadService(context, contentType, pratilipi);
        } else {
            Intent intent = new Intent(context, DownloadService.class);
            intent.putExtra(DownloadService.INTENT_EXTRA_CONTENT_TYPE, contentType);
            intent.putExtra(DownloadService.INTENT_EXTRA_CHAPTER_NUMBER, mChapterNumber);
            intent.putExtra(DownloadService.INTENT_EXTRA_PRATILIPI_ID, pratilipi.getPratilipiId());
            //All chapter contains only one page.
            intent.putExtra(DownloadService.INTENT_EXTRA_PAGE_NUMBER, 1);
            intent.putExtra(DownloadService.INTENT_EXTRA_RECEIVER, new DownloadReceiver(new Handler()));
            context.startService(intent);
        }
    }
}
