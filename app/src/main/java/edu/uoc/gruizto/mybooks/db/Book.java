package edu.uoc.gruizto.mybooks.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

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
