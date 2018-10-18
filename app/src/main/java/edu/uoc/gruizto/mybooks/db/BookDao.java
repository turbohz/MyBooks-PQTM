package edu.uoc.gruizto.mybooks.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface BookDao {
    @Query("SELECT * FROM mybooks ORDER BY id ASC")
    List<Book> getAllBooks();

    @Query("SELECT * FROM mybooks WHERE id=:id LIMIT 1")
    Book findBookById(String id);

    @Insert
    void insert(Book book);
}
