package com.rpham64.android.photogallery.ui.gallery;

import android.util.Log;

import com.orhanobut.logger.Logger;
import com.rpham64.android.photogallery.models.Photo;
import com.rpham64.android.photogallery.network.response.FlickrResponse;
import com.rpham64.android.photogallery.utils.BasePresenter;
import com.rpham64.android.photogallery.utils.MvpView;
import com.rpham64.android.photogallery.utils.PagedResult;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Rudolf on 9/23/2016.
 */

public class PhotoGalleryPresenter extends BasePresenter<PhotoGalleryPresenter.View> {

    private static final String TAG = PhotoGalleryPresenter.class.getName();

    private static final String API_KEY = "027c43e90b643994b94b559626dc08be";
    private static final String METHOD_FETCH_RECENTS = "flickr.photos.getRecent";
    private static final String METHOD_SEARCH = "flickr.photos.search";

    private static final String SORT_RELEVANCE = "relevance";

    public PhotoGalleryPresenter() {

    }

    public void getPage(Observable<Integer> pagedObservable, String query) {

        addSubscription(
                pagedObservable
                        .flatMap(pageNumber -> getPagedObservable(query, pageNumber))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError(this::handleError)
                        .retry()
                        .subscribe(
                                response -> {
                                    PagedResult pagedResult = new PagedResult(
                                            response.photosResponse.page,
                                            response.photosResponse.pages
                                    );

                                    getView().showPictures(response.photosResponse.photos, pagedResult);
                                },
                                this::handleError
                        )
        );

    }

    private Observable<FlickrResponse> getPagedObservable(String query, int pageNumber) {

        if (query == null) {
            return getCoreApi().getRecentPhotosRx(pageNumber, METHOD_FETCH_RECENTS, API_KEY);
        } else {
            return getCoreApi().getPhotosBySearchRx(pageNumber, METHOD_SEARCH, API_KEY, query, SORT_RELEVANCE);
        }

    }

    private void handleError(Throwable throwable) {
        Log.i(TAG, "Throwable: " + throwable.toString());
        Logger.d(throwable.toString());
        throwable.printStackTrace();
        getView().showError();
    }

    public interface View extends MvpView {
        void showError();
        void showPictures(List<Photo> photos, PagedResult pagedResult);
        void refresh();
        void updateItems();
    }
}
