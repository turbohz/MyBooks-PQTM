package edu.uoc.gruizto.mybooks.share;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import java.io.File;

import androidx.core.content.FileProvider;
import edu.uoc.gruizto.mybooks.storage.StorageHelper;

public class ShareIntentBuilder extends ContextWrapper {

    private Intent mIntent;
    private StorageHelper mStorageHelper;

    public ShareIntentBuilder(Context context) {
        super(context);

        mStorageHelper = new StorageHelper(context);

        mIntent = new Intent();
        mIntent.setAction(Intent.ACTION_SEND);
    }

    public ShareIntentBuilder setText(String text) {

        mIntent.putExtra(Intent.EXTRA_TEXT, text);
        mIntent.setType("text/plain");

        return this;
    }

    // this can be used to specify the exact
    // app we're sharing to (like whatsapp)
    public ShareIntentBuilder setPackage(String name)
    {
        mIntent.setPackage(name);
        return this;
    }

    public ShareIntentBuilder setImage(int resourceId) {

        File file = mStorageHelper.copyAssetToInternalStorage(resourceId, "share", "icon.png");
        Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), "edu.uoc.gruizto.mybooks.fileprovider", file);
        mIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        mIntent.setType("image/png");
        mIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return this;
    }

    public Intent build() {
        return mIntent;
    }
}
