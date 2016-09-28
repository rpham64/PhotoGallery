package com.rpham64.android.photogallery.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Rudolf on 9/28/2016.
 */

public class Photos {

    public int page;

    public int pages;

    @SerializedName("perpage")
    public int perPage;

    public int total;

    @SerializedName("photo")
    public List<Photo> photos;
}
