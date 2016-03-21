package com.bignerdranch.android.photogallery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Hides foreground notifications
 *
 * Created by Rudolf on 3/20/2016.
 */
public abstract class VisibleFragment extends Fragment {

    private static final String TAG = "VisibleFragment";

    /**
     * Dynamic Broadcast Receiver
     *
     * Receives broadcast intent from PollService
     */
    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // If received, foreground activity is visible,
            // so cancel the notification
            Log.i(TAG, "Cancelling notification");

            setResultCode(Activity.RESULT_CANCELED);
        }
    };

    @Override
    public void onStart() {
        super.onStart();

        // Register dynamic broadcast receiver
        IntentFilter intentFilter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(mOnShowNotification, intentFilter,
                PollService.PERMISSION_PRIVATE, null);
    }

    @Override
    public void onStop() {
        super.onStop();

        // Unregister dynamic broadcast receiver
        getActivity().unregisterReceiver(mOnShowNotification);
    }
}
