package edu.uoc.gruizto.mybooks.db;

import android.app.Application;

import java.util.List;

public class BookRepository {

    private BookDao mBookDao;

    public BookRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mBookDao = db.bookDao();
    }

    public List<Book> getAll() { return mBookDao.getAll(); }

    public void insert(Book book) { mBookDao.insert(book); }

    public void delete(Book book) { mBookDao.delete(book); }

    public Book findById(String id) {
        return mBookDao.findById(id);
    }

    public void deleteAll() { mBookDao.deleteAll(); }
}

