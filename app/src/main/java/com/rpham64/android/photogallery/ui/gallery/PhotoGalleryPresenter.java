package com.rpham64.android.photogallery.ui.gallery;

import android.util.Log;

import com.orhanobut.logger.Logger;
import com.rpham64.android.photogallery.models.Photo;
import com.rpham64.android.photogallery.models.Photos;
import com.rpham64.android.photogallery.network.response.FlickrResponse;
import com.rpham64.android.photogallery.base.BasePresenter;
import com.rpham64.android.photogallery.utils.PagedResult;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Rudolf on 9/23/2016.
 */

public class PhotoGalleryPresenter extends BasePresenter<PhotoGalleryContract.View>
        implements PhotoGalleryContract.Presenter {

    private static final String TAG = PhotoGalleryPresenter.class.getSimpleName();

    private static final String SORT_RELEVANCE = "relevance";

    // Callback object for handling response after retrieving photos from Flickr.
    private Callback<FlickrResponse> mFetchPhotosCallback;

    public PhotoGalleryPresenter() {

        // Initialize callback for fetching recent photos and search.
        mFetchPhotosCallback = new Callback<FlickrResponse>() {
            @Override
            public void onResponse(Call<FlickrResponse> call, Response<FlickrResponse> response) {
                Log.i(TAG, "Response: " + response.toString());

                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(TAG, "GET request for recent photos failed.");
                    if (getView() != null) {
                        getView().showError();
                    }
                }

                Photos photosResponse = response.body().photosResponse;
                List<Photo> photoList = photosResponse.photos;
                Log.i(TAG, "Got photos list of size: " + photoList.size());

                // Send photos to the View, if it exists.
                if (getView() != null) {

                    // Create PagedResult to store the response's current page and total number of pages.
                    PagedResult pagedResult = new PagedResult(
                            photosResponse.page,
                            photosResponse.pages
                    );

                    // Pass photo list back to the View.
                    getView().showPhotos(photoList, pagedResult);
                }
            }

            @Override
            public void onFailure(Call<FlickrResponse> call, Throwable t) {
                handleError(t);
            }
        };
    }

    @Override
    public void getRecentPhotos(int page) {
        Call<FlickrResponse> getPhotosCall = getApiService().getRecentPhotosRx(page);
        getPhotosCall.enqueue(mFetchPhotosCallback);
    }

    @Override
    public void searchPhotos(String searchQuery, int page) {
        Call<FlickrResponse> getPhotosBySearchCall =
                getApiService().getPhotosBySearchRx(page, searchQuery, SORT_RELEVANCE);
        getPhotosBySearchCall.enqueue(mFetchPhotosCallback);
    }

    private void handleError(Throwable throwable) {
        Logger.d(throwable.toString());
        throwable.printStackTrace();
        getView().showError();
    }
}
