package com.rpham64.android.photogallery.network;

import com.rpham64.android.photogallery.network.response.FlickrResponse;
import com.rpham64.android.photogallery.network.response.SizesResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * An interface for handling RxJava and Retrofit HTTP network calls.
 */
public interface ApiService {

    @GET("services/rest?method=flickr.photos.getRecent")
    Call<FlickrResponse> getRecentPhotosRx(@Query("page") int page);

    @GET("services/rest?method=flickr.photos.search")
    Call<FlickrResponse> getPhotosBySearchRx(@Query("page") int page,
                                                   @Query("text") String query,
                                                   @Query("sort") String sortType
    );

    @GET("services/rest?method=flickr.photos.getSizes")
    Call<SizesResponse> getPhotoBySize(@Query("photo_id") String photoId);
}
