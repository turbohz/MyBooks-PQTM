package edu.uoc.gruizto.mybooks.db

import android.app.Application
import androidx.lifecycle.LiveData
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers

class BookRepository(application: Application) {

    val db:AppDatabase = AppDatabase.getDatabase(application)

    private val mBookDao: BookDao = db.bookDao()

    val all: LiveData<List<Book>>
        get() = mBookDao.all

    fun findById(id: String): Maybe<Book> {
        return mBookDao.findById(id).subscribeOn(Schedulers.io())
    }

    fun insert(book: Book): Completable {
        return mBookDao.insert(book).subscribeOn(Schedulers.io())
    }

    fun insertMany(books: List<Book>): Completable {
        return mBookDao.insertMany(books).subscribeOn(Schedulers.io())
    }

    fun delete(book: Book) {
        mBookDao.delete(book)
    }

    fun deleteAll() {
        mBookDao.deleteAll()
    }
}

