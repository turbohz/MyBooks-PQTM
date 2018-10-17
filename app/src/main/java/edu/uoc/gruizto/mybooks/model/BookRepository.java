package edu.uoc.gruizto.mybooks.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uoc.gruizto.mybooks.db.Book;

/**
 * Generates and gives access to the book collection data
 */
public class BookRepository {

    /**
     * A list of sample book items.
     */
    public static final List<Book> BOOKS = new ArrayList<Book>();

    /**
     * A map of sample book items, by ID.
     */
    public static final Map<String, Book> BOOK_MAP = new HashMap<String, Book>();

    private static final int COUNT = 25;

    static {
        for (int i = 1; i <= COUNT; i++) {
            addBook(createDummyBook(i));
        }
    }

    private static void addBook(Book book) {
        BOOKS.add(book);
        BOOK_MAP.put(book.id, book);
    }

    private static Book createDummyBook(int position) {
        Book book = new Book();
        book.id = String.valueOf(position);
        book.title = "Title for Book #" + position;
        book.description = "Description for Book #"+ position;
        book.author = "Author for Book #"+ position;
        return book;
    }
}
