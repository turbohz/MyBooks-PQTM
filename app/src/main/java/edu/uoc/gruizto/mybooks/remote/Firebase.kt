package edu.uoc.gruizto.mybooks.remote

import android.util.Log
import androidx.annotation.NonNull
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import edu.uoc.gruizto.mybooks.db.Book
import io.reactivex.Single
import io.reactivex.SingleEmitter

class Firebase {

    /**
     * Try to sign in to Firebase with an email and password.
     *
     * If the user is already signed in, returns the User straight away.
     *
     * @return a single of user (or error)
     */
    private fun signIn(): Single<FirebaseUser> {

        val auth = FirebaseAuth.getInstance()

        if (null != auth.currentUser) {
            // trivial case: already signed in
            Log.i(Firebase.TAG, "Already signed in")
            return Single.just(auth.currentUser)

        } else {
            // signIn with email and password
            return Single.create { emitter ->
                auth
                        .signInAnonymously()
                        .addOnCompleteListener {
                            val user = auth.currentUser
                            if (it.isSuccessful && null != user) {
                                Log.d(Firebase.TAG, "signInAnonymously:success")
                                emitter.onSuccess(user)
                            } else {
                                // If sign in fails, pass an error to be shown
                                Log.w(Firebase.TAG, "signInWithEmail:failure", it.exception)
                                emitter.onError(Exception("Unable to authenticate with Firebase"))
                            }
                        }
            }
        }
    }

    /**
     * Fetch Firebase book data.
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
     * @return a future list of books
     **/
    fun fetch(): Single<List<Book>> {

        val fetch = Single.create { emitter: SingleEmitter<List<Book>> ->
            FirebaseDatabase.getInstance()
                    .reference
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                            Log.w(Firebase.TAG, "onDataChange")
                            var books: List<Book>? = dataSnapshot.child("books").getValue(object : GenericTypeIndicator<ArrayList<Book>>() {})

                            if (null == books) {
                                // as is required in the exercise instructions,
                                // instead of an error, we return the last cached data
                                emitter.onSuccess(ArrayList())
                            } else {
                                // fill in book ids

                                books = books.mapIndexed { index, book ->
                                    book.id = (index + 1).toString()
                                    book
                                }

                                emitter.onSuccess(books)
                            }
                        }

                        override fun onCancelled(@NonNull databaseError: DatabaseError) {
                            Log.i(Firebase.TAG, "Firebase error " + databaseError.message)
                            // as is required in the exercise instructions,
                            // instead of an error, we return the last cached data
                            emitter.onSuccess(ArrayList())
                        }
                    })

        };

        return signIn().flatMap { fetch }
    }

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
                                Log.w(Firebase.TAG, "Unable to obtain firebaseInstanceId", task.exception)
                                it.onError(Exception("Unexpected error!"))
                            }
                        }
            }
        }

    companion object {

        private val TAG = Firebase::class.java.name

        private val instance = Firebase()
        fun fetch() = instance.fetch()
        fun getFirebaseInstanceId() = instance.firebaseInstanceId
    }
}