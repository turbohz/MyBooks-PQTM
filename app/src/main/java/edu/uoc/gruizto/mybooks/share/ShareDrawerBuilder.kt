package edu.uoc.gruizto.mybooks.share

import androidx.annotation.StringRes
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import edu.uoc.gruizto.mybooks.R

class ShareDrawerBuilder {

    companion object {

        fun createItem (id:Long, @StringRes label:Int) :PrimaryDrawerItem =
            PrimaryDrawerItem()
                    .withIdentifier(id)
                    .withName(label)
                    .withSelectable(false)

        private const val PROFILE_NAME = "Gerard Ruiz"
        private const val PROFILE_EMAIL = "gruizto@uoc.edu"

        val profile:ProfileDrawerItem =
            ProfileDrawerItem()
                    .withName(PROFILE_NAME)
                    .withEmail(PROFILE_EMAIL)
                    .withIcon(R.drawable.portrait)
    }
}