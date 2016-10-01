package com.rpham64.android.photogallery;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.rpham64.android.photogallery.network.CoreApi;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Rudolf on 9/25/2016.
 */

public class ApplicationController extends Application {

    private static final String BASE_URL = "https://api.flickr.com/";

    private static ApplicationController sInstance;

    private OkHttpClient mOkHttpClient;
    private CoreApi mCoreApi;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        Stetho.initializeWithDefaults(this);

        mOkHttpClient = new OkHttpClient().newBuilder()
                .addNetworkInterceptor(new StethoInterceptor())
                .build();
    }

    public static ApplicationController getInstance() {
        return sInstance;
    }

    public CoreApi getCoreApi() {

        if (mCoreApi == null) {

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(mOkHttpClient)
                    .build();

            mCoreApi = retrofit.create(CoreApi.class);

        }

        return mCoreApi;
    }


}
