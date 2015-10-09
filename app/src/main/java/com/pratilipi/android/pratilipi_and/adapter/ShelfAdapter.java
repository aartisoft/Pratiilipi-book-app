package com.pratilipi.android.pratilipi_and.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.pratilipi.android.pratilipi_and.AppController;
import com.pratilipi.android.pratilipi_and.R;
import com.pratilipi.android.pratilipi_and.data.PratilipiContract;
import com.pratilipi.android.pratilipi_and.datafiles.Pratilipi;
import com.pratilipi.android.pratilipi_and.util.AppUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rahul Ranjan on 9/24/2015.
 */
public class ShelfAdapter extends RecyclerView.Adapter<ShelfAdapter.DataViewHolder>  {

    private static final String LOG_TAG = ShelfAdapter.class.getSimpleName();

    private List<Pratilipi> mPratilipiList;
    private ViewGroup mViewGroup;
    private ImageLoader mImageLoader;

    public ShelfAdapter(){
        Log.e(LOG_TAG, "ShelfAdapter Constructor");
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
        Log.e(LOG_TAG, "onCreateViewHolder function");
        this.mViewGroup = parent;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.shelf_cardview, parent, false);
        DataViewHolder dataViewHolder = new DataViewHolder(v);

        return dataViewHolder;
    }

    @Override
    public void onBindViewHolder(DataViewHolder holder, int position) {
        Log.e(LOG_TAG, "onBindViewHolder function");
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
//                Intent i = new Intent(context, DetailActivity.class);
//                i.putExtra(DetailActivity.PRATILIPI, pratilipi);
//                i.putExtra(DetailActivity.PARENT_ACTIVITY_CLASS_NAME, context.getClass().getSimpleName());
//                context.startActivity(i);
                Uri uri = PratilipiContract.PratilipiEntity.getPratilipiByIdUri(pratilipi.getPratilipiId());
                Log.e(LOG_TAG, "Pratilipi URI : " + uri);
                Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
                cursor.moveToFirst();
                Toast.makeText(context, "Is Downloaded : " + cursor.getInt(cursor.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_DOWNLOAD_STATUS)), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class DataViewHolder extends RecyclerView.ViewHolder{

        CardView cardView;
        TextView bookTitle;
        TextView authorName;
        NetworkImageView bookCover;
        RatingBar ratingBar;
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
        }
    }

    public void swapCursor( Cursor c ){
        Log.e(LOG_TAG, "swapCursor function");
        if( c == null ) {
            mPratilipiList.clear();
            notifyDataSetChanged();
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
        }

        Log.e(LOG_TAG, "Notify data set changed");
        notifyDataSetChanged();
        return;
    }
}
