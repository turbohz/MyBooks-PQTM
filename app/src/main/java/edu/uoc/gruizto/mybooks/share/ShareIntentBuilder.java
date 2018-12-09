package edu.uoc.gruizto.mybooks.share;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import androidx.core.content.FileProvider;

public class ShareIntentBuilder extends ContextWrapper {

    private Intent mIntent;

    public ShareIntentBuilder(Context context) {
        super(context);
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

        File file = copyAssetToInternalStorage(resourceId);
        Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), "edu.uoc.gruizto.mybooks.fileprovider", file);
        mIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        mIntent.setType("image/png");
        mIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return this;
    }

    public Intent build() {
        return mIntent;
    }

    private File copyAssetToInternalStorage (int resourceId) {

        InputStream is = getResources().openRawResource(resourceId);
        BufferedInputStream bis = new BufferedInputStream(is);
        File file = null;

        try {

            // creates "share" folder if it does not exist

            File shareFolder = new File(getApplicationContext().getFilesDir(), "share");
            shareFolder.mkdir();

            // stream data to icon.png file

            file = new File(shareFolder, "icon.png");
            OutputStream os =  new FileOutputStream(file.toString());
            BufferedOutputStream bos = new BufferedOutputStream(os);

            int b;

            while (true) {
                b = bis.read();
                if (b>=0) {
                    bos.write(b);
                } else {
                    bis.close();
                    bos.flush();
                    bos.close();
                    break;
                }
            };

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }
}
