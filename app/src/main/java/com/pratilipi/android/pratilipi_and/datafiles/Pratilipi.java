package com.pratilipi.android.pratilipi_and.datafiles;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Rahul Ranjan on 9/10/2015.
 */
public class Pratilipi implements Serializable {

    private String pratilipiId;
    private String title;
    private String type;
    private String authorId;
    private String authorName;
    private String languageId;
    private String languageName;
    private String state;
    private String summary;
    private String index;
    private String contentType;
    private Long ratingCount;
    private float averageRating;
    private double price;
    private double discountedPrice;
    private String fontSize;
    private int currentChapter;
    private int pageCount;
    private int currentPage;
    private String pageUrl;
    private String coverImageUrl;
    private String genreList;
    private int creationDate;
//    private int downloadStatus;


    public String getPratilipiId() {
        return pratilipiId;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getLanguageId() {
        return languageId;
    }

    public String getLanguageName() {
        return languageName;
    }

    public String getState() {
        return state;
    }

    public String getSummary() {
        return summary;
    }

    public String getIndex() {
        return index;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getRatingCount() {
        return ratingCount;
    }

    public float getAverageRating() {
        return averageRating;
    }

    public double getPrice() {
        return price;
    }

    public double getDiscountedPrice() {
        return discountedPrice;
    }

    public String getFontSize() {
        return fontSize;
    }

    public int getCurrentChapter() {
        return currentChapter;
    }

    public int getPageCount() {
        return pageCount;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public String getPageUrl(){
        return pageUrl;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public String getGenreList() {
        return genreList;
    }

    public int getCreationDate() {
        return creationDate;
    }

//    public int getDownloadStatus(){
//        return downloadStatus;
//    }


    //SETTERS
    public void setPratilipiId(String pratilipiId) {
        this.pratilipiId = pratilipiId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public void setLanguageId(String languageId) {
        this.languageId = languageId;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setRatingCount(Long ratingCount) {
        this.ratingCount = ratingCount;
    }

    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setDiscountedPrice(double discountedPrice) {
        this.discountedPrice = discountedPrice;
    }

    public void setCurrentChapter(int currentChapter) {
        this.currentChapter = currentChapter;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public void setPageUrl(String pageUrl){
        //New website url format : hindi.pratilipi.com/pageUrl
        String name;
        if(isValidJSONObject(languageName)) {
            Gson gson = new GsonBuilder().create();
            JsonObject object = gson.fromJson(languageName, JsonElement.class).getAsJsonObject();
            name = object.get("nameEn").toString();
        } else
            name = languageName;
        Log.e("Pratilipi datafile", "Language Object : " + name);
        //substring is taken to remove double inverted quotes.
        this.pageUrl = name.substring(1, name.length()-1).toLowerCase() + ".pratilipi.com" + pageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
    }

    public void setGenreList(String genreList) {
        this.genreList = genreList;
    }

    public void setCreationDate(int creationDate) {
        this.creationDate = creationDate;
    }

//    public void setDownloadStatus(int downloadStatus){
//        this.downloadStatus = downloadStatus;
//    }

    private boolean isValidJSONObject(String language){
        try {
            new JSONObject(language);
        } catch (JSONException ex) {
            //Not a valid JSON object
            return false;
        }
        return true;
    }
}
