package com.rpham64.android.photogallery;

import android.net.Uri;

/**
 * Photo contents
 *
 * Created by Rudolf on 3/12/2016.
 */
public class GalleryItem {

    private String mCaption;            // Photo Caption/Title
    private String mId;                 // Photo ID
    private String mUrl;                // Photo Link (flickr)
    private String mOwner;              // Photo User ID

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getOwner() {
        return mOwner;
    }

    public void setOwner(String owner) {
        mOwner = owner;
    }

    /**
     * Generates photo page URL
     *
     * @return
     */
    public Uri getPhotoPageUri() {
        return Uri.parse("http://www.flickr.com/photos/")
                .buildUpon()
                .appendPath(mOwner)
                .appendPath(mId)
                .build();
    }

    @Override
    public String toString() {
        return mCaption;
    }
}
