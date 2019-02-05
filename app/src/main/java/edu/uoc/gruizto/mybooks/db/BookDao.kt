package edu.uoc.gruizto.mybooks.db

import androidx.lifecycle.LiveData
import androidx.room.*
import io.reactivex.Completable

@Dao
interface BookDao {
    @get:Query("SELECT * FROM mybooks ORDER BY id ASC")
    val all: LiveData<List<Book>>

    @Query("SELECT * FROM mybooks WHERE id=:id LIMIT 1")
    fun findById(id: String): Book?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(book: Book): Completable

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMany(books: List<Book>): Completable

    @Delete
    fun delete(book: Book)

    @Query("DELETE FROM mybooks")
    fun deleteAll()
}
