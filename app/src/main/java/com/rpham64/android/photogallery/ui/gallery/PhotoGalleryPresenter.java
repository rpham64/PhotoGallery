package com.rpham64.android.photogallery.ui.gallery;

import com.orhanobut.logger.Logger;
import com.rpham64.android.photogallery.models.Photo;
import com.rpham64.android.photogallery.network.response.FlickrResponse;
import com.rpham64.android.photogallery.utils.BasePresenter;
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

    private static final String SORT_RELEVANCE = "relevance";

    public PhotoGalleryPresenter() {

    }

    public void setPollService(boolean turnPollServiceOn) {

//        if (turnPollServiceOn) {
//
//            addSubscription(
//                    Observable.create(new Observable.OnSubscribe<T>() {
//                        @Override
//                        public void call(Subscriber<? super T> subscriber) {
//
//                        }
//                    })
//            )
//
//        }

    }

    public void getPage(int page, String query) {

        addSubscription(
                Observable.just(page)
                        .flatMap(pageNumber -> getPagedObservable(query, pageNumber))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError(this::handleError)
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
            return getCoreApi().getRecentPhotosRx(pageNumber);
        } else {
            return getCoreApi().getPhotosBySearchRx(pageNumber, query, SORT_RELEVANCE);
        }

    }

    private void handleError(Throwable throwable) {
        Logger.d(throwable.toString());
        throwable.printStackTrace();
        getView().showError();
    }

    public interface View {
        void showError();
        void showPictures(List<Photo> photos, PagedResult pagedResult);
        void refresh();
    }
}
