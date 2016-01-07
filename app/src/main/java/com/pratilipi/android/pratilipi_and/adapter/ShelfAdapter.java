package com.pratilipi.android.pratilipi_and.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.pratilipi.android.pratilipi_and.util.AppUtil;
import com.pratilipi.android.pratilipi_and.util.ContentUtil;
import com.pratilipi.android.pratilipi_and.util.ShelfUtil;
import com.pratilipi.android.reader.ReaderActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rahul Ranjan on 9/24/2015.
 */
public class ShelfAdapter extends RecyclerView.Adapter<ShelfAdapter.DataViewHolder> {

    private static final String LOG_TAG = ShelfAdapter.class.getSimpleName();


    private List<Pratilipi> mPratilipiList;
    private ViewGroup mViewGroup;
    private ImageLoader mImageLoader;
    private int mDeleteContentPosition = 1;
    private int mRemoveContentFromShelfPosition = 0;
    private int mAboutContentPosition = 2;

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
    public void onBindViewHolder(DataViewHolder holder, int position) {
        final Context context = mViewGroup.getContext();
        String lan = context.getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE).getString("selectedLanguage", "");
        Typeface typeFace = null;
        if(lan.equalsIgnoreCase(AppUtil.HINDI_LOCALE ))
            typeFace= Typeface.createFromAsset(context.getAssets(), "fonts/devanagari.ttf");
        else if(lan.equalsIgnoreCase( AppUtil.TAMIL_LOCALE ))
            typeFace= Typeface.createFromAsset(context.getAssets(), "fonts/tamil.ttf");
        else if(lan.equalsIgnoreCase( AppUtil.GUJARATI_LOCALE ))
            typeFace= Typeface.createFromAsset(context.getAssets(), "fonts/gujarati.ttf");

        holder.bookTitle.setTypeface(typeFace);
        holder.authorName.setTypeface(typeFace);
        final Pratilipi pratilipi = mPratilipiList.get(position);
        holder.bookTitle.setText(pratilipi.getTitle());
        holder.authorName.setText(pratilipi.getAuthorName());
        holder.bookCover.setImageUrl("http:" + pratilipi.getCoverImageUrl(), mImageLoader);
        holder.ratingBar.setRating(pratilipi.getAverageRating());

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //STARTING READER ACTIVITY
                Intent i = new Intent( context, ReaderActivity.class );
                i.putExtra(ReaderActivity.PRATILIPI, pratilipi);
                i.putExtra(ReaderActivity.PARENT_ACTIVITY_CLASS_NAME, context.getClass().getSimpleName());
                context.startActivity(i);
            }
        });


        List<String> menuItems = new ArrayList<>();
        menuItems.add("Remove From Shelf");
        if(pratilipi.getDownloadStatus() == PratilipiContract.PratilipiEntity.CONTENT_NOT_DOWNLOADED)
            menuItems.add("Download");
        else{
            menuItems.add("Delete Content");
        }
        menuItems.add("About");
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, menuItems);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        holder.dropdown.setAdapter(dataAdapter);
//        holder.dropdown.setSelection(0, false);
        holder.dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            int mCount = 0;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCount = mCount + 1;
                if(mCount > 1){
                    // On selecting a spinner item
                    String item = parent.getItemAtPosition(position).toString();
                    if(position == mRemoveContentFromShelfPosition) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);

                        builder.setTitle("Confirm");
                        builder.setMessage("Do you want to remove " + pratilipi.getTitle() + " from shelf?");

                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeContentFromShelf(context, pratilipi);

                                notifyItemRemoved(position);

                                //Update UI List
//                                Cursor cursor = context.getContentResolver().query(PratilipiContract.ShelfEntity.CONTENT_URI, null, null, null, null);
//                                swapCursor(cursor);

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

                    } else if(position == mDeleteContentPosition) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Confirm");
                        builder.setMessage("Are you sure you want to delete " + pratilipi.getTitle() + " from phone memory?");

                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteContent(context, pratilipi);
                                //COULDN'T UPDATE SPECIFIC PRATILIPI OBJECT'S DOWNLOAD_STATUS. SO FETCHED WHOLE LIST FROM DATABASE
                                //TODO : FIND PROPER SOLUTION FOR THIS HACK
                                Cursor cursor = context.getContentResolver().query(PratilipiContract.ShelfEntity.CONTENT_URI, null, null, null, null);
                                swapCursor(cursor);

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
                    } else if( position == mAboutContentPosition ){
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
            pratilipi.setDownloadStatus(c.getInt(c.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_DOWNLOAD_STATUS)));
            pratilipi.setCreationDate(c.getInt(c.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_CREATION_DATE)));

            mPratilipiList.add(pratilipi);
            ShelfAdapter.this.notifyDataSetChanged();
        }

        return;
    }

    private void removeContentFromShelf(final Context context, final Pratilipi pratilipi){
        ShelfUtil.removePratilipiFromShelf(context, pratilipi, new GetCallback() {
            @Override
            public void done(boolean isSuccessful, String data) {
                if (isSuccessful) {
                    onSuccess(context, data, pratilipi);
                } else {
                    onFailed(context, data);
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
        if(pratilipi.getDownloadStatus() == PratilipiContract.PratilipiEntity.CONTENT_NOT_DOWNLOADED){
            Log.e(LOG_TAG, "Content is NOT downloaded");
            return;
        }
        ContentUtil.delete(context, pratilipi);
    }
}
