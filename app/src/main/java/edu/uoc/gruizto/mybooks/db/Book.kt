package edu.uoc.gruizto.mybooks.db

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mybooks")
class Book() : Parcelable {

    @PrimaryKey
    var id: String = ""
    var title: String? = null
    var author: String? = null
    var publicationDate: String? = null
    var description: String? = null
    var coverUrl: String? = null

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        title = parcel.readString()
        author = parcel.readString()
        publicationDate = parcel.readString()
        description = parcel.readString()
        coverUrl = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(author)
        parcel.writeString(publicationDate)
        parcel.writeString(description)
        parcel.writeString(coverUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Book> {
        override fun createFromParcel(parcel: Parcel): Book {
            return Book(parcel)
        }

        override fun newArray(size: Int): Array<Book?> {
            return arrayOfNulls(size)
        }
    }
}