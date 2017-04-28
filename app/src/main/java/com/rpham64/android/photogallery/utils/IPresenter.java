package com.rpham64.android.photogallery.utils;

/**
 * Every presenter in the app must either implement this interface or extend BasePresenter
 * indicating the MvpView type that wants to be attached with.
 *
 * Source: https://github.com/ribot/android-boilerplate/tree/master/app/src/main/java/uk/co/ribot/androidboilerplate/ui/base
 */
public interface IPresenter<V> {

    void attachView(V mvpView);

    void detachView();

    void onResume();
    void onPause();
    void onDestroy();
}