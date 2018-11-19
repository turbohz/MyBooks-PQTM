package edu.uoc.gruizto.mybooks.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BookDao {
    @Query("SELECT * FROM mybooks ORDER BY id ASC")
    List<Book> getAll();

    @Query("SELECT * FROM mybooks WHERE id=:id LIMIT 1")
    Book findById(String id);

    @Insert
    void insert(Book book);

    @Delete
    void delete(Book book);

    @Query("DELETE FROM mybooks")
    void deleteAll();
}
