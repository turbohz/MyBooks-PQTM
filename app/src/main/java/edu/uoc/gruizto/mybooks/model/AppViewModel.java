package edu.uoc.gruizto.mybooks.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.ListIterator;

import edu.uoc.gruizto.mybooks.db.Book;
import edu.uoc.gruizto.mybooks.db.BookRepository;

public class AppViewModel extends AndroidViewModel {

    private static final String TAG = AppViewModel.class.getName();
    private BookRepository mBookRepository;
    private List<Book> mAllBooks;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDB;
    // FIXME: this should not be hardcoded, and secrets should not be stored in the app
    private static final String USER_EMAIL = "gruizto@uoc.edu";
    private static final String USER_PASSWORD = "QhW6Yk97sjvNr";

    public AppViewModel (Application application) {
        super(application);

        // Prepare model

        mBookRepository = new BookRepository(application);

        // Connect to Firebase database

        mAuth = FirebaseAuth.getInstance();
        mDB = FirebaseDatabase.getInstance();

        mDB
            .getReference()
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<Book> books = dataSnapshot.child("books").getValue(new GenericTypeIndicator<List<Book>>() {});
                    ListIterator<Book> i = books.listIterator();
                    Book book;
                    //The iterator.nextIndex() will return the index for you.
                    while(i.hasNext()){
                        String id = String.valueOf(i.nextIndex());
                        book = i.next();
                        book.id = id;
                        if (!exists(book)) {
                            // perhaps we should overwrite existing books,
                            // if we consider the remote data as our truth
                            Log.i(AppViewModel.TAG, "Inserting "+book.title);
                            insertBook(book);
                        } else {
                            Log.i(AppViewModel.TAG, "Book already exist "+book.title);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(AppViewModel.TAG, "ValueEventListener:" + databaseError);
                    // TODO: Should we show some error to the user?
                }
            });

        mAuth
            .signInWithEmailAndPassword(USER_EMAIL, USER_PASSWORD)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Log.d(AppViewModel.TAG, "signInWithEmail:success");
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(AppViewModel.TAG, "signInWithEmail:failure", task.getException());
                    }
                }
            });
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
