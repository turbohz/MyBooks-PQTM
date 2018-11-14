package edu.uoc.gruizto.mybooks.messaging;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.MessageFormat;

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

        // by using the position as notification id,
        // we'll only get a single notification by book

        int notificationId;

        try {
            notificationId = Integer.parseInt(bookPosition);
        } catch (NumberFormatException e) {
            notificationId = -1; // we will only accept positive values
        }

        if (notificationId < 0) {
            Log.e(TAG, "Invalid book position received:"+bookPosition);
            return;
        }

        // prepare actions
        // they will work on the running activity if it is on top

        // required to generate a unique intent per book id and action !!
        // otherwise, the intent is reused EVEN WITH different extras values
        // See: https://stackoverflow.com/a/7370448/77838
        int requestCode = notificationId;

        Intent viewBookDetailsIntent = new Intent(this, BookListActivity.class);
        viewBookDetailsIntent.setAction(Intent.ACTION_VIEW);
        viewBookDetailsIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        viewBookDetailsIntent.putExtra(BookDetailFragment.ARG_ITEM_ID, bookPosition);
        PendingIntent viewBookDetailsPendingIntent = PendingIntent.getActivity(this, requestCode, viewBookDetailsIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        String viewBookActionTitle = getString(R.string.default_notification_view_action);
        NotificationCompat.Action viewBookDetailsAction = new NotificationCompat.Action(0, viewBookActionTitle, viewBookDetailsPendingIntent);

        Intent deleteBookIntent = new Intent(this, BookListActivity.class);
        deleteBookIntent.setAction(Intent.ACTION_DELETE);
        deleteBookIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        deleteBookIntent.putExtra(BookDetailFragment.ARG_ITEM_ID, bookPosition);
        PendingIntent deleteBookPendingIntent = PendingIntent.getActivity(this, requestCode, deleteBookIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        String deleteBookActionTitle = getString(R.string.default_notification_delete_action);
        NotificationCompat.Action deleteBookDetailsAction = new NotificationCompat.Action(0, deleteBookActionTitle, deleteBookPendingIntent);

        // compose notification

        String channelId = getString(R.string.default_notification_channel_id);
        NotificationCompat.Builder notificationBuilder =
            new NotificationCompat.Builder(this, channelId)
                .setPriority(NotificationManagerCompat.IMPORTANCE_HIGH)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(MessageFormat.format(getString(R.string.default_notification_title), bookPosition))
                .setContentText(messageBody)
                .setLights(getResources().getColor(R.color.notificationLedColor), 1000, 200)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setSound(Uri.parse("android.resource://edu.uoc.gruizto.mybooks/"+R.raw.notification))
                .setVibrate(new long[]{ 100, 200, 300, 400, 500, 400, 300, 200, 400 })
                .addAction(viewBookDetailsAction)
                .addAction(deleteBookDetailsAction)
                ;

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}
