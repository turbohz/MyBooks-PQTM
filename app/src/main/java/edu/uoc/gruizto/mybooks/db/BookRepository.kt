package edu.uoc.gruizto.mybooks.db

import android.app.Application

class BookRepository(application: Application) {

    private val mBookDao: BookDao

    val all: List<Book>
        get() = mBookDao.all

    init {
        val db = AppDatabase.getDatabase(application)
        mBookDao = db!!.bookDao()
    }

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

