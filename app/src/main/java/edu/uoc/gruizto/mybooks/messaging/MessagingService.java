package edu.uoc.gruizto.mybooks.messaging;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import edu.uoc.gruizto.mybooks.R;
import edu.uoc.gruizto.mybooks.activity.BookListActivity;
import edu.uoc.gruizto.mybooks.fragment.BookDetailFragment;

public class MessagingService extends FirebaseMessagingService {

    public static final String BOOK_ID_KEY = "book_position";

    @Override
    public void onCreate() {
        new ChannelBuilder(this).build();
    }

    private static final String TAG = MessagingService.class.getName();

    /**
     * Called when a remote message is received
     *
     * @param remoteMessage message received
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        String bookPosition = null;

        // Check if message contains a data payload

        if (remoteMessage.getData().size() > 0) {
            try {
                bookPosition = remoteMessage.getData().get(BOOK_ID_KEY);
            } catch (Exception exception) {
                Log.e(TAG, exception.getMessage());
            }
        }

        // make bookPosition always a string

        bookPosition = (null == bookPosition) ? "":bookPosition;

        // Check if message contains a notification payload

        String message = "Message from Firebase";

        if (remoteMessage.getNotification() != null) {
            message = remoteMessage.getNotification().getBody();
        }

        displayNotification(message, bookPosition);
    }

    @Override
    public void onNewToken(String token) {

        Log.i(TAG, "new token generated:"+token);
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     * @param bookPosition Book position in the received message
     */
    private void displayNotification(String messageBody, String bookPosition) {

        // prepare actions
        // they will work on the running activity if it is on top

        Intent viewBookDetailsIntent = new Intent(this, BookListActivity.class);
        viewBookDetailsIntent.setAction(Intent.ACTION_VIEW);
        viewBookDetailsIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        viewBookDetailsIntent.putExtra(BookDetailFragment.ARG_ITEM_ID, bookPosition);
        PendingIntent viewBookDetailsPendingIntent = PendingIntent.getActivity(this, 0, viewBookDetailsIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        String viewBookActionTitle = getString(R.string.default_notification_view_action);
        NotificationCompat.Action viewBookDetailsAction = new NotificationCompat.Action(0, viewBookActionTitle, viewBookDetailsPendingIntent);

        Intent deleteBookIntent = new Intent(this, BookListActivity.class);
        PendingIntent deleteBookPendingIntent = PendingIntent.getActivity(this, 0, deleteBookIntent, 0);
        String deleteBookActionTitle = getString(R.string.default_notification_delete_action);
        NotificationCompat.Action deleteBookDetailsAction = new NotificationCompat.Action(0, deleteBookActionTitle, deleteBookPendingIntent);

        // compose notification

        String channelId = getString(R.string.default_notification_channel_id);
        NotificationCompat.Builder notificationBuilder =
            new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Incoming notification")
                .setContentText(messageBody)
                .addAction(viewBookDetailsAction)
                .addAction(deleteBookDetailsAction)
                ;

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
