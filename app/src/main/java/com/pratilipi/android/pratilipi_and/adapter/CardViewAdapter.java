package com.pratilipi.android.pratilipi_and.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.pratilipi.android.pratilipi_and.DetailActivity;
import com.pratilipi.android.pratilipi_and.R;
import com.pratilipi.android.pratilipi_and.data.PratilipiContract;
import com.pratilipi.android.pratilipi_and.datafiles.Homescreen;
import com.pratilipi.android.pratilipi_and.datafiles.Pratilipi;
import com.pratilipi.android.pratilipi_and.util.PratilipiUtil;
import com.pratilipi.android.reader.ReaderActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rahul Ranjan on 8/26/2015.
 */
public class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.DataViewHolder> {

    private static final String LOG_TAG = CardViewAdapter.class.getSimpleName();

    private List<Homescreen> mHomescreenList;
    private ViewGroup mViewGroup;

    public CardViewAdapter(){
        Log.e(LOG_TAG, "CardViewAdapter constructor called");
        this.mHomescreenList = new ArrayList<>();
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mHomescreenList.size();
    }

    @Override
    public CardViewAdapter.DataViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Log.e(LOG_TAG, "onCreateViewHolder function called");
        this.mViewGroup = viewGroup;
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_layout, viewGroup, false);
        DataViewHolder dataViewHolder = new DataViewHolder(v);

        return dataViewHolder;
    }

    @Override
    public void onBindViewHolder(CardViewAdapter.DataViewHolder dateViewHolder, int position) {

        final Context context = mViewGroup.getContext();
        final Homescreen homescreenObject = mHomescreenList.get(position);
        dateViewHolder.bookTitle.setText(homescreenObject.getTitle());
        //Hack to handle 2 different types of Url returned by different APIs.
        if(homescreenObject.getCoverImageUrl().contains("http:")) {
            String coverUrl = homescreenObject.getCoverImageUrl();
            if(coverUrl.contains("?"))
                coverUrl = coverUrl + "&" + "width=150";
            else
                coverUrl = coverUrl + "?" + "width=150";
            Glide
                    .with(context)
                    .load(coverUrl)
                    .placeholder(R.drawable.ic_default_image_120)   //Shows image while loading original image
                    .error(R.drawable.ic_default_image_120)     //Shows image after loading original image fails
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)    //Cache only result version
                    .skipMemoryCache(true)      //Skip memory cache
                    .into(dateViewHolder.bookCover);
        } else
            //TODO : Remove this when Shelf and mobileInit API calls are made to Android module.
            Glide
                    .with(context)
                    .load("http:" + homescreenObject.getCoverImageUrl())
                    .placeholder(R.drawable.ic_default_image_120)   //Shows image while loading original image
                    .error(R.drawable.ic_default_image_120)     //Shows image after loading original image fails
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)    //Cache only result version
                    .skipMemoryCache(true)      //Skip memory cache
                    .into(dateViewHolder.bookCover);

        if( homescreenObject.getPrice() == 0f) {
            dateViewHolder.freeButton.setText("FREE!");
        }

        dateViewHolder.mHomeCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.e(LOG_TAG, "Item clicked. Title : " + homescreenObject.getTitle());
                Pratilipi pratilipi = PratilipiUtil.getPratilipiById(context, homescreenObject.getPratilipiId());
                Intent i = new Intent(context, DetailActivity.class);
                i.putExtra(ReaderActivity.PRATILIPI, pratilipi);
                i.putExtra(ReaderActivity.PARENT_ACTIVITY_CLASS_NAME, context.getClass().getSimpleName());
                context.startActivity(i);
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public class DataViewHolder extends RecyclerView.ViewHolder{

        CardView mHomeCardView;
        ImageView bookCover;
//        RatingBar mRatingBar;
        TextView bookTitle;
//        TextView authorName;
//        TextView ratingCount;
//        TextView avgeragerating;
        TextView freeButton;
//        TextView priceText;
        public DataViewHolder(View itemView) {
            super(itemView);

            mHomeCardView = (CardView)itemView.findViewById(R.id.home_card_view);
            bookCover = (ImageView)itemView.findViewById(R.id.card_layout_cover_imageview);
//            mRatingBar = (RatingBar)itemView.findViewById(R.id.averageRatingRatingBar);
            bookTitle = (TextView)itemView.findViewById(R.id.card_layout_title_textview);
//            authorName = (TextView)itemView.findViewById(R.id.overlay_author_name);
//            ratingCount = (TextView)itemView.findViewById(R.id.featuredPageRatingNumber);
//            avgeragerating = (TextView)itemView.findViewById(R.id.averageRatingTextView);
            freeButton = (TextView)itemView.findViewById(R.id.card_layout_price_textview);
//            priceText= (TextView)itemView.findViewById(R.id.priceText);
            bookTitle.setSelected(true);

        }
    }

    public void swapCursor( Cursor cursor ){
        if( cursor == null ) {
            mHomescreenList = new ArrayList<>();
            this.notifyDataSetChanged();
            return;
        }

        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            Homescreen homescreen = new Homescreen();
            homescreen.setPratilipiId(cursor.getString(cursor.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_PRATILIPI_ID)));
            homescreen.setTitle(cursor.getString(cursor.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_TITLE)));
            homescreen.setCoverImageUrl(cursor.getString(cursor.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_COVER_IMAGE_URL)));
            homescreen.setPrice(cursor.getFloat(cursor.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_PRICE)));
            homescreen.setDiscountedPrice(cursor.getFloat(cursor.getColumnIndex(PratilipiContract.PratilipiEntity.COLUMN_DISCOUNTED_PRICE)));
            mHomescreenList.add(homescreen);
            notifyDataSetChanged();
        }

        return;
    }
}
