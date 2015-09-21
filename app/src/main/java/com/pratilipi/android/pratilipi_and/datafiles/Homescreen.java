package com.pratilipi.android.pratilipi_and.datafiles;

import java.io.Serializable;

/**
 * Created by Rahul Ranjan on 8/27/2015.
 */
public class Homescreen implements Serializable {

    // private variables
    private String pratilipiId; // Pratilipi id
    private String title; // content title
    private String contentType; // PRATILIPI,IMAGE
    private String authorId;
    private String authorFullName;
    private String coverImageUrl;
//    private long _ratingCount;
//    private long _starCount;
    private float price;
    private float discountedPrice;

    public Homescreen(){}

    public String getPratilipiId() {
        return pratilipiId;
    }

    public String getAuthorFullName() {
        return authorFullName;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getContentType() {
        return contentType;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public float getDiscountedPrice() {
        return discountedPrice;
    }

    public float getPrice() {
        return price;
    }

    public String getTitle() {
        return title;
    }

    public void setPratilipiId(String pratilipiId) {
        this.pratilipiId = pratilipiId;
    }

    public void setAuthorFullName(String authorFullName) {
        this.authorFullName = authorFullName;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public void setDiscountedPrice(float discountedPrice) {
        this.discountedPrice = discountedPrice;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
