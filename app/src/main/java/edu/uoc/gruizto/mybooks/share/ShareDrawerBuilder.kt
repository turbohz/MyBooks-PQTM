package edu.uoc.gruizto.mybooks.share

import android.app.Activity
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
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
import edu.uoc.gruizto.mybooks.R

object ProfileData {
    const val pictureURL = "https://lh3.googleusercontent.com/-xNpsxtjuGhw/W64vqH_Zk3I/AAAAAAAAACk/4cS2_mewPEcE0B1_894bisk65mLnyqFlQCEwYBhgL/portrait-face2.png"
    const val name = "Gerard Ruiz"
    const val email = "gruizto@uoc.edu"
}

fun drawerItem (id:Long, @StringRes label:Int) :PrimaryDrawerItem =
        PrimaryDrawerItem()
                .withIdentifier(id)
                .withName(label)
                .withSelectable(false)

class ShareDrawerBuilder(activity: Activity, toolbar: Toolbar?) {

    private val builder: DrawerBuilder = DrawerBuilder().withActivity(activity)

    constructor(activity: Activity) : this(activity,null)

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

        // Create the AccountHeader

        val profile: ProfileDrawerItem = ProfileDrawerItem()
                .withName(ProfileData.name)
                .withEmail(ProfileData.email)
                .withIcon(ProfileData.pictureURL)

        val header: AccountHeader = AccountHeaderBuilder()
                .withActivity(activity)
                .withSelectionListEnabledForSingleProfile(false)
                .addProfiles(profile)
                .build()

        builder.withAccountHeader(header)

        // initialise Drawer options

        builder.addDrawerItems(
            drawerItem(1, R.string.drawer_item_share_with_app),
            drawerItem(2, R.string.drawer_item_copy_to_clipboard),
            drawerItem(3, R.string.drawer_item_share_to_whatsapp)
        )

        // attach to toolbar if provided

        toolbar?.let { builder.withToolbar(it) }
    }

    fun build():Drawer {
        return builder.build()
    }
}