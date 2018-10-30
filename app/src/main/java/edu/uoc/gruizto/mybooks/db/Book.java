package edu.uoc.gruizto.mybooks.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "mybooks")
public class Book {

    @PrimaryKey
    @NonNull
    public String id;
    public String title;
    public String author;
    public String publicationDate;
    public String description;
    public String coverUrl;

    @Override
    public String toString() {
        return String.format("Book:%s:%s", this.id, this.title);
    }
}
