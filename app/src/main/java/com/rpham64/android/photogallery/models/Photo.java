package com.rpham64.android.photogallery.models;

import android.net.Uri;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Rudolf on 9/25/2016.
 */

public class Photo {

    public String id;

    public String owner;

    public String title;

    @SerializedName("url_s")
    public String url;

    /**
     * Generates photo page URL
     *
     * @return
     */
    public Uri getPhotoPageUri() {
        return Uri.parse("http://www.flickr.com/photos/")
                .buildUpon()
                .appendPath(owner)
                .appendPath(id)
                .build();
    }

    @Override
    public String toString() {
        return title;
    }
}
