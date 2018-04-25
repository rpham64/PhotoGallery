package com.rpham64.android.photogallery.ui.gallery;

import com.rpham64.android.photogallery.models.Photo;

import java.util.List;

public interface PhotoGalleryContract {

    interface View {

        void showPhotos(List<Photo> photos, int currentPage);

        void setSearchViewQuery(String query);

        void refresh();

        void showCannotLoadMoreToast();

        void showError();

        void hideError();
    }

    interface Presenter {

        void getPhotos();

        void getRecentPhotos(int page);

        void getPhotosBySearch(String query, int page);

        void loadMorePhotos();

        void onSearchViewClicked();

        void storeSearchQuery(String query);

        void clearSearchQuery();

        void refresh();

        void handleError(Throwable throwable);
    }
}
