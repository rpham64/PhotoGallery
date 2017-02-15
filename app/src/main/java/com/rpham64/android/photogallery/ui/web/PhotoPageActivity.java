package com.rpham64.android.photogallery.ui.web;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

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
}
