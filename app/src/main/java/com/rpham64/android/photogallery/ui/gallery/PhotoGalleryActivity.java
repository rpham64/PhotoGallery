package com.rpham64.android.photogallery.ui.gallery;

import android.support.v4.app.Fragment;

import com.rpham64.android.photogallery.ui.SingleFragmentActivity;

/**
 * Main Activity of the "Gallery" portion of this app.
 */
public class PhotoGalleryActivity extends SingleFragmentActivity {

    private static final String TAG = PhotoGalleryActivity.class.getSimpleName();

    @Override
    protected Fragment createFragment() {
        return PhotoGalleryFragment.newInstance();
    }
}
