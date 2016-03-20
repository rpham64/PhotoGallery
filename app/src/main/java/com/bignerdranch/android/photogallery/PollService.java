package com.bignerdranch.android.photogallery;

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
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.List;

/**
 * Polls for search results in background
 *
 * Created by Rudolf on 3/19/2016.
 */
public class PollService extends IntentService {

    private static final String TAG = "PollService";

    private static final String EXTRA_PAGE = "com.bignerdranch.android.photogallery.lastpagedfetched";

    private static final long POLL_INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public PollService() {
        super(TAG);
    }

    public static Intent newIntent(Context context) {
        return newIntent(context, 1);
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
     * @param isOn
     */
    public static void setServiceAlarm(Context context, boolean isOn) {
        setServiceAlarm(context, 1, isOn);
    }

    public static void setServiceAlarm(Context context, int lastPageFetched, boolean isOn) {

        // Construct PollIntent to start PollService
        Intent intent = PollService.newIntent(context, lastPageFetched);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);

        // Set alarm or cancel it
        if (isOn) {
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(), POLL_INTERVAL, pendingIntent);
        } else {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }

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

    @Override
    protected void onHandleIntent(Intent intent) {

        // Check: Network available and connected
        if (!isNetworkAvailableAndConnected()) return;

        // 1) Pull out current query and last result ID from QueryPreferences
        String query = QueryPreferences.getStoredQuery(this);
        String lastResultId = QueryPreferences.getLastResultId(this);
        int lastPageFetched = intent.getIntExtra(EXTRA_PAGE, 1);
        List<GalleryItem> items;

        // 2) Fetch latest result set with FlickrFetchr
        if (query == null) {
            items = new FlickrFetchr().fetchRecentPhotos(lastPageFetched);
        } else {
            items = new FlickrFetchr().searchPhotos(query, lastPageFetched);
        }

        // 3) If there are results, grab the first one
        if (items.size() == 0) return;

        String resultId = items.get(0).getId();

        // 4) Check: resultId == lastResultId
        if (resultId.equals(lastResultId)) {

            Log.i(TAG, "Got an old result: " + resultId);

        } else {

            Log.i(TAG, "Got a new result: " + resultId);

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

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(0, notification);

        }

        // 5) Store first result back into QueryPreferences
        QueryPreferences.setLastResultId(this, resultId);

    }

    /**
     * Checks if network is available for background applications
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
