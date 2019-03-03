package edu.uoc.gruizto.mybooks.activity;

import android.app.NotificationManager;

import androidx.lifecycle.ViewModelProviders;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.squareup.picasso.Picasso;

import org.reactivestreams.Subscription;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import edu.uoc.gruizto.mybooks.BuildConfig;
import edu.uoc.gruizto.mybooks.R;
import edu.uoc.gruizto.mybooks.db.Book;
import edu.uoc.gruizto.mybooks.fragment.BookDetailFragment;
import edu.uoc.gruizto.mybooks.messaging.ChannelBuilder;
import edu.uoc.gruizto.mybooks.model.AppViewModel;
import edu.uoc.gruizto.mybooks.remote.Firebase;
import edu.uoc.gruizto.mybooks.share.ShareDrawerBuilder;
import edu.uoc.gruizto.mybooks.share.ShareIntentBuilder;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
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

        // Disposable is needed to clean up the Rx entities
        // used in the asynchronous refresh of the view model

        mDisposable = new CompositeDisposable();

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

        mAdapter = new SimpleItemRecyclerViewAdapter(this, mTwoPane, mViewModel.getBooks());
        mRecyclerView.setAdapter(mAdapter);

        // Configure slide to refresh

        mRefresh = findViewById(R.id.book_list_refresh);
        mRefresh.setOnRefreshListener(this::refreshBookList);

        // Configure drawer

        final ShareDrawerBuilder builder = new ShareDrawerBuilder(this);

        //initialize and create the image loader logic

        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Picasso.get().load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Picasso.get().cancelRequest(imageView);
            }
        });

        // Create the AccountHeader

        AccountHeader header = new AccountHeaderBuilder()
            .withActivity(this)
            .addProfiles(ShareDrawerBuilder.Companion.getProfile())
            .build();

        // create the drawer and remember the `mDrawer` result

        mDrawer = builder.getBuilder()
            .withAccountHeader(header)
            .withToolbar(toolbar)
            .addDrawerItems(
                    ShareDrawerBuilder.Companion.createItem(1, R.string.drawer_item_share_with_app),
                    ShareDrawerBuilder.Companion.createItem(2, R.string.drawer_item_copy_to_clipboard),
                    ShareDrawerBuilder.Companion.createItem(3, R.string.drawer_item_share_to_whatsapp)
            )
            .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                @Override
                public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                    ShareIntentBuilder builder;
                    Intent intent = null;
                    Context context = BookListActivity.this;
                    String shareText = context.getResources().getString(R.string.app_description);

                    switch (position) {
                        case 1: // Generic share

                            builder = new ShareIntentBuilder(context);
                            intent = builder
                                    .setText(shareText)
                                    .setImage(R.raw.icon)
                                    .build();

                            startActivity(Intent.createChooser(intent, getResources().getText(R.string.send_to)));
                            break;

                        case 2: // copy to clipboard

                            String label = context.getResources().getString(R.string.app_name);
                            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                            clipboardManager.setPrimaryClip(ClipData.newPlainText(label, shareText));

                            // notify the user

                            Toast.makeText(context, getResources().getString(R.string.message_share_to_clipboard_success), Toast.LENGTH_SHORT).show();

                            // close drawer

                            mDrawer.closeDrawer();
                            break;

                        case 3: // share to whatsapp

                            builder = new ShareIntentBuilder(context);
                            intent = builder
                                    .setText(shareText)
                                    .setImage(R.raw.icon)
                                    .setPackage("com.whatsapp")
                                    .build();
                            try {
                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                Log.e(TAG,e.getMessage());
                                Toast.makeText(context, "Whatsapp is not installed!", Toast.LENGTH_SHORT).show();
                            }

                            break;

                        default:
                            Log.w(TAG,"Unknown drawer option "+ position);
                    }

                    return true;
                }
            })
            .build();

        // this avoids having any item appear "selected"
        mDrawer.setSelection(0);

        if (BuildConfig.DEBUG) {

            // Set up (floating) Action Button to reset app state

            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setOnClickListener(view -> {
                Snackbar.make(view, "App state reset", Snackbar.LENGTH_LONG);
                FirebaseAuth.getInstance().signOut();
                mViewModel.deleteAllBooks()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(d -> mDisposable.add(d))
                    .subscribe();

                // if in twoPane view, clear book details fragment

                if (mTwoPane) {
                    BookDetailFragment fragment = new BookDetailFragment();
                    fragment.setArguments(new Bundle());
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
                            .commit();
                }
            });
        }

        // start me up!

        logFirebaseInstanceIdToken();

        Intent intent = getIntent();

        if (intent != null) {
            onNewIntent(intent);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {

        super.onNewIntent(intent);

        Log.i(TAG, "Handling intent:"+intent.toString());

        String action = intent.getAction();

        if(action == null){
            action = "";
        }

        if (action.equals(Intent.ACTION_MAIN)) {
            // try to do a refresh with data from Firebase
            // when we first launch the app
            refreshBookList();
            return;
        }

        String position = intent.getStringExtra(BookDetailFragment.ARG_ITEM_ID);

        if (position == null) {
            // no need to do anything
            return;
        }

        // FIXME: Sometimes position is null (back from details)

        Single<Book> withBook = mViewModel.findBookById(position)
                .toSingle()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(mDisposable::add)
                .doOnError(e -> {
                    if (e instanceof NoSuchElementException) {
                        Snackbar.make(mRecyclerView, R.string.message_book_not_found, Snackbar.LENGTH_LONG).show();
                    } else {
                        Log.e(TAG, "Could not find book", e);
                    }
                });

        switch (action) {

            case Intent.ACTION_VIEW:
                withBook
                        .doOnSuccess(book -> showBook(book.getId()))
                        .subscribe();
                break;

            case Intent.ACTION_DELETE:
                withBook
                        .flatMapCompletable(this::deleteBook)
                        .subscribe();
                break;
        default:
            // this can happen when using the back button
            break;
        }
    }

    private Completable deleteBook(Book book) {

        return mViewModel.deleteBook(book).doOnComplete(() -> {
            String position = book.getId();
            // in two pane mode, clear screen if deleted book details are being displayed
            if (mTwoPane && position.equals(mCurrentBookId)) {
                clearDetails();
            }

            String message =  MessageFormat.format(getString(R.string.message_book_deleted), position);
            Snackbar.make(mRecyclerView, message, Snackbar.LENGTH_LONG).show();
            // dismiss notification: it's easy, since we used the position as notification id
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.cancel(Integer.parseInt(position));
        });
    }

    /**
     * clears the details pane.
     * Use when its content is invalid (for example, when the book has been removed)
     */
    private void clearDetails() {
        ViewGroup details = findViewById(R.id.item_detail_container);
        if (details != null) {
            details.removeAllViews();
        }
    }

    private void refreshBookList() {
        mViewModel.sync()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new CompletableObserver() {
                @Override
                public void onSubscribe(Disposable d) {
                    // add to disposables, dispose onDestroy activity
                    mDisposable.add(d);
                }

                @Override
                public void onComplete() {
                    mRefresh.setRefreshing(false);
                    clearDetails();
                }

                @Override
                public void onError(Throwable e) {
                    mRefresh.setRefreshing(false);
                    Snackbar.make(mRecyclerView, e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            });
    }

    @Override
    protected void onDestroy() {
        mAdapter.destroy();
        super.onDestroy();

        if(mDisposable != null && !mDisposable.isDisposed()) {
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

        Firebase.Companion.getFirebaseInstanceId()
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

    private void showBook(Book book) {
        if (mTwoPane) {
            mCurrentBookId = book.getId();
            Bundle arguments = new Bundle();
            arguments.putParcelable(BookDetailFragment.ARG_BOOK_KEY, book);
            BookDetailFragment fragment = new BookDetailFragment();
            fragment.setArguments(arguments);
            // add it to the activity back stack, using a fragment transaction
            BookListActivity.this.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.item_detail_container, fragment)
                    .commit();
        } else {

            Intent intent = new Intent(this, BookDetailActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.putExtra(BookDetailFragment.ARG_ITEM_ID, book.getId());
            this.startActivity(intent);
        }
    }


    private void showBook(String id) {
        mViewModel.findBookById(id)
                .toSingle()
                // NoSuchElementException is emitted when Maybe.empty()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Book>() {
                    @Override
                    public void onSubscribe(Disposable d) { mDisposable.add(d); }

                    @Override
                    public void onSuccess(Book book) {
                        BookListActivity.this.showBook(book);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof NoSuchElementException) {
                            Snackbar.make(mRecyclerView, R.string.message_book_not_found, Snackbar.LENGTH_LONG).show();
                        } else {
                            Log.e(TAG, "Unexpected error", e);
                        }
                    }
                });
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
        private Subscription mBookSubscription;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Book item = (Book) view.getTag();
                mParentActivity.showBook(item.getId());
            }
        };

        SimpleItemRecyclerViewAdapter(BookListActivity parent, boolean twoPane, Flowable<List<Book>> dataSource) {
            mValues = new ArrayList<Book>();
            mParentActivity = parent;
            mTwoPane = twoPane;
            dataSource
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(s -> {
                        mBookSubscription = s;
                        mBookSubscription.request(1);
                    })
                    .doOnNext(this::setItems)
                    .subscribe();
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

        public void destroy() {
            mBookSubscription.cancel();
        }
    }
}
