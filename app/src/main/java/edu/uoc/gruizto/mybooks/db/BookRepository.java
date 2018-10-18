package edu.uoc.gruizto.mybooks.db;

import android.app.Application;

import java.util.List;

public class BookRepository {

    private BookDao mBookDao;

    public BookRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mBookDao = db.bookDao();
    }

    public List<Book> getAllBooks() { return mBookDao.getAllBooks(); }

    public void insert(Book book) { mBookDao.insert(book); }

    public Book findBookById(String id) {
        return mBookDao.findBookById(id);
    }
}

