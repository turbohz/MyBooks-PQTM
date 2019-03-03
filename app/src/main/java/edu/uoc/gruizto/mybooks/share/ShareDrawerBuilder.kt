package edu.uoc.gruizto.mybooks.share

import androidx.annotation.StringRes
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem

class ShareDrawerBuilder {

    companion object {
        fun createItem (id:Long, @StringRes label:Int) :PrimaryDrawerItem =
            PrimaryDrawerItem().withIdentifier(id).withName(label).withSelectable(false)
    }
}