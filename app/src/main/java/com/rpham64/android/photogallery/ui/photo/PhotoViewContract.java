package com.rpham64.android.photogallery.ui.photo;

import com.rpham64.android.photogallery.base.BasePresenter;
import com.rpham64.android.photogallery.base.BaseView;

public interface PhotoViewContract {

    interface View extends BaseView<Presenter> {
        void showPhoto(String url);
    }

    interface Presenter extends BasePresenter {
        void getPhoto();
    }
}
