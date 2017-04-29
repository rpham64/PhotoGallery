package com.rpham64.android.photogallery.services;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.rpham64.android.photogallery.R;
import com.rpham64.android.photogallery.ui.gallery.PhotoGalleryActivity;
import com.rpham64.android.photogallery.utils.QueryPreferences;

/**
 * Polls for search results in background
 *
 * Created by Rudolf on 3/19/2016.
 */
public class PollService extends IntentService {

    private static final String TAG = "PollService";

    private static final String EXTRA_PAGE =
            "com.bignerdranch.android.photogallery.lastpagedfetched";

    private static final long POLL_INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;    // 15 minutes

    public static final String ACTION_SHOW_NOTIFICATION =
            "com.bignerdranch.android.photogallery.SHOW_NOTIFICATION";

    public static final String PERMISSION_PRIVATE = "com.rpham64.android.photogallery.PRIVATE";

    // Ordered Broadcast Intent strings
    public static final String REQUEST_CODE = "REQUEST_CODE";
    public static final String NOTIFICATION = "NOTIFICATION";

    public PollService() {
        super(TAG);
    }

    public static Intent newIntent(Context context) {
        return newIntent(context, 0);
    }

    public static Intent newIntent(Context context, int lastPageFetched) {

        Intent intent = new Intent(context, PollService.class);
        intent.putExtra(EXTRA_PAGE, lastPageFetched);

        return intent;
    }

    /**
     * Turns alarm on or off
     *
     * @param context
     * @param turnOn
     */

    public static void setServiceAlarm(Context context, boolean turnOn) {
        setServiceAlarm(context, 1, turnOn);
    }

    public static void setServiceAlarm(Context context, int lastPageFetched, boolean turnOn) {

        // Construct PollIntent to start PollService
        Intent intent = PollService.newIntent(context, lastPageFetched);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);

        // Set alarm or cancel it
        if (turnOn) {
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(), POLL_INTERVAL, pendingIntent);

            Toast.makeText(context, "Polling Service ON. New results will be retrieved every 15 minutes.", Toast.LENGTH_LONG).show();

        } else {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();

            Toast.makeText(context, "Polling service OFF", Toast.LENGTH_SHORT).show();
        }

        // Write to QueryPreferences when alarm is set
        QueryPreferences.setAlarmOn(context, turnOn);

    }

    /**
     * Checks if alarm is on or off
     *
     * @param context
     * @return
     */
    public static boolean isServiceAlarmOn(Context context) {

        Intent intent = PollService.newIntent(context);
        PendingIntent pendingIntent = PendingIntent
                .getService(context, 0, intent, PendingIntent.FLAG_NO_CREATE);

        return pendingIntent != null;
    }

    /**
     * Handles intent sent by AlarmManager in a time interval
     *
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        // Check: Network available and connected
        if (!isNetworkAvailableAndConnected()) return;

        // Add a Notification
        Resources resources = getResources();
        Intent i = PhotoGalleryActivity.newIntent(this);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setTicker(resources.getString(R.string.new_pictures_title))
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(resources.getString(R.string.new_pictures_title))
                .setContentText(resources.getString(R.string.new_pictures_text))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        // Display Notification
        showBackgroundNotification(0, notification);
    }

    /**
     * Sends out ordered broadcast intent to display notification
     *
     * @param requestCode
     * @param notification
     */
    private void showBackgroundNotification(int requestCode, Notification notification) {

        Intent intent = new Intent(ACTION_SHOW_NOTIFICATION);
        intent.putExtra(REQUEST_CODE, requestCode);
        intent.putExtra(NOTIFICATION, notification);

        // Sends out ordered broadcast to all broadcast receivers
        // (VisibleFragment -> PhotoGalleryActivity (ONLY if visible), NotificationReceiver)
        sendOrderedBroadcast(intent, PERMISSION_PRIVATE, null, null,
                Activity.RESULT_OK, null, null);
    }

    /**
     * Checks if com.rpham64.android.photogallery.network is available for background applications
     *
     * @return
     */
    private boolean isNetworkAvailableAndConnected() {

        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        boolean isNetworkAvailable = connectivityManager.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable &&
                connectivityManager.getActiveNetworkInfo().isConnected();

        return isNetworkConnected;
    }
}
