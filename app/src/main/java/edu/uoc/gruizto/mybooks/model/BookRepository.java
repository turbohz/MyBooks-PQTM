package edu.uoc.gruizto.mybooks.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uoc.gruizto.mybooks.R;

/**
 * Generates and gives access to the book collection data
 */
public class BookRepository {

    /**
     * A list of sample book items.
     */
    public static final List<BookItem> BOOKS = new ArrayList<BookItem>();

    /**
     * A map of sample book items, by ID.
     */
    public static final Map<String, BookItem> BOOK_MAP = new HashMap<String, BookItem>();

    private static final int COUNT = 25;

    static {
        for (int i = 1; i <= COUNT; i++) {
            addBook(createDummyBook(i));
        }
    }

    private static void addBook(BookItem book) {
        BOOKS.add(book);
        BOOK_MAP.put(book.id, book);
    }

    private static BookItem createDummyBook(int position) {
        BookItem book = new BookItem();
        book.id = String.valueOf(position);
        book.title = "Book #" + position;
        book.description = "Description for Book #"+ position;
        return book;
    }
}
