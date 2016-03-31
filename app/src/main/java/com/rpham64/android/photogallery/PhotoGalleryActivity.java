package com.rpham64.android.photogallery;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

/**
 * Main Activity
 */
public class PhotoGalleryActivity extends SingleFragmentActivity {

    public static Intent newIntent(Context context) {
        return new Intent(context, PhotoGalleryActivity.class);
    }

    @Override
    protected Fragment createFragment() {
        return PhotoGalleryFragment.newInstance();
    }

}