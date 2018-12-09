package edu.uoc.gruizto.mybooks.storage;

import android.content.Context;
import android.content.ContextWrapper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StorageHelper extends ContextWrapper {

    public StorageHelper(Context context) {
        super(context);
    }

    public File copyAssetToInternalStorage (int resId, String dir, String filename) {

        InputStream is = getResources().openRawResource(resId);
        BufferedInputStream bis = new BufferedInputStream(is);
        File file = null;

        try {

            // creates directory if it does not exist

            File shareFolder = new File(getApplicationContext().getFilesDir(), dir);
            shareFolder.mkdir();

            // stream data to icon.png file

            file = new File(shareFolder, filename);
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
