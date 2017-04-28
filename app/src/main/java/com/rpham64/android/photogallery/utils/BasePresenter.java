package com.rpham64.android.photogallery.utils;

/**
 * Created by Rudolf on 9/26/2016.
 */

import android.support.annotation.CallSuper;

import com.rpham64.android.photogallery.ApplicationController;
import com.rpham64.android.photogallery.network.CoreApi;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Base class that implements the IPresenter interface and provides a base implementation for
 * attachView() and detachView(). It also handles keeping a reference to the mvpView that
 * can be accessed from the children classes by calling getView().
 *
 * Source: https://github.com/ribot/android-boilerplate/tree/master/app/src/main/java/uk/co/ribot/androidboilerplate/ui/base
 */
public class BasePresenter<T> implements IPresenter<T> {

    private T mView;
    private CompositeSubscription subs = new CompositeSubscription();

    @Override
    @CallSuper
    public void onResume() {

    }

    @Override
    @CallSuper
    public void onPause() {

    }

    @Override
    @CallSuper
    public void onDestroy() {
        mView = null;

        if (subs != null) {
            subs.unsubscribe();
        }
    }

    @Override
    public void attachView(T view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    protected void addSubscription(Subscription sub) {
        subs.add(sub);
    }

    protected CoreApi getCoreApi() {
        return ApplicationController.getInstance().getCoreApi();
    }

    public T getView() {
        return mView;
    }
}
