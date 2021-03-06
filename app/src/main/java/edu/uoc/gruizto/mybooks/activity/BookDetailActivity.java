package edu.uoc.gruizto.mybooks.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.NavUtils;
import androidx.webkit.WebViewClientCompat;

import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;

import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProviders;
import edu.uoc.gruizto.mybooks.R;
import edu.uoc.gruizto.mybooks.db.Book;
import edu.uoc.gruizto.mybooks.form.PurchaseRequest;
import edu.uoc.gruizto.mybooks.fragment.BookDetailFragment;
import edu.uoc.gruizto.mybooks.model.AppViewModel;
import edu.uoc.gruizto.mybooks.storage.StorageHelper;
import io.reactivex.MaybeObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * An activity representing a single Item detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link BookListActivity}.
 */
public class BookDetailActivity extends AppCompatActivity {

    private static final String TAG = BookListActivity.class.getName();

    private AppViewModel mViewModel;
    private CompositeDisposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mViewModel = ViewModelProviders.of(this).get(AppViewModel.class);

        // Disposable is needed to clean up the Rx entities
        // used in the asynchronous refresh of the view model

        mDisposable = new CompositeDisposable();

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {

            String bookId = getIntent().getStringExtra(BookDetailFragment.ARG_ITEM_ID);

            mViewModel.findBookById(bookId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new MaybeObserver<Book>() {
                        @Override
                        public void onSubscribe(Disposable d) { mDisposable.add(d); }

                        @Override
                        public void onSuccess(Book book) {
                            // create fragment state bundle
                            Bundle arguments = new Bundle();
                            arguments.putParcelable(BookDetailFragment.ARG_BOOK_KEY, book);
                            // create the detail fragment, and provide it with the Bundle
                            BookDetailFragment fragment = new BookDetailFragment();
                            fragment.setArguments(arguments);
                            // add it to the activity back stack, using a fragment transaction
                            BookDetailActivity.this.getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.item_detail_container, fragment)
                                    .commit();
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }

        // setup web view

        final WebView webView = (WebView) findViewById(R.id.web_view);
        final FloatingActionButton fab = findViewById(R.id.fab_buy);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fab.hide();

                // copy form and allow to access if via FileProvider

                StorageHelper storageHelper = new StorageHelper(BookDetailActivity.this);
                File form = storageHelper.copyAssetToInternalStorage(R.raw.form, "web", "form.html");
                Uri formUri = FileProvider.getUriForFile(BookDetailActivity.this, "edu.uoc.gruizto.mybooks.fileprovider", form);

                webView.loadUrl(formUri.toString());
                webView.setVisibility(View.VISIBLE);

                webView.setWebViewClient(new WebViewClientCompat(){
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {

                        final PurchaseRequest request = new PurchaseRequest(url);
                        boolean success = request.isValid();

                        if (success) {

                            // restore fab and hide webview

                            view.setVisibility(View.INVISIBLE);
                            fab.show();

                        } else {

                            // reload to retry

                            view.reload();
                        }

                        String message = (String) getResources().getText(success ? R.string.purchase_success: R.string.purchase_failure);
                        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();

                        return true;
                    }
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, BookListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }
}
