package com.rpham64.android.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Standalone Broadcast Receiver
 *
 * Turns PollService alarm on at system boot
 *
 * Created by Rudolf on 3/20/2016.
 */
public class StartupReceiver extends BroadcastReceiver {

    private static final String TAG = "StartupReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received broadcast intent: " + intent.getAction());

        boolean turnOn = QueryPreferences.isAlarmOn(context);
        PollService.setServiceAlarm(context, turnOn);

    }

}
