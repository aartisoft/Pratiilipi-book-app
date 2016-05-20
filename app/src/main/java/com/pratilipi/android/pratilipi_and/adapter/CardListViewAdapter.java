package com.pratilipi.android.pratilipi_and.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.pratilipi.android.pratilipi_and.DetailActivity;
import com.pratilipi.android.pratilipi_and.R;
import com.pratilipi.android.pratilipi_and.data.PratilipiContract;
import com.pratilipi.android.pratilipi_and.datafiles.Pratilipi;
import com.pratilipi.android.reader.ReaderActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rahul Ranjan on 9/8/2015.
 */
public class CardListViewAdapter extends RecyclerView.Adapter<CardListViewAdapter.DataViewHolder> {

    private static final String LOG_TAG = CardListViewAdapter.class.getSimpleName();

    private List<Pratilipi> mPratilipiList;
    private ViewGroup mViewGroup;

    public CardListViewAdapter(){
        mPratilipiList = new ArrayList<>();
}

    @Override
    public CardListViewAdapter.DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.mViewGroup = parent;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_list_cardview, parent, false);
        DataViewHolder dataViewHolder = new DataViewHolder(v);

        return dataViewHolder;
    }

    @Override
    public void onBindViewHolder(CardListViewAdapter.DataViewHolder holder, int position) {
        final Context context = mViewGroup.getContext();
        String lan = context.getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE).getString("selectedLanguage", "");
        Typeface typeFace = null;
        if(lan.equalsIgnoreCase("hi"))
            typeFace= Typeface.createFromAsset(context.getAssets(), "fonts/devanagari.ttf");
        else if(lan.equalsIgnoreCase("ta"))
            typeFace= Typeface.createFromAsset(context.getAssets(), "fonts/tamil.ttf");
        else if(lan.equalsIgnoreCase("gu"))
            typeFace= Typeface.createFromAsset(context.getAssets(), "fonts/gujarati.ttf");

        holder.bookTitle.setTypeface(typeFace);
        holder.authorName.setTypeface(typeFace);
        final Pratilipi pratilipi = mPratilipiList.get(position);
        holder.bookTitle.setText(pratilipi.getTitle());
        holder.authorName.setText(pratilipi.getAuthorName());

        //Hack to handle 2 different types of Url returned by different APIs.
        if(pratilipi.getCoverImageUrl().contains("http:")) {
            String coverUrl = pratilipi.getCoverImageUrl();
            if(coverUrl.contains("?"))
                coverUrl = coverUrl + "&" + "width=150";
            else
                coverUrl = coverUrl + "?" + "width=150";
            Glide
                    .with(context)
                    .load(coverUrl)
                    .placeholder(R.drawable.ic_default_image_120)   //Shows image while loading original image
                    .error(R.drawable.ic_default_image_120)     //Shows image after loading original image fails
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)    //Cache only result version
                    .skipMemoryCache(true)      //Skip memory cache
                    .into(holder.bookCover);
        } else
            //TODO : Remove this when Shelf and mobileInit API calls are made to Android module.
            Glide
                    .with(context)
                    .load("http:" + pratilipi.getCoverImageUrl())
                    .placeholder(R.drawable.ic_default_image_120)   //Shows image while loading original image
                    .error(R.drawable.ic_default_image_120)     //Shows image after loading original image fails
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)    //Cache only result version
                    .skipMemoryCache(true)      //Skip memory cache
                    .into(holder.bookCover);

        holder.ratingBar.setRating(pratilipi.getAverageRating());
        if( pratilipi.getPrice() == 0 )
            holder.price.setText(R.string.string_free);
        else
            holder.price.setText(String.valueOf(pratilipi.getPrice()));

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, DetailActivity.class);
                i.putExtra(ReaderActivity.PRATILIPI, pratilipi);
                i.putExtra(ReaderActivity.PARENT_ACTIVITY_CLASS_NAME, context.getClass().getSimpleName());
                context.startActivity(i);
                Log.e(LOG_TAG, "Item clicked. Title : " + pratilipi.getTitle());
            }
        });

    }

    @Override
    public int getItemCount() {
        return mPratilipiList.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public class DataViewHolder extends RecyclerView.ViewHolder{

        CardView cardView;
        TextView bookTitle;
        TextView authorName;
        ImageView bookCover;
        RatingBar ratingBar;
        TextView ratingCount;
//        TextView averageRating;
        ImageView imgOverflowButton ;
        TextView price;

        public DataViewHolder(View itemView) {
            super(itemView);

            cardView = (CardView)itemView.findViewById(R.id.card_list_cardview);
            bookTitle = (TextView)itemView.findViewById(R.id.card_list_title_textview);
            authorName = (TextView)itemView.findViewById(R.id.card_list_author_name_textview);
            bookCover = (ImageView)itemView.findViewById(R.id.card_list_cover_imageview);
            ratingBar = (RatingBar)itemView.findViewById(R.id.card_list_rating);
            ratingCount = (TextView)itemView.findViewById(R.id.card_list_rating_count_textview);
//            averageRating = (TextView)itemView.findViewById(R.id.averageRatingTextView);
            imgOverflowButton = (ImageView) itemView.findViewById(R.id.overflow_cardlist);
            price = (TextView) itemView.findViewById(R.id.card_list_price_textview);
        }
    }

    public void swapCursor(Cursor c){
        mPratilipiList.clear();
        if( c == null ) {
//            mPratilipiList = new ArrayList<>();
            CardListViewAdapter.this.notifyDataSetChanged();
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
//            CardListViewAdapter.this.notifyItemInserted( mPratilipiList.size()-1 );
            CardListViewAdapter.this.notifyDataSetChanged();
        }
        return;
    }

    public void addToPratilipiList( List<Pratilipi> pratilipiList ){
        mPratilipiList.addAll( pratilipiList );
    }

}
