package edu.uoc.gruizto.mybooks.share

import androidx.annotation.StringRes
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem

class ShareDrawerBuilder {

    companion object {

        fun createItem (id:Long, @StringRes label:Int) :PrimaryDrawerItem =
            PrimaryDrawerItem()
                    .withIdentifier(id)
                    .withName(label)
                    .withSelectable(false)

        private const val PROFILE_PICTURE_URL = "https://lh3.googleusercontent.com/-xNpsxtjuGhw/W64vqH_Zk3I/AAAAAAAAACk/4cS2_mewPEcE0B1_894bisk65mLnyqFlQCEwYBhgL/portrait-face2.png"
        private const val PROFILE_NAME = "Gerard Ruiz"
        private const val PROFILE_EMAIL = "gruizto@uoc.edu"

        val profile:ProfileDrawerItem =
            ProfileDrawerItem()
                    .withName(PROFILE_NAME)
                    .withEmail(PROFILE_EMAIL)
                    .withIcon(PROFILE_PICTURE_URL)
    }
}