package edu.uoc.gruizto.mybooks.db;

public class Book {

    private static final String DEFAULT_COVER_URL = "https://i.pinimg.com/564x/27/7a/0f/277a0f2733bc01ec7710a51faca1de31.jpg";

    public String id;
    public String title;
    public String author;
    public String publicationDate;
    public String description;
    public String coverUrl;

    public Book() {
        this.id = "0";
        this.title = "Default title";
        this.author = "Default author";
        this.publicationDate = "17/10/2018";
        this.description = "Default description";
        this.coverUrl = DEFAULT_COVER_URL;
    }

    @Override
    public String toString() {
        return String.format("Book:%s:%s", this.id, this.title);
    }
}
