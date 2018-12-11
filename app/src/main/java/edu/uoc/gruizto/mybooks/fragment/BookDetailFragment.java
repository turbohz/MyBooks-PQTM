package edu.uoc.gruizto.mybooks.fragment;

import android.app.Activity;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import edu.uoc.gruizto.mybooks.R;
import edu.uoc.gruizto.mybooks.db.Book;
import edu.uoc.gruizto.mybooks.activity.BookDetailActivity;
import edu.uoc.gruizto.mybooks.activity.BookListActivity;


/**
 * A fragment representing a single Book detail screen.
 * This fragment is either contained in a {@link BookListActivity}
 * in two-pane mode (on tablets) or a {@link BookDetailActivity}
 * on handsets.
 */
public class BookDetailFragment extends Fragment {
    /**
     * The key to pass around the book id
     */
    public static final String ARG_ITEM_ID = "item_id";
    /**
     * The key use to store the book as a Parcel
     */
    public static final String ARG_BOOK_KEY = "book";

    /**
     * The content this fragment is presenting.
     */
    private Book mBook;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BookDetailFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_BOOK_KEY)) {
            mBook = getArguments().getParcelable(ARG_BOOK_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.item_detail, container, false);

        // Show the book details

        if (mBook != null) {

            StringBuilder builder = new StringBuilder();
            String lineBreak = "<br/>";
            builder
                    .append("<strong>" + mBook.getTitle() + "</strong>")
                    .append(lineBreak)
                    .append(mBook.getPublicationDate())
                    .append("<p>" + mBook.getDescription() + "</p>")
            ;

            ((TextView) rootView.findViewById(R.id.item_detail)).setText(Html.fromHtml(builder.toString()));

            // Update picture

            ImageView cover = rootView.findViewById(R.id.book_cover);
            Picasso.get().load(mBook.getCoverUrl()).into(cover);

            // Update view title

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mBook.getTitle());
            }
        } else {

            // hide default image

            ImageView cover = rootView.findViewById(R.id.book_cover);
            cover.setVisibility(View.GONE);
        }

        return rootView;
    }
}
