package com.rpham64.android.photogallery.ui.gallery;

import com.rpham64.android.photogallery.models.Photo;
import com.rpham64.android.photogallery.utils.PagedResult;

import java.util.List;

public interface PhotoGalleryContract {

    interface View {
        void showPhotos(List<Photo> photos, PagedResult pagedResult);
        void showError();
        void refresh();
    }

    interface Presenter {
        void getRecentPhotos(int page);
        void searchPhotos(String searchQuery, int page);
    }
}
