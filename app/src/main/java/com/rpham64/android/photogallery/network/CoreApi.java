package com.rpham64.android.photogallery.network;

import com.rpham64.android.photogallery.network.response.FlickrResponse;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Rudolf on 9/23/2016.
 */

public interface CoreApi {

    @GET("services/rest/?")
    Observable<FlickrResponse> getRecentPhotosRx(@Query("page") int page,
                                                 @Query("method") String method,
                                                 @Query("api_key") String key,
                                                 @Query("format") String format,
                                                 @Query("nojsoncallback") int callback,
                                                 @Query("extras") String extra);

    @GET("services/rest/?")
    Observable<FlickrResponse> getPhotosBySearchRx(@Query("page") int page,
                                                   @Query("method") String method,
                                                   @Query("api_key") String key,
                                                   @Query("format") String format,
                                                   @Query("nojsoncallback") int callback,
                                                   @Query("extras") String extra,
                                                   @Query("text") String query,
                                                   @Query("sort") String order);
}
