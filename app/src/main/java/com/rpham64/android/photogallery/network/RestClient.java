package com.rpham64.android.photogallery.network;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton class for containing and handling networking-related components.
 */
public class RestClient {

    private static final String BASE_URL = "https://api.flickr.com/";
    private static final String API_KEY = "027c43e90b643994b94b559626dc08be";

    // Static reference to this singleton class.
    private static RestClient sInstance;

    private OkHttpClient mOkHttpClient;
    private ApiService mApiService;

    private RestClient() {
        // Private Constructor - Only called when initializing for the first time.
    }

    public static synchronized RestClient getInstance() {
        if (sInstance == null) {
            sInstance = new RestClient();
        }
        return sInstance;
    }

    public ApiService getApiService() {
        if (mApiService == null) {
            // Initialize OkHttp client, Retrofit, and ApiService.
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

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(mOkHttpClient)
                    .build();

            mApiService = retrofit.create(ApiService.class);
        }
        return mApiService;
    }
}
