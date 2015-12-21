package com.pratilipi.android.pratilipi_and.datafiles;

/**
 * Created by Rahul Ranjan on 11/21/2015.
 */
public class Content {

    /**
     * TODO : REMOVE VARIABLES WHICH ARE NOT USED AS MENTIONED IN FOLLOWING LINES.
     * chapterNo and pageNo are not used anywhere and can be removed.
     * pratilipiId is also not used.
     */

    private String chapterNo;
    private byte[] imageContent;
    private String pageNo;
    private String pratilipiId;
    private String textContent;

    public String getChapterNo() {
        return chapterNo;
    }

    public void setChapterNo(String chapterNo) {
        this.chapterNo = chapterNo;
    }

    public byte[] getImageContent() {
        return imageContent;
    }

    public void setImageContent(byte[] imageContent) {
        this.imageContent = imageContent;
    }

    public String getPageNo() {
        return pageNo;
    }

    public void setPageNo(String pageNo) {
        this.pageNo = pageNo;
    }

    public String getPratilipiId() {
        return pratilipiId;
    }

    public void setPratilipiId(String pratilipiId) {
        this.pratilipiId = pratilipiId;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }
}
