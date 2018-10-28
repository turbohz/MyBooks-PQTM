package edu.uoc.gruizto.mybooks.activity;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import edu.uoc.gruizto.mybooks.R;
import edu.uoc.gruizto.mybooks.db.Book;
import edu.uoc.gruizto.mybooks.fragment.BookDetailFragment;
import edu.uoc.gruizto.mybooks.model.AppViewModel;

public class BookListActivity extends AppCompatActivity {

    private static final String TAG = BookListActivity.class.getName();
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        // Set up Action Toolbar

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up (floating) Action Button

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            // snackbar is similar to a toast, but can have behavior
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            }
        });

        // If we find a detail view, we're in two pane mode
        // (it is only used in the large-screen layouts (res/values-w900dp) layout

        if (findViewById(R.id.item_detail_container) != null) {
            mTwoPane = true;
        }

        final AppViewModel model = ViewModelProviders.of(this).get(AppViewModel.class);

        RecyclerView recyclerView = findViewById(R.id.book_list);
        assert recyclerView != null;
        recyclerView.setAdapter(
            new BookListActivity.SimpleItemRecyclerViewAdapter(
                this,
                model.getBooks(),
                mTwoPane
            )
        );
    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<BookListActivity.SimpleItemRecyclerViewAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView titleView;
            final TextView authorView;

            ViewHolder(View view) {
                super(view);
                titleView = view.findViewById(R.id.item_list_title);
                authorView = view.findViewById(R.id.item_list_author);
            }
        }

        private static final int ODD_ROW_VIEW_TYPE = 0;
        private static final int EVEN_ROW_VIEW_TYPE = 1;
        private final BookListActivity mParentActivity;
        private final List<Book> mValues;
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Book item = (Book) view.getTag();
            if (mTwoPane) {
                // create fragment state bundle
                Bundle arguments = new Bundle();
                arguments.putString(BookDetailFragment.ARG_ITEM_ID, item.id);
                // create the detail fragment, and provide it with the Bundle
                BookDetailFragment fragment = new BookDetailFragment();
                fragment.setArguments(arguments);
                // add it to the activity back stack, using a fragment transaction
                mParentActivity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.item_detail_container, fragment)
                        .commit();
            } else {
                Context context = view.getContext();
                Intent intent = new Intent(context, BookDetailActivity.class);
                intent.putExtra(BookDetailFragment.ARG_ITEM_ID, item.id);

                context.startActivity(intent);
            }
            }
        };

        SimpleItemRecyclerViewAdapter(BookListActivity parent,
                                      List<Book> items,
                                      boolean twoPane) {
            mValues = items;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        @Override
        public int getItemViewType(int position) {
            // HEADSUP! first "position" is 0, but we'll count rows starting from 1, which is more natural
            // position 0,2,4 .. will then be odd rows
            return (position % 2 == 0) ? ODD_ROW_VIEW_TYPE : EVEN_ROW_VIEW_TYPE;
        }

        @Override
        public BookListActivity.SimpleItemRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            @LayoutRes int layout;

            switch (viewType) {
                case ODD_ROW_VIEW_TYPE:
                    layout = R.layout.item_list_content_odd;
                    break;
                case EVEN_ROW_VIEW_TYPE:
                    layout = R.layout.item_list_content_even;
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected viewType:"+viewType);
            }

            View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
            return new BookListActivity.SimpleItemRecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final BookListActivity.SimpleItemRecyclerViewAdapter.ViewHolder holder, int position) {
            holder.titleView.setText(mValues.get(position).title);
            holder.authorView.setText(mValues.get(position).author);
            // store Book instance as a TAG
            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public void setItems(List<Book> items) {
            mValues.clear();
            mValues.addAll(items);
            notifyDataSetChanged();
        }
    }
}
