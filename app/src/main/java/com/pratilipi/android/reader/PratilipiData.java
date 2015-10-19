package com.pratilipi.android.reader;

import java.util.Arrays;
import java.util.UUID;

public class PratilipiData {

    public static class Chapter {

        private String title;

        private Page[] pages;


        public Chapter( String title, Page[] pages ) {
            this.title = title;
            this.pages = pages;
        }


        public String getTitle() {
            return title;
        }

        public int getPageCount() {
            return pages == null ? 0 : pages.length;
        }

        public Page getPage( int pageNo ) {
            return pages == null || pages.length <= pageNo ? null : pages[ pageNo ];
        }

    }

    public static class Page {

        private Pagelet[] pagelets;


        public Page( Pagelet[] pagelets ) {
            this.pagelets = pagelets;
        }


        public int getPageletCount() {
            return pagelets == null ? 0 : pagelets.length;
        }

        public Pagelet getPagelet( int pageletNo ) {
            return pagelets == null || pagelets.length <= pageletNo ? null : pagelets[ pageletNo ];
        }

        public Pagelet[] getPagelets( int pageletStart, int pageletCount ) {
            return pagelets == null || pagelets.length <= pageletStart ? null : Arrays.copyOfRange(pagelets, pageletStart, pageletStart + pageletCount);
        }

    }

    public static class Pagelet {

        private String id;

        private String data;

        private PageletType type;


        public Pagelet( String data, PageletType type ) {
            this.id = UUID.randomUUID().toString();
            this.data = data;
            this.type = type;
        }


        public String getId() {
            return id;
        }

        public String getData() {
            return data;
        }

        public PageletType getType() {
            return type;
        }

    }

    public enum PageletType {
        TEXT, HTML, IMAGE
    }


    public Chapter[] chapters;


    public PratilipiData( Chapter[] chapters ) {
        this.chapters = chapters;
    }


    public int getChapterCount() {
        return chapters == null ? 0 : chapters.length;
    }

    public Chapter getChapter( int chapterNo ) {
        return chapters == null || chapters.length <= chapterNo ? null : chapters[ chapterNo ];
    }

}
