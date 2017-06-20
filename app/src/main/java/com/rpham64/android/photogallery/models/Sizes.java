package com.rpham64.android.photogallery.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Rudolf on 6/19/2017.
 */

public class Sizes {

    @SerializedName("canblog")
    public String canBlog;

    @SerializedName("canprint")
    public String canPrint;

    @SerializedName("candownload")
    public String canDownload;

    @SerializedName("size")
    public List<Size> sizesList;
}
