package edu.uoc.gruizto.mybooks.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;


import java.util.List;
import java.util.ListIterator;

import edu.uoc.gruizto.mybooks.db.Book;
import edu.uoc.gruizto.mybooks.db.BookRepository;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;

public class AppViewModel extends AndroidViewModel {

    private static final String TAG = AppViewModel.class.getName();
    private BookRepository mBookRepository;
    // FIXME: this should not be hardcoded, and secrets should not be stored in the app
    private static final String USER_EMAIL = "gruizto@uoc.edu";
    private static final String USER_PASSWORD = "QhW6Yk97sjvNr";

    public AppViewModel (Application application) {
        super(application);

        // Prepare model

        mBookRepository = new BookRepository(application);
    }

    public List<Book> getBooks() { return mBookRepository.getAll(); }

    public boolean exists(Book book) {
        return (null != findBookById(book.id));
    }
    public void insertBook(Book book) { mBookRepository.insert(book); }


    public Book findBookById(String id) {
        return mBookRepository.findById(id);
    }

    public void deleteAllBooks() { mBookRepository.deleteAll(); }
}

    /**
     * Try to sign in to Firebase with an email and password.
     *
     * If the user is already signed in, returns the User straight away.
     *
     * @return Single<FirebaseUser> an RxJava Single value providing a User, or an Exception
     */
    private Single<FirebaseUser> signIn() {

        final FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (null != user) {
            // already signed in
            Log.i(AppViewModel.TAG, "Already signed in");
            return Single.just(user);

        } else {
            // signIn with email and password
            return Single.create(new SingleOnSubscribe<FirebaseUser>() {
                @Override
                public void subscribe(final SingleEmitter<FirebaseUser> emitter) {

                    auth
                        .signInWithEmailAndPassword(USER_EMAIL, USER_PASSWORD)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.d(AppViewModel.TAG, "signInWithEmail:success");
                                    FirebaseUser user = auth.getCurrentUser();
                                    emitter.onSuccess(user);
                                } else {
                                    // If sign in fails, pass an error to be shown
                                    Log.w(AppViewModel.TAG, "signInWithEmail:failure", task.getException());
                                    emitter.onError(new Exception("Unable to authenticate with Firebase"));
                                }
                            }
                        });
                }
            });
        }
    }

    /**
     * Refresh the book data from the server.
     *
     * As specified in the exercise instructions,
     * in case an error happens, we return the last cached data.
     *
     * Notice we first try to sign in, if we are not authenticated.
     *
     * @return Single<List<Book>>
     */
    public Single<List<Book>> refresh() {

        final Single<List<Book>> fetchBooks = Single.create(new SingleOnSubscribe<List<Book>>() {
            @Override
            public void subscribe(final SingleEmitter<List<Book>> emitter) {

                // try to fetch data, and feed the observable according to the result
                FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Log.w(AppViewModel.TAG, "onDataChange");
                            List<Book> books = dataSnapshot.child("books").getValue(new GenericTypeIndicator<List<Book>>() {
                            });
                            ListIterator<Book> i = books.listIterator();
                            Book book;
                            //The iterator.nextIndex() will return the index for you.
                            while (i.hasNext()) {
                                String id = String.valueOf(i.nextIndex());
                                book = i.next();
                                book.id = id;
                                if (null == findBookById(book.id)) {
                                    Log.i(AppViewModel.TAG, "Inserting " + book.title);
                                    insertBook(book);
                                } else {
                                    Log.i(AppViewModel.TAG, "Book already exist " + book.title);
                                }
                            }

                            emitter.onSuccess(getBooks());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.i(AppViewModel.TAG, "Firebase error " + databaseError.getMessage());
                            // as is required in the exercise instructions,
                            // instead of an error, we return the last cached data
                            emitter.onSuccess(getBooks());
                        }
                    });
            }
        });

        return signIn()
            .flatMap(new Function<FirebaseUser, SingleSource<? extends List<Book>>>() {
                @Override
                public SingleSource<? extends List<Book>> apply(FirebaseUser firebaseUser) {
                    return fetchBooks;
                }
            });
    }
}