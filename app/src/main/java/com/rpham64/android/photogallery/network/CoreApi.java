package com.rpham64.android.photogallery.network;

import com.rpham64.android.photogallery.network.response.FlickrResponse;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Rudolf on 9/23/2016.
 */

public interface CoreApi {

    @GET("services/rest/?format=json&nojsoncallback=1&extras=url_s")
    Observable<FlickrResponse> getRecentPhotosRx(@Query("page") int page,
                                                 @Query("method") String method,
                                                 @Query("api_key") String key);

    @GET("services/rest/?format=json&nojsoncallback=1&extras=url_s")
    Observable<FlickrResponse> getPhotosBySearchRx(@Query("page") int page,
                                                   @Query("method") String method,
                                                   @Query("api_key") String key,
                                                   @Query("text") String query,
                                                   @Query("sort") String order);
}
