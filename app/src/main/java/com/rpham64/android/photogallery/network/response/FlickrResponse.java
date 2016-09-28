package com.rpham64.android.photogallery.network.response;

import com.google.gson.annotations.SerializedName;
import com.rpham64.android.photogallery.models.Photos;

/**
 * Created by Rudolf on 9/25/2016.
 */

public class FlickrResponse {

    @SerializedName("photos")
    public Photos photosResponse;

    public String stat;
}
