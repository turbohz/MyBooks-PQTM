package edu.uoc.gruizto.mybooks.messaging;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

import java.util.ArrayList;

import edu.uoc.gruizto.mybooks.R;

public class ChannelBuilder extends ContextWrapper {

    public ChannelBuilder(Context context) {
        super(context);
    }

    /**
     * Creates a notification channel, for devices on Oreo or newer
     * NOTICE that we must set the sound here as well
     * (customizing it in the notification doesn't work)
     */
    public void build() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel;

            // Check if the channel already exists

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            String channelId = getString(R.string.default_notification_channel_id);
            CharSequence name = getString(R.string.default_notification_channel_name);
            String description = getString(R.string.default_notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);

            // add other customizations

            AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();
            channel.setSound(Uri.parse("android.resource://edu.uoc.gruizto.mybooks/"+R.raw.notification), attributes);
            channel.enableLights(true);
            channel.setLightColor(Color.parseColor("#5B3C88"));
            channel.enableVibration(true);
            channel.setVibrationPattern( new long[]{ 100, 200, 300, 400, 500, 400, 300, 200, 400 });

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this

            notificationManager.createNotificationChannel(channel);
        }
    }
}
