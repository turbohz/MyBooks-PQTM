package edu.uoc.gruizto.mybooks.share

import android.app.Activity
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.StringRes
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import com.squareup.picasso.Picasso

private const val PROFILE_PICTURE_URL = "https://lh3.googleusercontent.com/-xNpsxtjuGhw/W64vqH_Zk3I/AAAAAAAAACk/4cS2_mewPEcE0B1_894bisk65mLnyqFlQCEwYBhgL/portrait-face2.png"
private const val PROFILE_NAME = "Gerard Ruiz"
private const val PROFILE_EMAIL = "gruizto@uoc.edu"

class ShareDrawerBuilder(activity: Activity) {

    val builder: DrawerBuilder = DrawerBuilder().withActivity(activity)

    // initialise DrawerImageLoader to use Picasso
    // it is used to download the Profile photo

    init {
        DrawerImageLoader.init(object : AbstractDrawerImageLoader() {
            override fun set(imageView: ImageView?, uri: Uri?, placeholder: Drawable?) {
                Picasso.get().load(uri).placeholder(placeholder!!).into(imageView)
            }

            override fun cancel(imageView: ImageView?) {
                Picasso.get().cancelRequest(imageView!!)
            }
        })
    }

    // Create the AccountHeader

    init {

        val profile: ProfileDrawerItem = ProfileDrawerItem()
                .withName(PROFILE_NAME)
                .withEmail(PROFILE_EMAIL)
                .withIcon(PROFILE_PICTURE_URL)

        val header: AccountHeader = AccountHeaderBuilder()
                .withActivity(activity)
                .addProfiles(profile)
                .build()

        builder.withAccountHeader(header)
    }


    fun build():Drawer {
        return builder.build()
    }

    companion object {

        fun createItem (id:Long, @StringRes label:Int) :PrimaryDrawerItem =
            PrimaryDrawerItem()
                    .withIdentifier(id)
                    .withName(label)
                    .withSelectable(false)




    }
}