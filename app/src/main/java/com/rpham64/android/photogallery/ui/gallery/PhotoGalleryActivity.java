package com.rpham64.android.photogallery.ui.gallery;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.rpham64.android.photogallery.utils.SingleFragmentActivity;

/**
 * Main Activity
 */
public class PhotoGalleryActivity extends SingleFragmentActivity {

    private static final String TAG = "PhotoGalleryActivity";

    public static Intent newIntent(Context context) {
        return new Intent(context, PhotoGalleryActivity.class);
    }

    @Override
    protected Fragment createFragment() {
        return PhotoGalleryFragment.newInstance();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate PhotoGalleryActivity");
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.i(TAG, "onStart PhotoGalleryActivity");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "onResume PhotoGalleryActivity");
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.i(TAG, "onPause PhotoGalleryActivity");
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.i(TAG, "onStop PhotoGalleryActivity");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "onDestroy PhotoGalleryActivity");
    }
}
