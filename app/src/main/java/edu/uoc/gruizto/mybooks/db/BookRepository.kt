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

    fun delete(book: Book) : Completable {
        return mBookDao.delete(book).subscribeOn(Schedulers.io())
    }

    fun deleteAll(): Completable {
        // we need to "wrap" the Completable
        // because the generated code in the DAO
        // uses the DB before creating the Completable
        // before we are allowed to schedule the
        // action to be done in a background thread
        // TODO: check if the issue is fixed in future versions of Room
        return Maybe.just("")
                .flatMapCompletable { _ -> mBookDao.deleteAll() }
                .subscribeOn(Schedulers.io())
    }
}

