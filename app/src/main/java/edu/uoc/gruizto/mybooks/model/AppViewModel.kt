package edu.uoc.gruizto.mybooks.model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.iid.FirebaseInstanceId
import edu.uoc.gruizto.mybooks.db.Book
import edu.uoc.gruizto.mybooks.db.BookRepository
import edu.uoc.gruizto.mybooks.remote.Firebase
import io.reactivex.Completable
import io.reactivex.CompletableOnSubscribe
import io.reactivex.Single

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val mBookRepository: BookRepository = BookRepository(application)

    val books: List<Book>
        get() = mBookRepository.all

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

    /**
     * Synchronizes Firebase and Room data.
     *
     * Synchronization always happens in one direction:
     *
     * FROM remote TO local
     *
     * As specified in the exercise instructions,
     * local data acts as a kind of cache, so we can ignore
     * errors and use local data to display it.
     *
     * Notice we first try to sign in, if we are not authenticated.
     *
     * @return a Completable instance alerting when the sync is completed
     **/
    fun sync(): Completable {

        val sync = { books: List<Book> ->
            Completable.create { emitter ->
                val db = mBookRepository.db

                db.beginTransaction()

                try {

                    for (book: Book in books) {
                        insertBook(book)
                    }

                    db.setTransactionSuccessful()

                } catch (e: java.lang.Exception) {
                    Log.e(AppViewModel.TAG, "Exception when inserting book:" + e.message)
                } finally {
                    db.endTransaction()
                }

                emitter.onComplete()
            }
        }

        return Firebase.fetch().flatMapCompletable(sync)
    }

    companion object {

        private val TAG = AppViewModel::class.java.name
    }
}