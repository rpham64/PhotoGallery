package com.rpham64.android.photogallery.ui.gallery;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.rpham64.android.photogallery.R;
import com.rpham64.android.photogallery.utils.ActivityUtils;
import com.rpham64.android.photogallery.utils.SearchQuerySharedPreference;

/**
 * Main Activity that acts as the container for holding the View ({@link PhotoGalleryFragment}) and
 * Presenter ({@link PhotoGalleryPresenter}) components of the Gallery.
 */
public class PhotoGalleryActivity extends AppCompatActivity {

    private static final String TAG = PhotoGalleryActivity.class.getSimpleName();

    private PhotoGalleryPresenter mPhotoGalleryPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        PhotoGalleryFragment photoGalleryFragment =
                (PhotoGalleryFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (photoGalleryFragment == null) {
            photoGalleryFragment = PhotoGalleryFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    photoGalleryFragment, R.id.fragment_container);
        }

        SearchQuerySharedPreference searchQuerySharedPreference = new SearchQuerySharedPreference(this);
        mPhotoGalleryPresenter = new PhotoGalleryPresenter(photoGalleryFragment, searchQuerySharedPreference);
    }
}
