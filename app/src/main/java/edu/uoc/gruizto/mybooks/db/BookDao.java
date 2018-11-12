package edu.uoc.gruizto.mybooks.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

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
