package edu.uoc.gruizto.mybooks.messaging;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import edu.uoc.gruizto.mybooks.R;
import edu.uoc.gruizto.mybooks.activity.BookListActivity;

public class MessagingService extends FirebaseMessagingService {

    private static final String TAG = MessagingService.class.getName();

    /**
     * Called when a remote message is received
     *
     * @param remoteMessage message received
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // Check if message contains a data payload

        if (remoteMessage.getData().size() > 0) {
            Log.i(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload

        if (remoteMessage.getNotification() != null) {
            String message = remoteMessage.getNotification().getBody();
            Log.i(TAG, "Message Notification Body: " + message);
            sendNotification(message);
        }
    }

    @Override
    public void onNewToken(String token) {

        Log.i(TAG, "new token generated:"+token);
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {

        String channelId = getString(R.string.default_notification_channel_id);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("Incoming notification")
                        .setContentText(messageBody);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
