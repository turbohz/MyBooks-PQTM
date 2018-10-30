package edu.uoc.gruizto.mybooks.fragment;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import edu.uoc.gruizto.mybooks.R;
import edu.uoc.gruizto.mybooks.db.Book;
import edu.uoc.gruizto.mybooks.model.AppViewModel;
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
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The content this fragment is presenting.
     */
    private Book book;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BookDetailFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            String bookId = getArguments().getString(ARG_ITEM_ID);
            AppViewModel model = ViewModelProviders.of(this).get(AppViewModel.class);
            book = model.findBookById(bookId);

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.item_detail, container, false);

        // Show the book details

        if (book != null) {

            StringBuilder builder = new StringBuilder();
            String lineBreak = "<br/>";
            builder
                    .append("<strong>" + book.title + "</strong>")
                    .append(lineBreak)
                    .append(book.publicationDate)
                    .append("<p>" + book.description + "</p>")
            ;

            ((TextView) rootView.findViewById(R.id.item_detail)).setText(Html.fromHtml(builder.toString()));

            // Update picture

            ImageView cover = rootView.findViewById(R.id.book_cover);
            Picasso.get().load(book.coverUrl).into(cover);

            // Update view title

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(book.title);
            }
        } else {

            // hide default image

            ImageView cover = rootView.findViewById(R.id.book_cover);
            cover.setVisibility(View.GONE);
        }

        return rootView;
    }
}
