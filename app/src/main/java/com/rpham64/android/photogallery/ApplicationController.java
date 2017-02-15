package com.rpham64.android.photogallery;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.rpham64.android.photogallery.network.CoreApi;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import tr.xip.errorview.ErrorView;

/**
 * Created by Rudolf on 9/25/2016.
 */

public class ApplicationController extends Application {

    private static final String BASE_URL = "https://api.flickr.com/";
    private static final String API_KEY = "027c43e90b643994b94b559626dc08be";

    private static ApplicationController sInstance;

    private OkHttpClient mOkHttpClient;
    private CoreApi mCoreApi;

    private ErrorView.Config mErrorConfig;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        Stetho.initializeWithDefaults(this);

        mOkHttpClient = new OkHttpClient().newBuilder()
                .addNetworkInterceptor(new StethoInterceptor())
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        HttpUrl url = request.url().newBuilder()
                                .addQueryParameter("api_key", API_KEY)
                                .addQueryParameter("format", "json")
                                .addQueryParameter("nojsoncallback", "1")
                                .addQueryParameter("extras", "url_s")
                                .build();
                        request = request.newBuilder().url(url).build();
                        return chain.proceed(request);
                    }
                })
                .build();

        mErrorConfig = ErrorView.Config.create()
                .title("Oops!")
                .retryVisible(true)
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

    public ErrorView.Config getErrorConfig() {
        return mErrorConfig;
    }
}
