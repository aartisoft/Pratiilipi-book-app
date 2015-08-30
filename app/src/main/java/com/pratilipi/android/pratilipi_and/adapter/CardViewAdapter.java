package com.pratilipi.android.pratilipi_and.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.pratilipi.android.pratilipi_and.AppController;
import com.pratilipi.android.pratilipi_and.R;
import com.pratilipi.android.pratilipi_and.datafiles.Homescreen;

import java.util.List;

/**
 * Created by Rahul Ranjan on 8/26/2015.
 */
public class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.DataViewHolder> {

    private static final String LOG_TAG = CardViewAdapter.class.getSimpleName();

    private List<Homescreen> mHomescreenList;
    private ViewGroup mViewGroup;

    private ImageLoader mImageLoader;

    public CardViewAdapter( List<Homescreen> homescreens ){
        this.mHomescreenList = homescreens;
        mImageLoader = new AppController().getInstance().getImageLoader();
    }

    @Override
    public int getItemCount() {
        return mHomescreenList.size();
    }

    @Override
    public CardViewAdapter.DataViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        this.mViewGroup = viewGroup;
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_layout, viewGroup, false);
        DataViewHolder dataViewHolder = new DataViewHolder(v);

        return dataViewHolder;
    }

    @Override
    public void onBindViewHolder(CardViewAdapter.DataViewHolder dateViewHolder, int position) {
        final Context context = mViewGroup.getContext();

        final Homescreen homescreenObject = mHomescreenList.get(position);

        dateViewHolder.bookTitle.setText(homescreenObject.getmTitle());
        dateViewHolder.bookCover.setImageUrl("http:" + homescreenObject.getmCoverImageUrl(), mImageLoader);

        if( homescreenObject.getmPrice() == 0f) {
            dateViewHolder.freeButton.setText("FREE!");
        }
//        else {
//            holder.freeButton.setText("\u20B9"+ "100");
//            holder.freeButton.setTextColor(Color.GRAY);
//            holder.freeButton.setPaintFlags(holder.freeButton.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
//            holder.priceText.setText(" \u20B9" + "20");
//        }
//        random =!random;


//        if (homescreenObject.get_ratingCount() > 0) {
//
//            float val = (float) homescreenObject.get_starCount() / homescreenObject.get_ratingCount();
//            if (val != 0.0) {
////                holder.mRatingBar.setRating(val);
//
//                NumberFormat numberformatter = NumberFormat.getNumberInstance();
//                numberformatter.setMaximumFractionDigits(1);
//                numberformatter.setMinimumFractionDigits(1);
//                String rating = numberformatter.format(val);
//
////                holder.ratingCount.setText(String.valueOf("("+metadataObj.get_ratingCount() + " ratings)"));
////                holder.avgeragerating.setText("Average rating: " + rating + "/5");
//
//            }else{
//                Log.d("Val is Null", "");
//            }
//        }

        dateViewHolder.mHomeCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(LOG_TAG, "Card Clicked " + homescreenObject.getmTitle());
//                Intent i = new Intent(context, DetailPageActivity.class);
//                i.putExtra(DetailPageActivity.METADATA, (Serializable) homescreenObject);
//                context.startActivity(i);
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public class DataViewHolder extends RecyclerView.ViewHolder{

        CardView mHomeCardView;
        NetworkImageView bookCover;
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
            bookCover = (NetworkImageView)itemView.findViewById(R.id.card_layout_cover_imageview);
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
}
