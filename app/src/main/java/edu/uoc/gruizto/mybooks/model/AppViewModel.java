package edu.uoc.gruizto.mybooks.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;

import java.util.List;

import edu.uoc.gruizto.mybooks.db.Book;
import edu.uoc.gruizto.mybooks.db.BookRepository;

public class AppViewModel extends AndroidViewModel {

    private BookRepository mBookRepository;
    private List<Book> mAllBooks;

    public AppViewModel (Application application) {
        super(application);
        mBookRepository = new BookRepository(application);
    }

    public List<Book> getBooks() { return mBookRepository.getAll(); }

    public boolean exists(Book book) {
        return (null != findBookById(book.id));
    }
    public void insertBook(Book book) { mBookRepository.insert(book); }


    public Book findBookById(String id) {
        return mBookRepository.findById(id);
    }
}
