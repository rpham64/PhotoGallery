package com.bignerdranch.android.photogallery;

import android.support.v4.app.Fragment;

/**
 * Main Activity
 */
public class PhotoGalleryActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return PhotoGalleryFragment.newInstance();
    }

}
