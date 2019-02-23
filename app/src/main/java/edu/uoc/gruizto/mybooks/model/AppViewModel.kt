package edu.uoc.gruizto.mybooks.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import edu.uoc.gruizto.mybooks.db.Book
import edu.uoc.gruizto.mybooks.db.BookRepository
import edu.uoc.gruizto.mybooks.remote.Firebase
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val mBookRepository: BookRepository = BookRepository(application)

    val books: Flowable<List<Book>> = mBookRepository.all

    fun insertBook(book: Book): Completable {
        return mBookRepository.insert(book)
    }

    fun findBookById(id: String): Maybe<Book> {
        return mBookRepository.findById(id)
    }

    fun deleteAllBooks(): Completable {
        return mBookRepository.deleteAll()
    }

    fun deleteBook(book: Book): Completable {
        return mBookRepository.delete(book)
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

        return Firebase.fetchBooks()
                .flatMapCompletable { books -> mBookRepository.insertMany(books) }
                .subscribeOn(Schedulers.single())
    }

    companion object {

        private val TAG = AppViewModel::class.java.name
    }
}