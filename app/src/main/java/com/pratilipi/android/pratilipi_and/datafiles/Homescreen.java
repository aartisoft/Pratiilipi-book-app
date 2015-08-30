package com.pratilipi.android.pratilipi_and.datafiles;

import java.io.Serializable;

/**
 * Created by Rahul Ranjan on 8/27/2015.
 */
public class Homescreen implements Serializable {

    // private variables
    private String mPratilipiId; // Pratilipi id
    private String mTitle; // content title
    private String mContentType; // PRATILIPI,IMAGE
    private String mAuthorId;
    private String mAuthorFullName;
    private String mCoverImageUrl;
//    private long _ratingCount;
//    private long _starCount;
    private float mPrice;
    private float mDiscountedPrice;

    public Homescreen(){}

    public String getmPratilipiId() {
        return mPratilipiId;
    }

    public String getmAuthorFullName() {
        return mAuthorFullName;
    }

    public String getmAuthorId() {
        return mAuthorId;
    }

    public String getmContentType() {
        return mContentType;
    }

    public String getmCoverImageUrl() {
        return mCoverImageUrl;
    }

    public float getmDiscountedPrice() {
        return mDiscountedPrice;
    }

    public float getmPrice() {
        return mPrice;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmPratilipiId(String mPratilipiId) {
        this.mPratilipiId = mPratilipiId;
    }

    public void setmAuthorFullName(String mAuthorFullName) {
        this.mAuthorFullName = mAuthorFullName;
    }

    public void setmAuthorId(String mAuthorId) {
        this.mAuthorId = mAuthorId;
    }

    public void setmContentType(String mContentType) {
        this.mContentType = mContentType;
    }

    public void setmCoverImageUrl(String mCoverImageUrl) {
        this.mCoverImageUrl = mCoverImageUrl;
    }

    public void setmDiscountedPrice(float mDiscountedPrice) {
        this.mDiscountedPrice = mDiscountedPrice;
    }

    public void setmPrice(float mPrice) {
        this.mPrice = mPrice;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }
}
