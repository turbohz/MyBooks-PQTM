package edu.uoc.gruizto.mybooks.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.annotation.NonNull
import android.util.Log

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId

import edu.uoc.gruizto.mybooks.db.Book
import edu.uoc.gruizto.mybooks.db.BookRepository
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val mBookRepository: BookRepository = BookRepository(application)

    val books: List<Book>
        get() = mBookRepository.all

    fun exists(book: Book): Boolean {
        return null != findBookById(book.id)
    }

    fun insertBook(book: Book) {
        mBookRepository.insert(book)
    }

    fun findBookById(id: String): Book? {
        return mBookRepository.findById(id)
    }

    fun deleteAllBooks() {
        mBookRepository.deleteAll()
    }

    fun deleteBook(book: Book) {
        mBookRepository.delete(book)
    }

    // Get Instance ID token
    val firebaseInstanceId: Single<String>
        get() {
            return Single.create {
                FirebaseInstanceId.getInstance()
                    .instanceId
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful && null != task.result) {
                            val token = task.result!!.token
                            it.onSuccess(token)
                        } else {
                            Log.w(AppViewModel.TAG, "Unable to obtain firebaseInstanceId", task.exception)
                            it.onError(Exception("Unexpected error!"))
                        }
                    }
            }
        }

    /**
     * Try to sign in to Firebase with an email and password.
     *
     * If the user is already signed in, returns the User straight away.
     *
     * @return Single<FirebaseUser> an RxJava Single value providing a User, or an Exception
     */
    private fun signIn(): Single<FirebaseUser?> {

        val auth = FirebaseAuth.getInstance()

        if (null != auth.currentUser) {
            // trivial case: already signed in
            Log.i(AppViewModel.TAG, "Already signed in")
            return Single.just(auth.currentUser)

        } else {
            // signIn with email and password
            return Single.create { emitter ->
                auth
                    .signInWithEmailAndPassword(USER_EMAIL, USER_PASSWORD)
                    .addOnCompleteListener {
                        val user = auth.currentUser
                        if (it.isSuccessful && null != user) {
                            Log.d(AppViewModel.TAG, "signInWithEmail:success")
                            emitter.onSuccess(user)
                        } else {
                            // If sign in fails, pass an error to be shown
                            Log.w(AppViewModel.TAG, "signInWithEmail:failure", it.exception)
                            emitter.onError(Exception("Unable to authenticate with Firebase"))
                        }
                    }
            }
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
     * @return Single<List></List><Book>>
    </Book> */
    fun refresh(): Single<List<Book>> {

        val fetchBooks = Single.create(SingleOnSubscribe<List<Book>> { emitter ->

            // try to fetch data, and feed the observable according to the result
            FirebaseDatabase.getInstance()
                .reference
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                        Log.w(AppViewModel.TAG, "onDataChange")
                        val books = dataSnapshot.child("books").getValue(object : GenericTypeIndicator<ArrayList<Book>>() {})

                        if (null == books) {
                            // as is required in the exercise instructions,
                            // instead of an error, we return the last cached data
                            emitter.onSuccess(this@AppViewModel.books)
                            return
                        }

                        val i = books.listIterator()
                        var book: Book
                        //The iterator.nextIndex() will return the index for you.
                        while (i.hasNext()) {
                            val id = i.nextIndex().toString()
                            book = i.next()
                            book.id = id
                            if (exists(book)) {
                                Log.i(AppViewModel.TAG, "Book already exist " + book.title!!)
                            } else {
                                Log.i(AppViewModel.TAG, "Inserting " + book.title!!)
                                insertBook(book)
                            }
                        }

                        // we can now return the updated book list

                        emitter.onSuccess(this@AppViewModel.books)
                    }

                    override fun onCancelled(@NonNull databaseError: DatabaseError) {
                        Log.i(AppViewModel.TAG, "Firebase error " + databaseError.message)
                        // as is required in the exercise instructions,
                        // instead of an error, we return the last cached data
                        emitter.onSuccess(this@AppViewModel.books)
                    }
                })
        })

        return signIn().flatMap { fetchBooks }
    }

    companion object {

        private val TAG = AppViewModel::class.java.name
        // FIXME: this should not be hardcoded, and secrets should not be stored in the app
        private val USER_EMAIL = "gruizto@uoc.edu"
        private val USER_PASSWORD = "QhW6Yk97sjvNr"
    }
}