package edu.uoc.gruizto.mybooks.activity;

import android.app.NotificationManager;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.LayoutRes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import edu.uoc.gruizto.mybooks.R;
import edu.uoc.gruizto.mybooks.db.Book;
import edu.uoc.gruizto.mybooks.fragment.BookDetailFragment;
import edu.uoc.gruizto.mybooks.messaging.ChannelBuilder;
import edu.uoc.gruizto.mybooks.model.AppViewModel;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class BookListActivity extends AppCompatActivity {

    private static final String TAG = BookListActivity.class.getName();
    private static final int GOOGLE_SERVICES_UPDATE_DIALOG_REQUEST = 1;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private CompositeDisposable mDisposable;
    private SwipeRefreshLayout mRefresh;
    private AppViewModel mViewModel;
    private SimpleItemRecyclerViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private String mCurrentBookId; // used in two pane view
    private Drawer mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library

        new ChannelBuilder(this).build();

        // Set up Action Toolbar

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // If we find a detail view, we're in two pane mode
        // (it is only used in the large-screen layouts (res/values-w900dp) layout

        if (findViewById(R.id.item_detail_container) != null) {
            mTwoPane = true;
        }

        mViewModel = ViewModelProviders.of(this).get(AppViewModel.class);

        // build recycler view with cached data

        mRecyclerView = findViewById(R.id.book_list);
        assert mRecyclerView != null;

        mAdapter = new SimpleItemRecyclerViewAdapter(
                this,
                mViewModel.getBooks(),
                mTwoPane
            );

        mRecyclerView.setAdapter(mAdapter);

        // Configure slide to refresh

        mRefresh = findViewById(R.id.book_list_refresh);

        mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mAdapter.clear();
                refreshModel();
            }
        });

        // Configure drawer

        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.drawer_item_home);
        SecondaryDrawerItem item2 = new SecondaryDrawerItem().withIdentifier(2).withName(R.string.drawer_item_settings);

        // create the drawer and remember the `mDrawer` result

        mDrawer = new DrawerBuilder()
            .withActivity(this)
            .withToolbar(toolbar)
            .addDrawerItems(
                    item1,
                    new DividerDrawerItem(),
                    item2,
                    new SecondaryDrawerItem()
            )
            .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                @Override
                public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                    // do something with the clicked item :D
                    return true;
                }
            })
            .build();


        // Set up (floating) Action Button to reset app state
        // FIXME: Get rid of this before deploy

	    FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "App state reset", Snackbar.LENGTH_LONG);
                FirebaseAuth.getInstance().signOut();
                mViewModel.deleteAllBooks();
                mAdapter.clear();

                // if in twoPane view, clear book details fragment

                if (mTwoPane) {
                    BookDetailFragment fragment = new BookDetailFragment();
                    fragment.setArguments(new Bundle());
                    getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.item_detail_container, fragment)
                        .commit();
                }
            }
        });

        // Disposable is needed to clean up the Rx entities
        // used in the asynchronous refresh of the view model

        mDisposable = new CompositeDisposable();

        // start me up!

        logFirebaseInstanceIdToken();

        Intent intent = getIntent();

        if (null != intent) {
            onNewIntent(intent);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {

        super.onNewIntent(intent);

        Log.i(TAG, "Handling intent:"+intent.toString());

        // handle main intent

        String action = intent.getAction();

        if (action == Intent.ACTION_MAIN) {
            // try to do a refresh with data from Firebase
            refreshModel();
            return;
        } else {
            // display cached book list
            mAdapter.setItems(mViewModel.getBooks());
        }

        // handle case where no action is specified
        // (this happens when coming from the detail activity

        if (null == action) {
            return;
        }

        // handle notification intents

        String position = intent.getStringExtra(BookDetailFragment.ARG_ITEM_ID);

        switch (action) {

            case Intent.ACTION_VIEW:
                if (null == position || null == mViewModel.findBookById(position)) {
                    Snackbar.make(mRecyclerView, R.string.message_book_not_found, Snackbar.LENGTH_LONG).show();
                    return;
                } else {
                    showBook(position);
                }
                break;

            case Intent.ACTION_DELETE:
                if (null == position || null == mViewModel.findBookById(position)) {
                    Snackbar.make(mRecyclerView, R.string.message_book_not_found, Snackbar.LENGTH_LONG).show();
                    return;
                } else {
                    deleteBook(position);
                }
                break;

            default:

                // Ignore other actions
                break;
        }
    }

    private void deleteBook(String position) {
        mViewModel.deleteBook(mViewModel.findBookById(position));
        mAdapter.setItems(mViewModel.getBooks());
        // in two pane mode, clear screen if deleted book details are being displayed
        if (mTwoPane && position.equals(mCurrentBookId)) {
            clearDetails();
        }
        //
        Snackbar.make(mRecyclerView, MessageFormat.format(getString(R.string.message_book_deleted), position), Snackbar.LENGTH_LONG).show();
        // dismiss notification: it's easy, since we used the position as notification id
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(Integer.parseInt(position));
    }

    /**
     * clears the details pane.
     * Use when its content is invalid (for example, when the book has been removed)
     */
    private void clearDetails() {
        ViewGroup details = findViewById(R.id.item_detail_container);
        if (null != details) {
            details.removeAllViews();
        }
    }

    /**
     * use Rx Single to get book data asynchronously
     */
    private void refreshModel() {
        mViewModel.refresh()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new SingleObserver<List<Book>>() {
                @Override
                public void onSubscribe(Disposable d) {
                    // add to disposables, dispose onDestroy activity
                    mDisposable.add(d);
                }

                @Override
                public void onSuccess(List<Book> books) {
                    mRefresh.setRefreshing(false);
                    mAdapter.setItems(books);
                    clearDetails();
                }

                @Override
                public void onError(Throwable e) {
                    mRefresh.setRefreshing(false);
                    Snackbar.make(mRecyclerView, e.getMessage(), Snackbar.LENGTH_LONG).show();
                }});
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(null != mDisposable && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

        List<Integer> errorCodes = Arrays.asList(
                ConnectionResult.SERVICE_MISSING,
                ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED,
                ConnectionResult.SERVICE_DISABLED
        );

        int googleApiAvailabilityStatus = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (errorCodes.contains(googleApiAvailabilityStatus)) {

            Log.d(TAG, "Requesting Google Services Update");
            googleApiAvailability.getErrorDialog(this, googleApiAvailabilityStatus, GOOGLE_SERVICES_UPDATE_DIALOG_REQUEST).show();

        }
    }

    /**
     * We can use this instance id token to send messages
     * specifically to a particular device.
     *
     * Useful for testing.
     */
    protected void logFirebaseInstanceIdToken() {

        mViewModel
            .getFirebaseInstanceId()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new SingleObserver<String>() {
                @Override
                public void onSubscribe(Disposable d) {
                    // add to disposables, dispose onDestroy activity
                    mDisposable.add(d);
                }

                @Override
                public void onSuccess(String token) {
                    Log.i(TAG, "Firebase instance id token:" + token);
                }

                @Override
                public void onError(Throwable e) {
                    mRefresh.setRefreshing(false);
                    Snackbar.make(mRecyclerView, e.getMessage(), Snackbar.LENGTH_LONG).show();
                }});
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == GOOGLE_SERVICES_UPDATE_DIALOG_REQUEST) {
            Log.d(TAG, "Got result:"+String.valueOf(resultCode));
        }
    }

    private void showBook(String id) {

        Log.i(TAG, "Showing book "+id);

        if (mTwoPane) {
            // create fragment state bundle
            Bundle arguments = new Bundle();
            arguments.putString(BookDetailFragment.ARG_ITEM_ID, id);
            // create the detail fragment, and provide it with the Bundle
            BookDetailFragment fragment = new BookDetailFragment();
            fragment.setArguments(arguments);
            // add it to the activity back stack, using a fragment transaction
            this.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.item_detail_container, fragment)
                    .commit();
            mCurrentBookId = id;
        } else {

            Intent intent = new Intent(this, BookDetailActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.putExtra(BookDetailFragment.ARG_ITEM_ID, id);
            this.startActivity(intent);
        }
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
                mParentActivity.showBook(item.getId());
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
            holder.titleView.setText(mValues.get(position).getTitle());
            holder.authorView.setText(mValues.get(position).getAuthor());
            // store Book instance as a TAG
            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public void clear() {
            mValues.clear();
            notifyDataSetChanged();
        }

        public void setItems(List<Book> items) {
            mValues.clear();
            mValues.addAll(items);
            notifyDataSetChanged();
        }
    }
}
