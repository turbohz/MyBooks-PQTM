package edu.uoc.gruizto.mybooks.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mybooks")
data class Book (
    @PrimaryKey
    var id: String,
    var title: String? = null,
    var author: String? = null,
    var publicationDate: String? = null,
    var description: String? = null,
    var coverUrl: String? = null
) {
    constructor():this("")
}