package com.rpham64.android.photogallery.network;

import com.rpham64.android.photogallery.network.response.FlickrResponse;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Rudolf on 9/23/2016.
 */

public interface CoreApi {

    @GET("services/rest")
    Observable<FlickrResponse> getRecentPhotosRx(@Query("method") String method, @Query("page") int page);

    @GET("services/rest")
    Observable<FlickrResponse> getPhotosBySearchRx(@Query("method") String method,
                                                   @Query("page") int page,
                                                   @Query("text") String query,
                                                   @Query("sort") String sortType
    );
}
