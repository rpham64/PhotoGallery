package com.rpham64.android.photogallery.ui.web;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.rpham64.android.photogallery.utils.SingleFragmentActivity;

/**
 * Hosting activity of PhotoPageFragment
 *
 * Displays an image's WebView on click
 *
 * Created by Rudolf on 3/22/2016.
 */
public class PhotoPageActivity extends SingleFragmentActivity {

    private static final String TAG = "PhotoPageActivity";

    private PhotoPageFragment mPhotoPageFragment;

    public static Intent newIntent(Context context, Uri photoPageUri) {

        Intent intent = new Intent(context, PhotoPageActivity.class);
        intent.setData(photoPageUri);

        return intent;
    }

    @Override
    protected Fragment createFragment() {
        mPhotoPageFragment = PhotoPageFragment.newInstance(getIntent().getData());
        return mPhotoPageFragment;
    }

    @Override
    public void onBackPressed() {

        if (mPhotoPageFragment.isBackPressed()) return;

        super.onBackPressed();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate PhotoPageActivity");
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.i(TAG, "onStart PhotoPageActivity");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "onResume PhotoPageActivity");
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.i(TAG, "onPause PhotoPageActivity");
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.i(TAG, "onStop PhotoPageActivity");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "onDestroy PhotoPageActivity");
    }
}
