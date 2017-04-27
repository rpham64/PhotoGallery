package com.rpham64.android.photogallery.ui.gallery;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.rpham64.android.photogallery.ui.SingleFragmentActivity;

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
}
