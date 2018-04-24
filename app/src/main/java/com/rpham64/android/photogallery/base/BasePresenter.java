package com.rpham64.android.photogallery.base;

import com.rpham64.android.photogallery.network.ApiService;
import com.rpham64.android.photogallery.network.RestClient;
import com.rpham64.android.photogallery.utils.IPresenter;

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
    public void attachView(T view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;

        if (subs != null) {
            subs.unsubscribe();
        }
    }

    @Override
    public T getView() {
        return mView;
    }

    @Override
    public ApiService getApiService() {
        return RestClient.getInstance().getApiService();
    }

    protected void addSubscription(Subscription sub) {
        subs.add(sub);
    }
}
