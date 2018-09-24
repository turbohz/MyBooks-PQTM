package edu.uoc.gruizto.mybooks.model;

import java.util.Date;


public class BookItem {

    private static final String DEFAULT_COVER_URL = "";

    public String id;
    public String title;
    public String author;
    public Date publicationDate;
    public String description;
    public String coverUrl;

    public BookItem() {
        this.id = "0";
        this.title = "Default title";
        this.author = "Default author";
        this.publicationDate = new Date();
        this.description = "Default description";
        this.coverUrl = DEFAULT_COVER_URL;
    }

    @Override
    public String toString() {
        return String.format("BookItem:%s:%s", this.id, this.title);
    }
}
