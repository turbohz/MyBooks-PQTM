package edu.uoc.gruizto.mybooks.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BookDao {
    @get:Query("SELECT * FROM mybooks ORDER BY id ASC")
    val all: List<Book>

    @Query("SELECT * FROM mybooks WHERE id=:id LIMIT 1")
    fun findById(id: String): Book

    @Insert
    fun insert(book: Book)

    @Delete
    fun delete(book: Book)

    @Query("DELETE FROM mybooks")
    fun deleteAll()
}
