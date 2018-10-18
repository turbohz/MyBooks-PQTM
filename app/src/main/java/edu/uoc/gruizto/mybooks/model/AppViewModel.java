package edu.uoc.gruizto.mybooks.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;

import java.util.List;

import edu.uoc.gruizto.mybooks.db.Book;
import edu.uoc.gruizto.mybooks.db.BookRepository;

public class AppViewModel extends AndroidViewModel {

    private BookRepository mRepository;
    private List<Book> mAllBooks;

    public AppViewModel (Application application) {
        super(application);
        mRepository = new BookRepository(application);
    }

    public List<Book> getBooks() { return mRepository.getAllBooks(); }

    public boolean exists(Book book) {
        return (null != findBookById(book.id));
    }

    public void insert(Book book) { mRepository.insert(book); }

    public Book findBookById(String id) {
        return mRepository.findBookById(id);
    }
}
