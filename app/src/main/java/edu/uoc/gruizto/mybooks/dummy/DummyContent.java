package edu.uoc.gruizto.mybooks.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uoc.gruizto.mybooks.model.BookItem;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<BookItem> ITEMS = new ArrayList<BookItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, BookItem> ITEM_MAP = new HashMap<String, BookItem>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(BookItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static BookItem createDummyItem(int position) {
        BookItem bookItem = new BookItem();
        bookItem.id = String.valueOf(position);
        bookItem.title = "BookItem " + position;
        bookItem.description = makeDescription(position);
        return bookItem;
    }

    private static String makeDescription(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Description for BookItem at #").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }
}
