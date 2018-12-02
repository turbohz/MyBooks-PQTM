package edu.uoc.gruizto.mybooks.share;

import android.content.Intent;

public class ShareIntentBuilder {

    private Intent mIntent;

    public ShareIntentBuilder() {

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

    public Intent build() {
        return mIntent;
    }
}
