package com.rpham64.android.photogallery.network;

import com.rpham64.android.photogallery.network.response.FlickrResponse;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Rudolf on 9/23/2016.
 */

public interface CoreApi {

    String API_KEY = "027c43e90b643994b94b559626dc08be";
    String METHOD_FETCH_RECENTS = "flickr.photos.getRecent";
    String METHOD_SEARCH = "flickr.photos.search";

    String SORT_RELEVANCE = "relevance";

    @GET("services/rest/?" +
            "format=json" +
            "&nojsoncallback=1" +
            "&extras=url_s" +
            "&method=" + METHOD_FETCH_RECENTS +
            "&api_key=" + API_KEY
    )
    Observable<FlickrResponse> getRecentPhotosRx(@Query("page") int page);

    @GET("services/rest/?" +
            "format=json" +
            "&nojsoncallback=1" +
            "&extras=url_s" +
            "&method=" + METHOD_SEARCH +
            "&api_key=" + API_KEY +
            "&sort=" + SORT_RELEVANCE
    )
    Observable<FlickrResponse> getPhotosBySearchRx(@Query("page") int page, @Query("text") String query);
}
