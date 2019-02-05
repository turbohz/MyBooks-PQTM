package edu.uoc.gruizto.mybooks.db

import android.app.Application
import androidx.lifecycle.LiveData

class BookRepository(application: Application) {

    val db:AppDatabase = AppDatabase.getDatabase(application)

    private val mBookDao: BookDao = db.bookDao()

    val all: LiveData<List<Book>>
        get() = mBookDao.all

    fun insert(book: Book) {
        mBookDao.insert(book)
    }

    fun delete(book: Book) {
        mBookDao.delete(book)
    }

    fun findById(id: String): Book? {
        return mBookDao.findById(id)
    }

    fun deleteAll() {
        mBookDao.deleteAll()
    }
}

